package com.cinema.movie_booking.repository;

import com.cinema.movie_booking.entity.BookingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingDetailRepository extends JpaRepository<BookingDetail, Integer> {
    // Hàm quan trọng nhất: Check xem danh sách ghế này đã bị ai đặt trong suất chiếu này chưa
    boolean existsByShowtimeIdAndSeatIdIn(Integer showtimeId, List<Integer> seatIds);

    // Lấy chi tiết các ghế đã đặt của 1 đơn hàng cụ thể
    List<BookingDetail> findByBookingId(Integer bookingId);

    // Lấy danh sách ID các ghế đã được đặt cho một suất chiếu
    @org.springframework.data.jpa.repository.Query("SELECT bd.seat.id FROM BookingDetail bd WHERE bd.showtime.id = :showtimeId")
    List<Integer> findBookedSeatIdsByShowtimeId(@org.springframework.data.repository.query.Param("showtimeId") Integer showtimeId);
}