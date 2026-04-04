package com.cinema.movie_booking.repository;

import com.cinema.movie_booking.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Integer> {
    // Lấy toàn bộ ghế của 1 phòng chiếu (Để vẽ sơ đồ ghế trên Frontend)
    List<Seat> findByRoomId(Integer roomId);
}