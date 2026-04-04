package com.cinema.movie_booking.dto;
import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}