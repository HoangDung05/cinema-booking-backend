package com.cinema.movie_booking.service.impl;

import com.cinema.movie_booking.entity.Movie;
import com.cinema.movie_booking.entity.MovieStatus;
import com.cinema.movie_booking.repository.MovieRepository;
import com.cinema.movie_booking.service.MovieService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;

    public MovieServiceImpl(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Override
    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    @Override
    public Movie getMovieById(Integer id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phim với ID: " + id));
    }

    @Override
    public Movie saveMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    @Override
    public void deleteMovie(Integer id) {
        if (!movieRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy phim với ID: " + id + " để xóa");
        }
        movieRepository.deleteById(id);
    }

    @Override
    public List<Movie> filterMovies(LocalDate date, MovieStatus status) {
        if (date != null && status != null) {
            return movieRepository.findByReleaseDateAndStatus(date, status);
        } else if (date != null) {
            return movieRepository.findByReleaseDate(date);
        } else if (status != null) {
            return movieRepository.findByStatus(status);
        }
        return movieRepository.findAll();
    }
}