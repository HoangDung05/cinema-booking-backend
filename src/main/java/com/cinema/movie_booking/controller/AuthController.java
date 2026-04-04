package com.cinema.movie_booking.controller;

import com.cinema.movie_booking.dto.JwtResponse;
import com.cinema.movie_booking.dto.LoginRequest;
import com.cinema.movie_booking.dto.RegisterRequest;
import com.cinema.movie_booking.service.impl.AuthServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthServiceImpl authService;

    // API Đăng ký: POST http://localhost:8080/api/auth/register
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        try {
            String message = authService.registerUser(request);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // API Đăng nhập: POST http://localhost:8080/api/auth/login
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest request) {
        try {
            JwtResponse jwtResponse = authService.loginUser(request);
            return ResponseEntity.ok(jwtResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(401).body("Sai email hoặc mật khẩu!");
        }
    }
}