package com.cinema.movie_booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatStatusDTO {
    private Integer id;
    private String seatNumber;
    private String seatType; // "STANDARD" or "VIP" based on naming convention
    private boolean booked;  // true = đã có người đặt, false = còn trống
}
