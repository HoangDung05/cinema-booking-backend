package com.cinema.movie_booking.service.impl;

import com.cinema.movie_booking.entity.Showtime;
import com.cinema.movie_booking.repository.ShowtimeRepository;
import com.cinema.movie_booking.service.ShowtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShowtimeServiceImpl implements ShowtimeService {

    private final ShowtimeRepository showtimeRepository;

    @Override
    public List<Showtime> getShowtimesByMovieId(Integer movieId) {
        // We can use findByMovieIdAndStartTimeAfter to only get future showtimes if preferred.
        // But for development/testing, getting all is fine.
        return showtimeRepository.findByMovieId(movieId);
    }
}
