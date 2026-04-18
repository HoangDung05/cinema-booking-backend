package com.cinema.movie_booking.service.impl;

import com.cinema.movie_booking.dto.AdminStatsDTO;
import com.cinema.movie_booking.dto.BookingRequest;
import com.cinema.movie_booking.dto.BookingResponse;
import com.cinema.movie_booking.dto.BookingHistoryDTO;
import com.cinema.movie_booking.dto.PriceCalculateRequest;
import com.cinema.movie_booking.dto.PriceCalculateResponse;
import com.cinema.movie_booking.service.BookingService;
import com.cinema.movie_booking.service.PendingBookingExpirationService;
import com.cinema.movie_booking.entity.*;
import com.cinema.movie_booking.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final ShowtimeRepository showtimeRepository;
    private final VoucherRepository voucherRepository;
    private final UserRepository userRepository;
    private final SeatRepository seatRepository;
    private final PaymentRepository paymentRepository;
    private final PendingBookingExpirationService pendingBookingExpirationService;

    // ----------------------------------------------------------------
    // API 3: POST /api/bookings/calculate-price
    // Tính tổng tiền (kèm giảm giá voucher) KHÔNG lưu vào DB
    // ----------------------------------------------------------------
    @Transactional(readOnly = true)
    public PriceCalculateResponse calculatePrice(PriceCalculateRequest request) throws Exception {

        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new Exception("Không tìm thấy suất chiếu!"));

        int seatCount = request.getSeatIds().size();
        if (seatCount == 0) throw new Exception("Chưa chọn ghế nào!");

        BigDecimal pricePerSeat = showtime.getPrice();
        BigDecimal subtotal = pricePerSeat.multiply(BigDecimal.valueOf(seatCount));

        // Mặc định không có voucher
        BigDecimal discountAmount = BigDecimal.ZERO;
        String discountDescription = "Không áp dụng mã giảm giá";
        String appliedVoucherCode = null;

        if (request.getVoucherCode() != null && !request.getVoucherCode().isBlank()) {
            Voucher voucher = findValidVoucher(request.getVoucherCode());

            // Kiểm tra đơn hàng tối thiểu
            if (voucher.getMinOrderValue() != null && subtotal.compareTo(voucher.getMinOrderValue()) < 0) {
                throw new Exception("Đơn hàng phải đạt tối thiểu "
                        + voucher.getMinOrderValue() + "đ để dùng mã này!");
            }

            discountAmount = computeDiscount(voucher, subtotal);
            appliedVoucherCode = voucher.getCode();
            discountDescription = buildDiscountDescription(voucher);
        }

        BigDecimal finalTotal = subtotal.subtract(discountAmount).max(BigDecimal.ZERO);

        return new PriceCalculateResponse(
                seatCount,
                pricePerSeat,
                subtotal,
                appliedVoucherCode,
                discountAmount,
                finalTotal,
                discountDescription
        );
    }

    // ----------------------------------------------------------------
    // BƯỚC 1: KHÓA GHẾ (HOLD) - Tạo Booking PENDING
    // ----------------------------------------------------------------
    @Transactional(rollbackFor = Exception.class)
    public BookingResponse holdBooking(BookingRequest request) throws Exception {

        pendingBookingExpirationService.expireStalePendingBookings();

        if (request.getSeatIds() == null || request.getSeatIds().isEmpty()) {
            throw new Exception("Chưa chọn ghế nào!");
        }
        Set<Integer> uniqueSeatIds = new HashSet<>(request.getSeatIds());
        if (uniqueSeatIds.size() != request.getSeatIds().size()) {
            throw new Exception("Danh sách ghế bị trùng. Vui lòng chọn lại ghế!");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new Exception("Không tìm thấy người dùng!"));

        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new Exception("Không tìm thấy suất chiếu!"));

        boolean isSeatTaken = bookingDetailRepository.existsByShowtimeIdAndSeatIdIn(
                request.getShowtimeId(),
                request.getSeatIds()
        );
        if (isSeatTaken) {
            throw new Exception("Ghế bạn chọn đã có người đặt. Vui lòng chọn ghế khác!");
        }

        BigDecimal pricePerSeat = showtime.getPrice();
        BigDecimal subtotal = pricePerSeat.multiply(BigDecimal.valueOf(request.getSeatIds().size()));

        Booking newBooking = new Booking();
        newBooking.setUser(user);
        newBooking.setShowtime(showtime);
        newBooking.setTotalPrice(subtotal); // Giá gốc lúc hold
        newBooking.setStatus("PENDING");
        newBooking.setDiscountAmount(BigDecimal.ZERO);
        Booking savedBooking = bookingRepository.save(newBooking);

        List<BookingDetail> details = new ArrayList<>();
        for (Integer seatId : uniqueSeatIds) {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new Exception("Không tìm thấy ghế ID: " + seatId));

            BookingDetail detail = new BookingDetail();
            detail.setBooking(savedBooking);
            detail.setShowtime(showtime);
            detail.setSeat(seat);
            detail.setPriceAtBooking(pricePerSeat);
            details.add(detail);
        }
        try {
            bookingDetailRepository.saveAll(details);
        } catch (DataIntegrityViolationException e) {
            throw new Exception("Ghế bạn chọn vừa có người khác giữ. Vui lòng tải lại và chọn ghế khác!");
        }

        return new BookingResponse(
                savedBooking.getId(),
                "Khóa ghế thành công. Vui lòng thanh toán trong "
                        + PendingBookingExpirationService.PENDING_PAYMENT_DEADLINE_MINUTES + " phút.",
                subtotal
        );
    }

    // ----------------------------------------------------------------
    // BƯỚC 2: THANH TOÁN (PAY) - Xử lý Voucher và Thanh toán
    // ----------------------------------------------------------------
    @Transactional(rollbackFor = Exception.class)
    public BookingResponse payBooking(Integer bookingId, com.cinema.movie_booking.dto.PayBookingRequest request) throws Exception {
        pendingBookingExpirationService.expireStalePendingBookings();

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new Exception("Không tìm thấy đơn hàng ID: " + bookingId));

        if (!"PENDING".equalsIgnoreCase(booking.getStatus())) {
            throw new Exception("Đơn hàng này không ở trạng thái chờ thanh toán!");
        }

        // Tạm tính giá
        BigDecimal subtotal = booking.getTotalPrice();
        BigDecimal discountAmount = BigDecimal.ZERO;
        Voucher usedVoucher = null;

        if (request.getVoucherCode() != null && !request.getVoucherCode().isBlank()) {
            usedVoucher = findValidVoucher(request.getVoucherCode());

            if (usedVoucher.getUsageLimit() != null
                    && usedVoucher.getUsedCount() >= usedVoucher.getUsageLimit()) {
                throw new Exception("Mã giảm giá đã hết lượt sử dụng!");
            }

            if (usedVoucher.getMinOrderValue() != null
                    && subtotal.compareTo(usedVoucher.getMinOrderValue()) < 0) {
                throw new Exception("Đơn hàng phải đạt tối thiểu " + usedVoucher.getMinOrderValue() + "đ!");
            }

            discountAmount = computeDiscount(usedVoucher, subtotal);
            booking.setVoucher(usedVoucher);
            booking.setDiscountAmount(discountAmount);

            usedVoucher.setUsedCount(usedVoucher.getUsedCount() + 1);
            voucherRepository.save(usedVoucher);
        }

        BigDecimal finalTotal = subtotal.subtract(discountAmount).max(BigDecimal.ZERO);
        booking.setTotalPrice(finalTotal);
        booking.setStatus("PAID");
        bookingRepository.save(booking);

        Payment payment = new Payment();
        payment.setAmount(finalTotal);
        payment.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "MOMO");
        payment.setStatus("SUCCESS");
        payment.setBooking(booking);
        paymentRepository.save(payment);

        return new BookingResponse(
                booking.getId(),
                "Thanh toán hoàn tất!",
                finalTotal
        );
    }

    // ----------------------------------------------------------------
    // Lấy lịch sử đặt vé của User (tìm theo email)
    // ----------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<BookingHistoryDTO> getUserBookings(String email) {
        pendingBookingExpirationService.expireStalePendingBookings();

        List<Booking> bookings = bookingRepository.findByUserEmail(email);
        List<BookingHistoryDTO> result = new ArrayList<>();

        for (Booking b : bookings) {
            String movieTitle = b.getShowtime().getMovie().getTitle();
            Integer movieDuration = b.getShowtime().getMovie().getDuration();
            String posterUrl = b.getShowtime().getMovie().getPosterUrl();
            String cinemaName = b.getShowtime().getRoom().getCinema().getName();

            List<BookingDetail> details = bookingDetailRepository.findByBookingId(b.getId());
            List<String> seatNames = new ArrayList<>();
            for (BookingDetail bd : details) {
                seatNames.add(bd.getSeat().getSeatNumber());
            }

            result.add(new BookingHistoryDTO(
                    b.getId(),
                    movieTitle,
                    movieDuration,
                    posterUrl,
                    cinemaName,
                    b.getShowtime().getStartTime(),
                    b.getTotalPrice(),
                    b.getDiscountAmount(),
                    b.getStatus(),
                    String.join(", ", seatNames),
                    b.getCreatedAt()
            ));
        }
        return result;
    }

    // ----------------------------------------------------------------
    // Helper: Tìm voucher hợp lệ (còn hạn + đang ACTIVE)
    // ----------------------------------------------------------------
    private Voucher findValidVoucher(String code) throws Exception {
        LocalDateTime now = LocalDateTime.now();
        return voucherRepository
                .findByCodeIgnoreCaseAndStatusAndStartDateBeforeAndEndDateAfter(
                        code, "ACTIVE", now, now)
                .orElseThrow(() -> new Exception("Mã giảm giá '" + code + "' không hợp lệ hoặc đã hết hạn!"));
    }

    // ----------------------------------------------------------------
    // Helper: Tính số tiền được giảm dựa trên loại voucher
    // ----------------------------------------------------------------
    private BigDecimal computeDiscount(Voucher voucher, BigDecimal subtotal) {
        BigDecimal discount;
        if ("PERCENT".equalsIgnoreCase(voucher.getDiscountType())) {
            // Giảm theo %: subtotal * discountValue / 100
            discount = subtotal
                    .multiply(voucher.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
            // Không vượt quá maxDiscountAmount nếu có
            if (voucher.getMaxDiscountAmount() != null) {
                discount = discount.min(voucher.getMaxDiscountAmount());
            }
        } else {
            // FIXED: giảm cố định
            discount = voucher.getDiscountValue();
        }
        // Không giảm quá tổng tiền
        return discount.min(subtotal);
    }

    // ----------------------------------------------------------------
    // Helper: Tạo mô tả giảm giá để hiển thị cho khách
    // ----------------------------------------------------------------
    private String buildDiscountDescription(Voucher voucher) {
        if ("PERCENT".equalsIgnoreCase(voucher.getDiscountType())) {
            String desc = "Giảm " + voucher.getDiscountValue().toPlainString() + "%";
            if (voucher.getMaxDiscountAmount() != null) {
                desc += " (tối đa " + voucher.getMaxDiscountAmount().toPlainString() + "đ)";
            }
            return desc;
        } else {
            return "Giảm " + voucher.getDiscountValue().toPlainString() + "đ";
        }
    }

    public List<Booking> getAllBookingsForAdmin() {
        return bookingRepository.findAllByOrderByCreatedAtDesc();
    }

    public AdminStatsDTO getAdminStats() {
        // Chỉ tính doanh thu từ những đơn đã thanh toán (PAID)
        List<Booking> paidBookings = bookingRepository.findByStatus("PAID");

        // 1. Tính tổng doanh thu
        BigDecimal totalRevenue = paidBookings.stream()
                .map(Booking::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Tính tổng số vé (Cộng dồn số lượng ghế trong mỗi đơn PAID)
        long totalTickets = paidBookings.stream()
                .mapToLong(b -> b.getBookingDetails().size())
                .sum();

        // 3. Tính tổng số khách hàng
        long totalCustomers = userRepository.count();

        return new AdminStatsDTO(totalRevenue, totalTickets, totalCustomers);
    }

    @Override
    @Transactional(readOnly = true)
    public Booking getBookingById(Integer id) throws Exception {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new Exception("Không tìm thấy đơn hàng ID: " + id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelBooking(Integer id) throws Exception {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new Exception("Không tìm thấy đơn hàng ID: " + id));

        if (!"PENDING".equalsIgnoreCase(booking.getStatus())) {
            throw new Exception("Chỉ có thể hủy đơn đặt vé đang chờ thanh toán.");
        }

        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);

        // Hoàn lại lượt sử dụng voucher nếu trước đó có áp dụng
        if (booking.getVoucher() != null) {
            Voucher voucher = booking.getVoucher();
            if (voucher.getUsedCount() != null && voucher.getUsedCount() > 0) {
                voucher.setUsedCount(voucher.getUsedCount() - 1);
                voucherRepository.save(voucher);
            }
        }
    }
}