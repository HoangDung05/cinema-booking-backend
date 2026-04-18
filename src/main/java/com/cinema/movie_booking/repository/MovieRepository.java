package com.cinema.movie_booking.repository;

import com.cinema.movie_booking.entity.Movie;
import com.cinema.movie_booking.entity.MovieStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Integer> {

    List<Movie> findByReleaseDateAndStatus(LocalDate releaseDate, MovieStatus status);

    List<Movie> findByStatus(MovieStatus status);

    List<Movie> findByReleaseDate(LocalDate releaseDate);

    List<Movie> findByTitleContainingIgnoreCase(String keyword);

}