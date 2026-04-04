package com.cinema.movie_booking.service;

import com.cinema.movie_booking.entity.Seat;
import java.util.List;

public interface SeatService {
    List<Seat> getSeatsByRoomId(Integer roomId);
    List<Integer> getBookedSeatIdsForShowtime(Integer showtimeId);
}
