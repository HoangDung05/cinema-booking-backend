
package com.cinema.movie_booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AdminStatsDTO {
    private BigDecimal totalRevenue; // Tổng doanh thu (các đơn PAID)
    private long totalTickets; // Tổng số vé đã bán
    private long totalCustomers; // Tổng số khách hàng trong hệ thống
}