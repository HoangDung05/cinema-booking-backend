package com.cinema.movie_booking.service;

import com.cinema.movie_booking.dto.SeatStatusDTO;
import com.cinema.movie_booking.dto.ShowtimeDTO;
import com.cinema.movie_booking.entity.Showtime;

import java.util.List;

public interface ShowtimeService {
    // 1. Khách hàng: Lấy suất chiếu theo phim (chưa diễn ra) - trả entity (legacy)
    List<Showtime> getByMovieId(Integer movieId);

    // 2. Khách hàng: Lấy suất chiếu kèm thông tin rạp - dùng cho trang Chi tiết Phim
    List<ShowtimeDTO> getShowtimeDTOsByMovieId(Integer movieId);

    // 3. Khách hàng: Sơ đồ ghế của một suất chiếu
    List<SeatStatusDTO> getSeatsByShowtimeId(Integer showtimeId);

    // 4. Admin: Chi tiết 1 suất chiếu
    Showtime getById(Integer id);

    // 5. Admin: Thêm mới suất chiếu (có check trùng lịch)
    Showtime createShowtime(Showtime showtime);

    // 6. Admin: Cập nhật suất chiếu
    Showtime updateShowtime(Integer id, Showtime showtime);

    // 7. Admin: Xóa suất chiếu
    void deleteShowtime(Integer id);

    List<Showtime> getAllShowtimes();

    List<Showtime> getShowtimesByMovieId(Integer movieId);
}
