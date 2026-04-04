package com.cinema.movie_booking.security;

import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.security.Key;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {
    // Chìa khóa bí mật (Thực tế nên để trong application.properties)
    private final String jwtSecret = "CinemaTicketBookingSecretKeyMustBeLongEnough12345678901234567890"; // changed to >= 64 bytes
    private final int jwtExpirationMs = 86400000; // 1 ngày

    private Key key() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // Tạo Token khi Login thành công
    public String generateJwtToken(String username) {
        return Jwts.builder()
                .setSubject((username))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS512)
                .compact();
    }

    // Lấy Username từ Token
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token).getBody().getSubject();
    }

    // Kiểm tra Token hợp lệ
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken);
            return true;
        } catch (Exception e) {
            System.out.println("Lỗi Token: " + e.getMessage());
        }
        return false;
    }
}