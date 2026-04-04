package com.cinema.movie_booking.repository;

import com.cinema.movie_booking.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Integer> {
    // Lấy các suất chiếu của 1 bộ phim
    List<Showtime> findByMovieId(Integer movieId);

    // Lấy các suất chiếu của 1 phim nhưng phải BẮT ĐẦU SAU thời điểm hiện tại (không chiếu lại phim cũ)
    List<Showtime> findByMovieIdAndStartTimeAfter(Integer movieId, LocalDateTime currentTime);
}