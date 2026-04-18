package com.cinema.movie_booking.service;

import java.util.List;

import com.cinema.movie_booking.dto.AdminStatsDTO;
import com.cinema.movie_booking.dto.BookingHistoryDTO;
import com.cinema.movie_booking.dto.BookingRequest;
import com.cinema.movie_booking.dto.BookingResponse;
import com.cinema.movie_booking.dto.PayBookingRequest;
import com.cinema.movie_booking.dto.PriceCalculateRequest;
import com.cinema.movie_booking.dto.PriceCalculateResponse;
import com.cinema.movie_booking.entity.Booking;

public interface BookingService {
    List<Booking> getAllBookingsForAdmin();

    AdminStatsDTO getAdminStats();

    PriceCalculateResponse calculatePrice(PriceCalculateRequest request) throws Exception;

    BookingResponse holdBooking(BookingRequest request) throws Exception;

    BookingResponse payBooking(Integer bookingId, PayBookingRequest request) throws Exception;

    List<BookingHistoryDTO> getUserBookings(String email);

    Booking getBookingById(Integer id) throws Exception;

    void cancelBooking(Integer id) throws Exception;
}