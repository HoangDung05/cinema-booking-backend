package com.cinema.movie_booking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cinema.movie_booking.dto.ChangePasswordRequest;
import com.cinema.movie_booking.entity.User;
import com.cinema.movie_booking.service.BookingService;
import com.cinema.movie_booking.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final BookingService bookingService;
    private final com.cinema.movie_booking.service.VoucherService voucherService;

    // Lấy thông tin cá nhân: GET /api/users/me?email=...
    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(@RequestParam String email) {
        return ResponseEntity.ok(userService.getByEmail(email));
    }

    // Cập nhật thông tin cá nhân: PUT /api/users/me?email=...
    @PutMapping("/me")
    public ResponseEntity<?> updateMyProfile(@RequestParam String email, @RequestBody User user) {
        return ResponseEntity.ok(userService.updateProfile(email, user, true));
    }

    @PutMapping("/me/change-password")
    public ResponseEntity<?> changeMyPassword(@RequestParam String email, @RequestBody ChangePasswordRequest request) {
        try {
            userService.changePassword(email, request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok("Đổi mật khẩu thành công.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Lấy lịch sử đặt vé: GET /api/users/me/bookings?email=...
    @GetMapping("/me/bookings")
    public ResponseEntity<?> getUserBookingHistory(@RequestParam String email) {
        try {
            return ResponseEntity.ok(bookingService.getUserBookings(email));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi lấy lịch sử đặt vé: " + e.getMessage());
        }
    }

    // Lấy danh sách ví voucher: GET /api/users/me/vouchers?email=...
    @GetMapping("/me/vouchers")
    public ResponseEntity<?> getMyVouchers(@RequestParam String email) {
        try {
            return ResponseEntity.ok(voucherService.getUserVouchers(email));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
