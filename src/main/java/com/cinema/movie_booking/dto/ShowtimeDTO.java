package com.cinema.movie_booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShowtimeDTO {
    private Integer id;
    private LocalDateTime startTime;
    private BigDecimal price;

    // Room info
    private Integer roomId;
    private String roomName;

    // Cinema info
    private Integer cinemaId;
    private String cinemaName;
    private String cinemaAddress;
}
