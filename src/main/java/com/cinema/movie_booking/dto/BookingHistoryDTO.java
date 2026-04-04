package com.cinema.movie_booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookingHistoryDTO {
    private Integer bookingId;
    private String movieTitle;
    private String cinemaName;
    private LocalDateTime showtimeStart;
    private BigDecimal totalPrice;
    private String status;
    private String seatNames; // VD: "A1, A2"
}
