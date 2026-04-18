package com.cinema.movie_booking.service;

import com.cinema.movie_booking.entity.Booking;
import com.cinema.movie_booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Đơn PENDING quá thời gian thanh toán → CANCELLED. Ghế được giải phóng vì truy vấn ghế đã đặt loại trừ CANCELLED.
 */
@Service
@RequiredArgsConstructor
public class PendingBookingExpirationService {

    public static final int PENDING_PAYMENT_DEADLINE_MINUTES = 2;

    private final BookingRepository bookingRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void expireStalePendingBookings() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(PENDING_PAYMENT_DEADLINE_MINUTES);
        List<Booking> stale = bookingRepository.findByStatusAndCreatedAtBefore("PENDING", cutoff);
        for (Booking b : stale) {
            b.setStatus("CANCELLED");
        }
        bookingRepository.saveAll(stale);
    }
}
