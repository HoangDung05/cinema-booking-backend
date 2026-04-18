package com.cinema.movie_booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminShowtimeDTO {
    private Integer id;
    private LocalDateTime startTime;
    private BigDecimal price;
    private MovieInfo movie;
    private RoomInfo room;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MovieInfo {
        private Integer id;
        private String title;
        private Integer duration;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomInfo {
        private Integer id;
        private String name;
    }
}
