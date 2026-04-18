package com.cinema.movie_booking.controller;

import com.cinema.movie_booking.dto.AdminShowtimeDTO;
import com.cinema.movie_booking.service.ShowtimeService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/showtimes")
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    // Lấy tất cả suất chiếu
    @GetMapping
    public ResponseEntity<List<AdminShowtimeDTO>> getAll() {
        List<AdminShowtimeDTO> data = showtimeService.getAllShowtimes().stream()
                .map(showtime -> new AdminShowtimeDTO(
                        showtime.getId(),
                        showtime.getStartTime(),
                        showtime.getPrice(),
                        new AdminShowtimeDTO.MovieInfo(
                                showtime.getMovie().getId(),
                                showtime.getMovie().getTitle(),
                                showtime.getMovie().getDuration()),
                        new AdminShowtimeDTO.RoomInfo(
                                showtime.getRoom().getId(),
                                showtime.getRoom().getName())))
                .collect(Collectors.toList());
        return ResponseEntity.ok(data);
    }

    // Lấy 1 xuất chiếu
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(showtimeService.getById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Lấy sơ đồ ghế của 1 suất chiếu: GET /api/showtimes/{id}/seats
    @GetMapping("/{id}/seats")
    public ResponseEntity<?> getSeatsByShowtime(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(showtimeService.getSeatsByShowtimeId(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}