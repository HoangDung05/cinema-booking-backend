package com.cinema.movie_booking.repository;

import com.cinema.movie_booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
    // Lấy đơn hàng theo User (cho khách xem lịch sử)
    List<Booking> findByUserId(Integer userId);

    // Lấy đơn hàng theo email User
    List<Booking> findByUserEmail(String email);

    /** Đơn PENDING tạo trước mốc thời gian (dùng để hết hạn thanh toán). */
    List<Booking> findByStatusAndCreatedAtBefore(String status, LocalDateTime createdAtBefore);

    // Lấy toàn bộ đơn hàng, mới nhất lên đầu (cho Admin đối soát)
    List<Booking> findAllByOrderByCreatedAtDesc();

    // Tìm đơn hàng theo trạng thái để tính doanh thu
    List<Booking> findByStatus(String status);
}