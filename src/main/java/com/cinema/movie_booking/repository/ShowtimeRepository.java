package com.cinema.movie_booking.repository;

import com.cinema.movie_booking.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Integer> {
        List<Showtime> findByMovieId(Integer movieId);
        List<Showtime> findByRoomId(Integer roomId);

        // 1. Tìm suất chiếu theo phim và thời gian bắt đầu sau thời điểm 'now'
        // Giúp khách hàng không thấy các suất đã chiếu xong
        List<Showtime> findByMovieIdAndStartTimeAfterOrderByStartTimeAsc(Integer movieId, LocalDateTime now);

        // 2. Tìm tất cả suất chiếu trong một khoảng ngày nhất định (cho Admin quản lý)
        List<Showtime> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
}