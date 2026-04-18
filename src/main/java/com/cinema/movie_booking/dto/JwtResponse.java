package com.cinema.movie_booking.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Integer id;
    private String email;
    private String fullName;
    private String role;
    /** Giống cột users.role_id / roles.id — JSON có cả roleId và role_id */
    private Integer roleId;

    @JsonGetter("role_id")
    public Integer getRole_id() {
        return roleId;
    }

    public JwtResponse(String token, Integer id, String email, String fullName, String role, Integer roleId) {
        this.token = token;
        this.type = "Bearer";
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.roleId = roleId;
    }

}