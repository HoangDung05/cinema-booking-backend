package com.cinema.movie_booking.service.impl;

import com.cinema.movie_booking.dto.JwtResponse;
import com.cinema.movie_booking.dto.LoginRequest;
import com.cinema.movie_booking.dto.RegisterRequest;
import com.cinema.movie_booking.entity.Role;
import com.cinema.movie_booking.entity.User;
import com.cinema.movie_booking.repository.RoleRepository;
import com.cinema.movie_booking.repository.UserRepository;
import com.cinema.movie_booking.security.JwtUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           @Lazy AuthenticationManager authenticationManager,
                           JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @Transactional(rollbackFor = Exception.class)
    public String registerUser(RegisterRequest request) throws Exception {
        String phone = request.getPhone() != null ? request.getPhone().trim() : "";
        String password = request.getPassword() != null ? request.getPassword() : "";

        if (!phone.matches("\\d{10}")) {
            throw new Exception("Số điện thoại phải gồm đúng 10 chữ số.");
        }
        if (password.length() < 6) {
            throw new Exception("Mật khẩu phải có ít nhất 6 ký tự.");
        }

        // 1. Kiểm tra Email đã tồn tại chưa
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new Exception("Email này đã được sử dụng!");
        }

        // 2. Tìm Role CUSTOMER mặc định (Đảm bảo trong DB bảng roles đã có dòng 'CUSTOMER')
        Role userRole = roleRepository.findByName("CUSTOMER")
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName("CUSTOMER");
                    return roleRepository.save(newRole);
                });

        // 3. Tạo User mới và Mã hóa mật khẩu
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(password)); // Mật khẩu bị băm nát ở đây
        user.setFullName(request.getFullName());
        user.setPhone(phone);
        user.setRole(userRole);

        // 4. Lưu xuống Database
        userRepository.save(user);
        return "Đăng ký tài khoản thành công!";
    }

    @Transactional(readOnly = true)
    public JwtResponse loginUser(LoginRequest request) {
        // 1. Xác thực Username và Password với Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // 2. Lưu trạng thái đăng nhập vào Context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Lấy thông tin User từ DB để trả về Frontend
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        // 4. Tạo JWT Token
        String jwt = jwtUtils.generateJwtToken(authentication.getName());

        // 5. Đóng gói lên Khay trả về
        return new JwtResponse(
                jwt,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().getName(),
                user.getRole().getId()
        );
    }
}