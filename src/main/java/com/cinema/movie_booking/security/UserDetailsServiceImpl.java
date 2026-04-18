package com.cinema.movie_booking.security;

import com.cinema.movie_booking.entity.User;
import com.cinema.movie_booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Tìm User trong Database theo Email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user với email: " + email));

        // Chặn đăng nhập/xác thực nếu tài khoản đã bị khóa
        String status = user.getStatus() == null ? "ACTIVE" : user.getStatus().trim().toUpperCase();
        if ("LOCKED".equals(status)) {
            throw new LockedException("Tài khoản đã bị khóa.");
        }

        // Chuyển đổi Role của bạn thành GrantedAuthority của Spring Security
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().getName());

        // Trả về đối tượng User của Spring Security
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(authority)
        );
    }
}