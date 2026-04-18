package com.cinema.movie_booking.dto;

import lombok.Data;

@Data
public class RoomWriteRequest {
    private String name;
    private Integer cinemaId;
}
