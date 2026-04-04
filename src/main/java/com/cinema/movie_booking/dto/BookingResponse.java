package com.cinema.movie_booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Integer bookingId;
    private String message;
    private BigDecimal totalPrice;
}