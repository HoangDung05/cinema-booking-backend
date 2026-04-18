package com.cinema.movie_booking.repository;

import com.cinema.movie_booking.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    // JOIN FETCH role trong cùng query — tránh lazy + đảm bảo role_id đúng khi login
    @EntityGraph(attributePaths = {"role"})
    Optional<User> findByEmail(String email);

    // Eagerly load role for findAll
    @EntityGraph(attributePaths = {"role"})
    List<User> findAll();

    // Kiểm tra xem Email đã được đăng ký chưa
    boolean existsByEmail(String email);

    List<User> findByRoleName(String roleName);

    List<User> findByRoleId(Integer roleId);
}