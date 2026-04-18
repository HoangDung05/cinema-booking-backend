package com.cinema.movie_booking.service;

import java.util.List;
import com.cinema.movie_booking.entity.User;

public interface UserService {
    // Nhóm cho Admin
    List<User> getAllUsers();

    List<User> getAllCustomers();

    void deleteUser(Integer id);

    User getById(Integer id);

    // Nhóm cho Profile cá nhân
    User getByEmail(String email);

    // identifier: chính là cái email gốc để tìm user
    // isSelfUpdate: true nếu là User tự sửa, false nếu là Admin sửa
    User updateProfile(String identifier, User updatedUser, boolean isSelfUpdate);

    User updateUserStatus(Integer id, String newStatus);

    User updateUser(Integer id, User user);

    /** Đổi role (CUSTOMER <-> ADMIN) */
    User updateUserRole(Integer id, String roleName);

    void changePassword(String email, String currentPassword, String newPassword);

}