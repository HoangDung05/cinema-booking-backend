package com.cinema.movie_booking.repository;

import com.cinema.movie_booking.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    // Tìm giao dịch thanh toán dựa trên mã đơn đặt vé
    Optional<Payment> findByBookingId(Integer bookingId);
}