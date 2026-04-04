package com.cinema.movie_booking.repository;

import com.cinema.movie_booking.entity.Cinema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CinemaRepository extends JpaRepository<Cinema, Integer> {
    // Các hàm CRUD cơ bản (Lấy tất cả rạp) đã có sẵn
}