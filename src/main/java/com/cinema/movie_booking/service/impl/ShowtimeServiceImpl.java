package com.cinema.movie_booking.service.impl;

import com.cinema.movie_booking.dto.SeatStatusDTO;
import com.cinema.movie_booking.dto.ShowtimeDTO;
import com.cinema.movie_booking.entity.Room;
import com.cinema.movie_booking.entity.Seat;
import com.cinema.movie_booking.entity.Showtime;
import com.cinema.movie_booking.repository.BookingDetailRepository;
import com.cinema.movie_booking.repository.MovieRepository;
import com.cinema.movie_booking.repository.RoomRepository;
import com.cinema.movie_booking.repository.SeatRepository;
import com.cinema.movie_booking.repository.ShowtimeRepository;
import com.cinema.movie_booking.service.PendingBookingExpirationService;
import com.cinema.movie_booking.service.ShowtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShowtimeServiceImpl implements ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final PendingBookingExpirationService pendingBookingExpirationService;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;

    // 1. Lấy suất chiếu theo phim (còn sắp chiếu) - legacy entity
    @Override
    public List<Showtime> getByMovieId(Integer movieId) {
        return showtimeRepository.findByMovieIdAndStartTimeAfterOrderByStartTimeAsc(
                movieId, LocalDateTime.now());
    }

    // 2. Lấy suất chiếu kèm thông tin rạp/phòng (dùng cho trang Chi tiết Phim)
    @Override
    @Transactional(readOnly = true)
    public List<ShowtimeDTO> getShowtimeDTOsByMovieId(Integer movieId) {
        List<Showtime> showtimes = showtimeRepository
                .findByMovieIdAndStartTimeAfterOrderByStartTimeAsc(movieId, LocalDateTime.now());

        return showtimes.stream().map(s -> new ShowtimeDTO(
                s.getId(),
                s.getStartTime(),
                s.getPrice(),
                s.getRoom().getId(),
                s.getRoom().getName(),
                s.getRoom().getCinema().getId(),
                s.getRoom().getCinema().getName(),
                s.getRoom().getCinema().getAddress()
        )).collect(Collectors.toList());
    }

    // 3. Sơ đồ ghế: ghế nào đã đặt, ghế nào còn trống
    @Override
    @Transactional
    public List<SeatStatusDTO> getSeatsByShowtimeId(Integer showtimeId) {
        pendingBookingExpirationService.expireStalePendingBookings();

        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy suất chiếu ID: " + showtimeId));

        Integer roomId = showtime.getRoom().getId();
        List<Seat> allSeats = seatRepository.findByRoomId(roomId);
        if (allSeats.isEmpty()) {
            allSeats = seedDefaultSeats(showtime.getRoom());
        }

        List<Integer> bookedSeatIds = bookingDetailRepository.findBookedSeatIdsByShowtimeId(showtimeId);
        Set<Integer> bookedSet = Set.copyOf(bookedSeatIds);

        return allSeats.stream().map(seat -> {
            String seatType = seat.getSeatNumber().startsWith("V") ? "VIP" : "STANDARD";
            return new SeatStatusDTO(
                    seat.getId(),
                    seat.getSeatNumber(),
                    seatType,
                    bookedSet.contains(seat.getId())
            );
        }).collect(Collectors.toList());
    }

    // 4. Lấy chi tiết 1 suất chiếu
    @Override
    public Showtime getById(Integer id) {
        return showtimeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy suất chiếu ID: " + id));
    }

    // 5. Admin: Thêm suất chiếu mới
    @Override
    public Showtime createShowtime(Showtime showtime) {
        validateShowtime(showtime, null);
        return showtimeRepository.save(showtime);
    }

    // 6. Admin: Cập nhật suất chiếu
    @Override
    public Showtime updateShowtime(Integer id, Showtime details) {
        // 1. Tìm suất chiếu cũ trong DB
        Showtime existing = showtimeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy suất chiếu!"));

        // 2. Chỉ cập nhật những trường nào có gửi lên (Tránh null)
        if (details.getStartTime() != null)
            existing.setStartTime(details.getStartTime());
        if (details.getPrice() != null)
            existing.setPrice(details.getPrice());

        // Nếu có đổi phòng thì mới set phòng mới
        if (details.getRoom() != null && details.getRoom().getId() != null) {
            existing.setRoom(details.getRoom());
        }

        if (details.getMovie() != null && details.getMovie().getId() != null) {
            existing.setMovie(details.getMovie());
        }

        validateShowtime(existing, id);

        // 3. Lưu lại đối tượng đã cập nhật
        return showtimeRepository.save(existing);
    }

    // 7. Admin: Xóa suất chiếu
    @Override
    public void deleteShowtime(Integer id) {
        showtimeRepository.deleteById(id);
    }

    @Override
    public List<Showtime> getAllShowtimes() {
        return showtimeRepository.findAll();
    }

    @Override
    public List<Showtime> getShowtimesByMovieId(Integer movieId) {
        return showtimeRepository.findByMovieId(movieId);
    }

    private void validateShowtime(Showtime showtime, Integer currentShowtimeId) {
        Integer movieId = showtime.getMovie() != null ? showtime.getMovie().getId() : null;
        Integer roomId = showtime.getRoom() != null ? showtime.getRoom().getId() : null;

        if (movieId == null) {
            throw new RuntimeException("Vui lòng chọn phim cho suất chiếu.");
        }
        if (roomId == null) {
            throw new RuntimeException("Vui lòng chọn phòng cho suất chiếu.");
        }
        if (showtime.getStartTime() == null) {
            throw new RuntimeException("Vui lòng chọn thời gian bắt đầu.");
        }
        Integer movieDuration = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phim đã chọn."))
                .getDuration();
        if (movieDuration == null || movieDuration <= 0) {
            throw new RuntimeException("Thời lượng phim không hợp lệ để tạo suất chiếu.");
        }
        roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng đã chọn."));

        LocalDateTime newStart = showtime.getStartTime();
        LocalDateTime newEnd = newStart.plusMinutes(movieDuration);

        List<Showtime> sameRoomShowtimes = showtimeRepository.findByRoomId(roomId);
        boolean hasOverlap = sameRoomShowtimes.stream()
                .filter(existing -> currentShowtimeId == null || !existing.getId().equals(currentShowtimeId))
                .anyMatch(existing -> {
                    if (existing.getMovie() == null || existing.getMovie().getDuration() == null) {
                        return false;
                    }
                    LocalDateTime existingStart = existing.getStartTime();
                    LocalDateTime existingEnd = existingStart.plusMinutes(existing.getMovie().getDuration());
                    return newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart);
                });

        if (hasOverlap) {
            throw new RuntimeException("Khung giờ chiếu bị trùng với suất chiếu khác trong cùng phòng.");
        }
    }

    private List<Seat> seedDefaultSeats(Room room) {
        char[] rows = {'A', 'B', 'C', 'D', 'E', 'F'};
        List<Seat> seats = new ArrayList<>();

        for (char row : rows) {
            for (int col = 1; col <= 8; col++) {
                Seat seat = new Seat();
                seat.setRoom(room);
                seat.setSeatNumber(row + String.valueOf(col));
                seats.add(seat);
            }
        }

        return seatRepository.saveAll(seats);
    }
}
