package com.cinema.movie_booking.repository;

import com.cinema.movie_booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
    // Lấy lịch sử đặt vé của 1 khách hàng
    List<Booking> findByUserId(Integer userId);
}