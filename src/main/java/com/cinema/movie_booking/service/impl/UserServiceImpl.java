package com.cinema.movie_booking.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import com.cinema.movie_booking.entity.Role;
import com.cinema.movie_booking.entity.User;
import com.cinema.movie_booking.repository.RoleRepository;
import com.cinema.movie_booking.repository.UserRepository;
import com.cinema.movie_booking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<User> getAllCustomers() {
        // Tận dụng hàm findByRoleName bạn đã có trong Repo
        return userRepository.findByRoleName("CUSTOMER");
    }

    @Override
    public User getById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User không tồn tại với ID: " + id));
    }

    @Override
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với Email: " + email));
    }

    @Override
    public User updateProfile(String email, User userDetails, boolean isSelfUpdate) {
        // 1. Tìm người dùng hiện tại trong DB dựa trên email gốc
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email: " + email));

        // 2. CẬP NHẬT CÁC TRƯỜNG CHUNG (Cả User và Admin đều sửa được)
        user.setFullName(userDetails.getFullName());
        user.setPhone(userDetails.getPhone());
        user.setAvatarUrl(userDetails.getAvatarUrl());

        // 3. KHÔNG CHO PHÉP SỬA EMAIL

        // 4. KIỂM TRA QUYỀN HẠN (Dựa vào biến isSelfUpdate)
        if (isSelfUpdate) {

            System.out.println("User " + email + " đang tự cập nhật thông tin cá nhân.");
        } else {

            if (userDetails.getStatus() != null) {
                user.setStatus(userDetails.getStatus());
            }

            System.out.println("Admin đang cập nhật thông tin cho User: " + email);
        }

        // 5. Lưu lại vào Database
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Không thể xóa vì User không tồn tại");
        }
        userRepository.deleteById(id);
    }

    @Override
    public User updateUserStatus(Integer id, String newStatus) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy User ID: " + id));

        // Chuẩn hóa chuỗi (Viết hoa hết cho chắc)
        String status = newStatus.toUpperCase();

        if (status.equals("ACTIVE") || status.equals("LOCKED")) {
            user.setStatus(status);
        } else {
            throw new RuntimeException("Trạng thái không hợp lệ! Chỉ được dùng ACTIVE hoặc LOCKED");
        }

        return userRepository.save(user);
    }

    @Override
    public User updateUser(Integer id, User userDetails) {
        throw new RuntimeException("Admin chỉ được phép cập nhật phân quyền người dùng.");
    }

    @Override
    public User updateUserRole(Integer id, String roleName) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy User ID: " + id));

        String normalized = roleName == null ? "" : roleName.trim().toUpperCase();
        if (!normalized.equals("ADMIN") && !normalized.equals("CUSTOMER")) {
            throw new RuntimeException("Role không hợp lệ! Chỉ được dùng ADMIN hoặc CUSTOMER");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = authentication != null ? authentication.getName() : null;
        if ("CUSTOMER".equals(normalized)
                && currentEmail != null
                && currentEmail.equalsIgnoreCase(user.getEmail())) {
            throw new RuntimeException("Bạn không thể tự hạ quyền tài khoản admin đang đăng nhập.");
        }

        Role role = roleRepository.findByName(normalized)
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName(normalized);
                    return roleRepository.save(r);
                });

        user.setRole(role);
        return userRepository.save(user);
    }

    @Override
    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email: " + email));

        if (currentPassword == null || currentPassword.isBlank()) {
            throw new RuntimeException("Vui lòng nhập mật khẩu hiện tại.");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("Mật khẩu mới phải có ít nhất 6 ký tự.");
        }
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Mật khẩu hiện tại không đúng.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

}