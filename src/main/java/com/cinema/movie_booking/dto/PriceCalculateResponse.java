package com.cinema.movie_booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceCalculateResponse {
    private int seatCount;
    private BigDecimal pricePerSeat;
    private BigDecimal subtotal;         // pricePerSeat * seatCount (trước giảm giá)
    private String voucherCode;          // null nếu không dùng voucher
    private BigDecimal discountAmount;   // số tiền được giảm (0 nếu không có voucher)
    private BigDecimal finalTotal;       // tổng sau khi giảm giá
    private String discountDescription; // mô tả giảm giá VD: "Giảm 20% (tối đa 50,000đ)"
}
