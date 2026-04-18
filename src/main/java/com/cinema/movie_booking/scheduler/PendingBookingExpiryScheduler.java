package com.cinema.movie_booking.scheduler;

import com.cinema.movie_booking.service.PendingBookingExpirationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PendingBookingExpiryScheduler {

    private final PendingBookingExpirationService pendingBookingExpirationService;

    @Scheduled(fixedRate = 30_000)
    public void expireStalePendingBookings() {
        pendingBookingExpirationService.expireStalePendingBookings();
    }
}
