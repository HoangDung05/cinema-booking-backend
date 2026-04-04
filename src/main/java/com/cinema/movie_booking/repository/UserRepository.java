package com.cinema.movie_booking.repository;

import com.cinema.movie_booking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    // Tìm User theo Email để phục vụ lúc Login
    Optional<User> findByEmail(String email);

    // Kiểm tra xem Email đã được đăng ký chưa
    boolean existsByEmail(String email);
}