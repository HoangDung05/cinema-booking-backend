package com.cinema.movie_booking.dto;

import lombok.Data;

import java.util.List;

@Data
public class PriceCalculateRequest {
    private Integer showtimeId;
    private List<Integer> seatIds;
    private String voucherCode; // Optional - có thể null nếu không dùng voucher
}
