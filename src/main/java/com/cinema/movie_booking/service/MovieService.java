package com.cinema.movie_booking.service;

import com.cinema.movie_booking.entity.Movie;
import com.cinema.movie_booking.entity.MovieStatus;

import java.time.LocalDate;
import java.util.List;

public interface MovieService {

    List<Movie> getAllMovies();

    Movie getMovieById(Integer id);

    Movie saveMovie(Movie movie);

    void deleteMovie(Integer id);

    List<Movie> filterMovies(LocalDate date, MovieStatus status);

    List<Movie> getNowShowingMovies();

    List<Movie> getComingSoonMovies();

    List<Movie> searchMovies(String keyword);

    List<Movie> filterMovies(LocalDate date);
}