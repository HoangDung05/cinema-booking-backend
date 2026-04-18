package com.cinema.movie_booking.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter chạy MỖI REQUEST một lần.
 * Nhiệm vụ: Đọc JWT từ header "Authorization: Bearer <token>",
 * xác thực token, và nếu hợp lệ thì đặt Authentication vào SecurityContext
 * để Spring Security biết đây là user đã đăng nhập.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // Bước 1: Lấy token từ header Authorization
            String jwt = parseJwt(request);

            // Bước 2: Nếu có token và token hợp lệ thì xác thực
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {

                // Lấy email (username) từ token
                String email = jwtUtils.getUserNameFromJwtToken(jwt);

                // Load thông tin user từ DB
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // Tạo đối tượng Authentication và đặt vào SecurityContext
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            System.err.println("Không thể xác thực JWT token: " + e.getMessage());
        }

        // Tiếp tục chuỗi filter bất kể có token hợp lệ hay không
        filterChain.doFilter(request, response);
    }

    /**
     * Tách phần token ra khỏi header "Authorization: Bearer eyJhbGc..."
     * Trả về null nếu header không đúng định dạng.
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7); // Bỏ "Bearer " (7 ký tự)
        }
        return null;
    }
}
