package com.cinema.movie_booking.repository;

import com.cinema.movie_booking.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    // Tìm Role (ví dụ: CUSTOMER, ADMIN) để gán cho User lúc đăng ký
    Optional<Role> findByName(String name);
}