package com.cinema.movie_booking.repository;

import com.cinema.movie_booking.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {
    // Lấy danh sách phòng chiếu của 1 rạp cụ thể
    List<Room> findByCinemaId(Integer cinemaId);
}