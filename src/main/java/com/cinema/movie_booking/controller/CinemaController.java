package com.cinema.movie_booking.controller;

import com.cinema.movie_booking.entity.Cinema;
import com.cinema.movie_booking.service.CinemaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/cinemas")
public class CinemaController {

    private final CinemaService cinemaService;

    public CinemaController(CinemaService cinemaService) {
        this.cinemaService = cinemaService;
    }

    // 1. Lấy danh sách tất cả rạp
    @GetMapping
    public List<Cinema> getAll() {
        return cinemaService.getAllCinemas();
    }

    // 2. Lấy chi tiết 1 rạp theo ID
    @GetMapping("/{id}")
    public ResponseEntity<Cinema> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(cinemaService.getCinemaById(id));
    }

}