package com.cinema.movie_booking.controller;

import com.cinema.movie_booking.dto.BookingRequest;
import com.cinema.movie_booking.dto.BookingResponse;
import com.cinema.movie_booking.dto.PayBookingRequest;
import com.cinema.movie_booking.dto.PriceCalculateRequest;
import com.cinema.movie_booking.dto.PriceCalculateResponse;
import com.cinema.movie_booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // API 3: POST /api/bookings/calculate-price
    // Tính giá nháp (kèm voucher) KHÔNG lưu DB – dùng để hiển thị tóm tắt trước khi thanh toán
    @PostMapping("/calculate-price")
    public ResponseEntity<?> calculatePrice(@RequestBody PriceCalculateRequest request) {
        try {
            PriceCalculateResponse response = bookingService.calculatePrice(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // API 4: POST /api/bookings/hold
    // Nhận chỗ: lưu DB trạng thái PENDING, khóa ghế
    @PostMapping("/hold")
    public ResponseEntity<?> holdBooking(@RequestBody BookingRequest request) {
        try {
            BookingResponse response = bookingService.holdBooking(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // API 4.5: POST /api/bookings/{id}/pay
    // Thanh toán: xác nhận thẻ/momo, áp dụng voucher, đổi PENDING -> PAID
    @PostMapping("/{id}/pay")
    public ResponseEntity<?> payBooking(@PathVariable Integer id, @RequestBody PayBookingRequest request) {
        try {
            BookingResponse response = bookingService.payBooking(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // API 5: GET /api/bookings/{id}
    // Xem chi tiết hóa đơn
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookingDetails(@PathVariable Integer id) {
        try {
            com.cinema.movie_booking.entity.Booking booking = bookingService.getBookingById(id);
            
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("id", booking.getId());
            response.put("status", booking.getStatus());
            response.put("createdAt", booking.getCreatedAt());
            response.put("totalPrice", booking.getTotalPrice());
            response.put("discountAmount", booking.getDiscountAmount());
            
            if (booking.getShowtime() != null) {
                java.util.Map<String, Object> showtimeData = new java.util.HashMap<>();
                showtimeData.put("showtimeId", booking.getShowtime().getId());
                showtimeData.put("startTime", booking.getShowtime().getStartTime());
                showtimeData.put("movieTitle", booking.getShowtime().getMovie().getTitle());
                showtimeData.put("movieDuration", booking.getShowtime().getMovie().getDuration());
                showtimeData.put("posterUrl", booking.getShowtime().getMovie().getPosterUrl());
                showtimeData.put("roomName", booking.getShowtime().getRoom().getName());
                showtimeData.put("cinemaName", booking.getShowtime().getRoom().getCinema().getName());
                response.put("showtime", showtimeData);
            }
            
            java.util.List<String> seats = booking.getBookingDetails().stream()
                .map(d -> d.getSeat().getSeatNumber())
                .collect(java.util.stream.Collectors.toList());
            response.put("seats", seats);
            java.util.List<java.util.Map<String, Object>> seatDetails = booking.getBookingDetails().stream()
                .map(d -> {
                    java.util.Map<String, Object> seat = new java.util.HashMap<>();
                    seat.put("id", d.getSeat().getId());
                    seat.put("seatNumber", d.getSeat().getSeatNumber());
                    return seat;
                })
                .collect(java.util.stream.Collectors.toList());
            response.put("seatDetails", seatDetails);
            
            if (booking.getVoucher() != null) {
                response.put("voucherCode", booking.getVoucher().getCode());
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // API 6: DELETE /api/bookings/{id}
    // Hủy đơn đặt vé (Giải phóng ghế nếu quá thời gian thanh toán)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelBooking(@PathVariable Integer id) {
        try {
            bookingService.cancelBooking(id);
            return ResponseEntity.ok("Đã hủy đơn hàng và giải phóng ghế thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
