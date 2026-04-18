package com.cinema.movie_booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayBookingRequest {
    private String paymentMethod;
    private String voucherCode;
}
