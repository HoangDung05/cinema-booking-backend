package com.cinema.movie_booking.dto;

import lombok.Data;
import java.util.List;

@Data
public class BookingRequest {
    private Integer userId;
    private Integer showtimeId;
    private List<Integer> seatIds;
}