package com.cinema.movie_booking.controller;

import com.cinema.movie_booking.dto.BookingRequest;
import com.cinema.movie_booking.dto.BookingResponse;
import com.cinema.movie_booking.service.impl.BookingServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingServiceImpl bookingService;

    // API Đặt vé: POST http://localhost:8080/api/bookings
    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest request) {
        try {
            // Chuyển Request cho Service xử lý (Bao gồm check ghế, tính tiền, lưu DB)
            BookingResponse response = bookingService.createBooking(request);

            // Trả về mã 200 OK cùng thông tin đơn hàng
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // Nếu có lỗi (VD: "Ghế đã có người đặt"), trả về mã 400 Bad Request kèm câu thông báo lỗi
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // API Lấy lịch sử đặt vé của User: GET http://localhost:8080/api/bookings/user/{userId}
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserBookingHistory(@PathVariable Integer userId) {
        try {
            return ResponseEntity.ok(bookingService.getUserBookings(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi lấy lịch sử đặt vé: " + e.getMessage());
        }
    }
}