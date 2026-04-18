package com.cinema.movie_booking.repository;

import com.cinema.movie_booking.entity.Room;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {

    @EntityGraph(attributePaths = {"cinema"})
    @Query("SELECT r FROM Room r ORDER BY r.id")
    List<Room> findAllWithCinema();

    @EntityGraph(attributePaths = {"cinema"})
    @Query("SELECT r FROM Room r WHERE r.id = :id")
    Optional<Room> findByIdWithCinema(@Param("id") Integer id);

    List<Room> findByCinemaId(Integer cinemaId);
}