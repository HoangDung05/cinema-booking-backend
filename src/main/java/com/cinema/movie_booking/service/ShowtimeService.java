package com.cinema.movie_booking.service;

import com.cinema.movie_booking.entity.Showtime;
import java.util.List;

public interface ShowtimeService {
    List<Showtime> getShowtimesByMovieId(Integer movieId);
}
