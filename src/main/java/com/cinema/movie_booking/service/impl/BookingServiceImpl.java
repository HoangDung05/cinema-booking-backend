package com.cinema.movie_booking.service.impl;

import com.cinema.movie_booking.dto.BookingRequest;
import com.cinema.movie_booking.dto.BookingResponse;
import com.cinema.movie_booking.entity.*;
import com.cinema.movie_booking.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor // Tự động Inject các Repository mà không cần @Autowired
public class BookingServiceImpl {

    private final BookingRepository bookingRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final SeatRepository seatRepository;
    private final ShowtimeRepository showtimeRepository;
    private final UserRepository userRepository;

    @Transactional(rollbackFor = Exception.class)
    public BookingResponse createBooking(BookingRequest request) throws Exception {

        // BƯỚC 1: Lấy thông tin User và Showtime từ Database (Kiểm tra xem có tồn tại không)
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new Exception("Không tìm thấy thông tin người dùng!"));

        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new Exception("Không tìm thấy thông tin suất chiếu!"));

        // BƯỚC 2: CHECK TRÙNG GHẾ
        // Dùng hàm có sẵn của Spring Data JPA để đếm xem có ghế nào trong danh sách đã bị đặt chưa
        boolean isSeatTaken = bookingDetailRepository.existsByShowtimeIdAndSeatIdIn(
                request.getShowtimeId(),
                request.getSeatIds()
        );

        if (isSeatTaken) {
            throw new Exception("Ghế bạn chọn đã có người đặt. Vui lòng chọn ghế khác!");
        }

        // BƯỚC 3: TÍNH TỔNG TIỀN
        // Dựa vào SQL của bạn, giá vé nằm ở bảng Showtime.
        BigDecimal ticketPrice = showtime.getPrice();
        // Tổng tiền = Giá vé * Số lượng ghế chọn
        BigDecimal totalAmount = ticketPrice.multiply(new BigDecimal(request.getSeatIds().size()));

        // BƯỚC 4: TẠO BOOKING (Đơn hàng tổng)
        Booking newBooking = new Booking();
        newBooking.setUser(user); // Truyền vào cả Object User
        newBooking.setShowtime(showtime); // Truyền vào cả Object Showtime
        newBooking.setTotalPrice(totalAmount);
        newBooking.setStatus("PENDING"); // Để PENDING chờ thanh toán

        Booking savedBooking = bookingRepository.save(newBooking);

        // BƯỚC 5: TẠO CHI TIẾT BOOKING (Từng ghế một)
        List<BookingDetail> details = new ArrayList<>();

        for (Integer seatId : request.getSeatIds()) {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new Exception("Không tìm thấy ghế có ID: " + seatId));

            BookingDetail detail = new BookingDetail();
            detail.setBooking(savedBooking);
            detail.setShowtime(showtime);
            detail.setSeat(seat);
            detail.setPriceAtBooking(ticketPrice); // Lưu lại giá vé tại thời điểm đặt để đối soát sau này

            details.add(detail);
        }

        // Lưu danh sách chi tiết vé vào DB cùng lúc
        bookingDetailRepository.saveAll(details);
        return new BookingResponse(
                savedBooking.getId(),
                "Đặt vé thành công! Vui lòng tiến hành thanh toán.",
                totalAmount
        );
    }

    @Transactional(readOnly = true)
    public List<com.cinema.movie_booking.dto.BookingHistoryDTO> getUserBookings(Integer userId) {
        List<Booking> bookings = bookingRepository.findByUserId(userId);
        List<com.cinema.movie_booking.dto.BookingHistoryDTO> result = new ArrayList<>();

        for (Booking b : bookings) {
            String movieTitle = b.getShowtime().getMovie().getTitle();
            String cinemaName = b.getShowtime().getRoom().getCinema().getName(); // Assuming Room has Cinema
            
            // Lấy danh sách tên ghế từ BookingDetail
            List<BookingDetail> details = bookingDetailRepository.findByBookingId(b.getId());
            List<String> seatNames = new ArrayList<>();
            for (BookingDetail bd : details) {
                seatNames.add(bd.getSeat().getSeatNumber());
            }
            String seatsStr = String.join(", ", seatNames);

            result.add(new com.cinema.movie_booking.dto.BookingHistoryDTO(
                    b.getId(),
                    movieTitle,
                    cinemaName,
                    b.getShowtime().getStartTime(),
                    b.getTotalPrice(),
                    b.getStatus(),
                    seatsStr
            ));
        }

        return result;
    }
}