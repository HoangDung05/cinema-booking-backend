package com.cinema.movie_booking.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Integer id;
    private String email;
    private String fullName;
    private String role;

    public JwtResponse(String token, Integer id, String email, String fullName, String role) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }

}