package com.cinema.movie_booking.controller;

import com.cinema.movie_booking.dto.ShowtimeDTO;
import com.cinema.movie_booking.entity.Movie;
import com.cinema.movie_booking.entity.MovieStatus;
import com.cinema.movie_booking.service.MovieService;
import com.cinema.movie_booking.service.ShowtimeService;

import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;
    private final ShowtimeService showtimeService;

    // 1. API LẤY DANH SÁCH & LỌC

    @GetMapping
    public ResponseEntity<List<Movie>> getAllOrFilterMovies(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) MovieStatus status) {

        List<Movie> movies = movieService.filterMovies(date, status);
        return ResponseEntity.ok(movies);
    }

    // 2. API LẤY CHI TIẾT 1 PHIM THEO ID

    @GetMapping("/{id}")
    public ResponseEntity<Movie> getMovieById(@PathVariable Integer id) {
        Movie movie = movieService.getMovieById(id);
        return ResponseEntity.ok(movie);
    }
    
    // 3. Lấy phim đang chiếu
    @GetMapping("/now-showing")
    public ResponseEntity<List<Movie>> getNowShowing() {
        return ResponseEntity.ok(movieService.getNowShowingMovies());
    }

    // 4. Lấy phim sắp chiếu
    @GetMapping("/coming-soon")
    public ResponseEntity<List<Movie>> getComingSoon() {
        return ResponseEntity.ok(movieService.getComingSoonMovies());
    }

    // Lấy các suất chiếu của một bộ phim cụ thể (Để vẽ bảng chọn giờ cho khách)
    @GetMapping("/{movieId}/showtimes")
    public ResponseEntity<List<ShowtimeDTO>> getShowtimesByMovie(@PathVariable Integer movieId) {
        List<ShowtimeDTO> showtimes = showtimeService.getShowtimeDTOsByMovieId(movieId);
        return ResponseEntity.ok(showtimes);
    }

    // 5. Tìm kiếm phim:
    @GetMapping("/search")
    public ResponseEntity<List<Movie>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(movieService.searchMovies(keyword));
    }

    // 6. Lọc phim:
    @GetMapping("/filter")
    public ResponseEntity<List<Movie>> filter(
            @RequestParam(required = false) String date) {

        if (date != null) {
            LocalDate localDate = LocalDate.parse(date);
            return ResponseEntity.ok(movieService.filterMovies(localDate));
        }

        return ResponseEntity.ok(movieService.getAllMovies());
    }

    // --- Admin CRUD (yêu cầu JWT) ---

    @PostMapping
    public ResponseEntity<Movie> createMovie(@RequestBody Movie movie) {
        movie.setId(null);
        Movie saved = movieService.saveMovie(movie);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Movie> updateMovie(@PathVariable Integer id, @RequestBody Movie movie) {
        Movie existing = movieService.getMovieById(id);
        existing.setTitle(movie.getTitle());
        existing.setDescription(movie.getDescription());
        existing.setDuration(movie.getDuration());
        existing.setReleaseDate(movie.getReleaseDate());
        existing.setPosterUrl(movie.getPosterUrl());
        existing.setTrailerUrl(movie.getTrailerUrl());
        existing.setStatus(movie.getStatus());
        return ResponseEntity.ok(movieService.saveMovie(existing));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Integer id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }
}