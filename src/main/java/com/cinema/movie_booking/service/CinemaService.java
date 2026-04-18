package com.cinema.movie_booking.service;

import com.cinema.movie_booking.entity.Cinema;
import java.util.List;

public interface CinemaService {
    List<Cinema> getAllCinemas();

    Cinema getCinemaById(Integer id);

    Cinema createCinema(Cinema cinema);

    Cinema updateCinema(Integer id, Cinema cinema);

    void deleteCinema(Integer id);
}