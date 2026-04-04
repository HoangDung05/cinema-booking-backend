package com.cinema.movie_booking.controller;

import com.cinema.movie_booking.entity.Showtime;
import com.cinema.movie_booking.service.ShowtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/showtimes")
@RequiredArgsConstructor
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    // API Lấy danh sách lịch chiếu của 1 Phim: GET http://localhost:8080/api/showtimes/movie/{movieId}
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<Showtime>> getShowtimesByMovie(@PathVariable Integer movieId) {
        return ResponseEntity.ok(showtimeService.getShowtimesByMovieId(movieId));
    }
}
