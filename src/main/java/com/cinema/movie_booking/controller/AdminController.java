
package com.cinema.movie_booking.controller;

import com.cinema.movie_booking.entity.Cinema;
import com.cinema.movie_booking.entity.Movie;
import com.cinema.movie_booking.entity.Room;
import com.cinema.movie_booking.entity.Showtime;
import com.cinema.movie_booking.entity.User;
import com.cinema.movie_booking.entity.Voucher;
import com.cinema.movie_booking.entity.Booking;
import com.cinema.movie_booking.service.BookingService;
import com.cinema.movie_booking.service.CinemaService;
import com.cinema.movie_booking.service.MovieService;
import com.cinema.movie_booking.service.RoomService;
import com.cinema.movie_booking.service.ShowtimeService;
import com.cinema.movie_booking.service.UserService;
import lombok.RequiredArgsConstructor;
import com.cinema.movie_booking.service.VoucherService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor

public class AdminController {

    private final BookingService bookingService;
    private final UserService userService;
    private final ShowtimeService showtimeService;
    private final MovieService movieService;
    private final CinemaService cinemaService;
    private final VoucherService voucherService;
    private final RoomService roomService;
    // 1. API Lấy số liệu Dashboard
    // GET http://localhost:8080/api/admin/stats
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        return ResponseEntity.ok(bookingService.getAdminStats());
    }

    // 2. API Xem toàn bộ lịch sử đặt vé hệ thống
    // GET http://localhost:8080/api/admin/bookings
    @GetMapping("/bookings")
    public ResponseEntity<?> getAllBookings() {
        try {
            List<Booking> bookings = bookingService.getAllBookingsForAdmin();
            
            // Transform Booking -> AdminBookingDTO
            java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
            
            for (Booking b : bookings) {
                java.util.Map<String, Object> dto = new java.util.HashMap<>();
                dto.put("bookingId", b.getId());
                dto.put("userEmail", b.getUser() != null ? b.getUser().getEmail() : "—");
                dto.put("userName", b.getUser() != null ? b.getUser().getFullName() : "—");
                dto.put("movieTitle", b.getShowtime() != null && b.getShowtime().getMovie() != null 
                    ? b.getShowtime().getMovie().getTitle() : "—");
                dto.put("showtimeStart", b.getShowtime() != null ? b.getShowtime().getStartTime() : null);
                
                // Tập hợp tên ghế (A1, A2, B1, ...)
                String seatNames = b.getBookingDetails().stream()
                    .map(detail -> detail.getSeat().getSeatNumber())
                    .collect(java.util.stream.Collectors.joining(", "));
                dto.put("seatNames", seatNames);
                
                dto.put("totalPrice", b.getTotalPrice());
                dto.put("status", b.getStatus());
                dto.put("createdAt", b.getCreatedAt());
                
                result.add(dto);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi lấy danh sách bookings: " + e.getMessage());
        }
    }

    // 3. API Quản lý danh sách khách hàng (như đã làm ở bước trước)

    @GetMapping("/customers")
    public ResponseEntity<?> getCustomers() {
        return ResponseEntity.ok(userService.getAllCustomers());
    }

    // 3.1. Lấy tất cả user
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    // 3.2. Cập nhật thông tin user

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable Integer id, @RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    // 3.3. Xóa user
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("Đã xóa khách hàng ID: " + id);
    }

    // 3.4. Đổi quyền (CUSTOMER <-> ADMIN)
    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> changeUserRole(@PathVariable Integer id, @RequestParam String role) {
        return ResponseEntity.ok(userService.updateUserRole(id, role));
    }

    // 3.5. Khóa tài khoản
    @PutMapping("/user/{id}/status")
    public ResponseEntity<?> changeStatus(
            @PathVariable Integer id,
            @RequestParam String status) {
        try {
            User updatedUser = userService.updateUserStatus(id, status);
            return ResponseEntity.ok(
                    "Người dùng " + updatedUser.getFullName() + " hiện đang ở trạng thái: " + updatedUser.getStatus());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 4. Showtimes
    // 4.1 thêm
    @PostMapping("/showtimes")
    public ResponseEntity<?> createShowtime(@RequestBody Showtime showtime) {
        try {
            Showtime created = showtimeService.createShowtime(showtime);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // Trả về lỗi nếu trùng lịch chiếu
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    // 4.3 sửa
    @PutMapping("/showtimes/{id}")
    public ResponseEntity<?> updateShowtime(@PathVariable Integer id, @RequestBody Showtime showtime) {
        try {
            return ResponseEntity.ok(showtimeService.updateShowtime(id, showtime));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 4.4 xóa
    @DeleteMapping("/showtimes/{id}")
    public ResponseEntity<?> deleteShowtimes(@PathVariable Integer id) {
        try {
            showtimeService.deleteShowtime(id);
            return ResponseEntity.ok("Xóa thành công suất chiếu ID: " + id);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Không thể xóa: " + e.getMessage());
        }
    }

    // 5.1. API THÊM PHIM MỚI (POST)

    @PostMapping("/movies")
    public ResponseEntity<Movie> createMovie(@RequestBody Movie movie) {
        Movie savedMovie = movieService.saveMovie(movie);
        return new ResponseEntity<>(savedMovie, HttpStatus.CREATED);
    }

    // 5.2. API CẬP NHẬT PHIM (PUT)

    @PutMapping("/movies/{id}")
    public ResponseEntity<Movie> updateMovie(@PathVariable Integer id, @RequestBody Movie movieDetails) {

        Movie existingMovie = movieService.getMovieById(id);

        existingMovie.setTitle(movieDetails.getTitle());
        existingMovie.setDescription(movieDetails.getDescription());
        existingMovie.setDuration(movieDetails.getDuration());
        existingMovie.setReleaseDate(movieDetails.getReleaseDate());
        existingMovie.setPosterUrl(movieDetails.getPosterUrl());
        existingMovie.setTrailerUrl(movieDetails.getTrailerUrl());
        existingMovie.setStatus(movieDetails.getStatus());

        Movie updatedMovie = movieService.saveMovie(existingMovie);
        return ResponseEntity.ok(updatedMovie);
    }

    // 5.3. API XÓA PHIM (DELETE)

    @DeleteMapping("/movies/{id}")
    public ResponseEntity<String> deleteMovie(@PathVariable Integer id) {
        movieService.deleteMovie(id);
        return ResponseEntity.ok("Xóa phim thành công!");
    }

    // 6 Room
    // 6.1. Tạo phòng mới cho 1 rạp
    @PostMapping("/rooms/cinema/{cinemaId}")
    public ResponseEntity<Room> createRoom(@PathVariable Integer cinemaId, @RequestBody Room room) {
        return ResponseEntity.ok(roomService.createRoom(cinemaId, room));
    }

    // 6.2 Xóa phòng
    @DeleteMapping("/rooms/{id}")
    public ResponseEntity<String> deleteRooms(@PathVariable Integer id) {
        roomService.deleteRoom(id);
        return ResponseEntity.ok("Đã xóa phòng thành công!");
    }

    // 6.3 Sửa room
    @PutMapping("/rooms/{id}")
    public ResponseEntity<?> updateRoom(@PathVariable Integer id, @RequestBody Room room) {
        try {
            Room updatedRoom = roomService.updateRoom(id, room);
            return ResponseEntity.ok(updatedRoom);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 7. Cinema
    // 7.1. Tạo mới rạp
    @PostMapping("/cinemas")
    public ResponseEntity<Cinema> createCinema(@RequestBody Cinema cinema) {
        return ResponseEntity.ok(cinemaService.createCinema(cinema));
    }

    // 7.2. Cập nhật thông tin rạp
    @PutMapping("/cinemas/{id}")
    public ResponseEntity<Cinema> updateCinema(@PathVariable Integer id, @RequestBody Cinema cinema) {
        return ResponseEntity.ok(cinemaService.updateCinema(id, cinema));
    }

    // 7.3. Xóa rạp
    @DeleteMapping("/cinemas/{id}")
    public ResponseEntity<String> deleteCinema(@PathVariable Integer id) {
        cinemaService.deleteCinema(id);
        return ResponseEntity.ok("Đã xóa rạp thành công!");
    }
    //8. voucher
    @GetMapping("/vouchers")
    public ResponseEntity<?> getAllVouchers() {
        return ResponseEntity.ok(voucherService.getAllVouchers());
    }
    //8.1. Thêm voucher
    @PostMapping("/vouchers")
    public ResponseEntity<Voucher> createVoucher(@RequestBody Voucher voucher) {
        return ResponseEntity.ok(voucherService.createVoucher(voucher));
    }
    //8.2. Put voucher
    @PutMapping("/vouchers/{id}")
    public ResponseEntity<Voucher> updateVoucher(@PathVariable Integer id, @RequestBody Voucher voucher) {
        return ResponseEntity.ok(voucherService.updateVoucher(id, voucher));
    }
    //8.3. Delete voucher
    @DeleteMapping("/vouchers/{id}")
    public ResponseEntity<String> deleteVoucher(@PathVariable Integer id) {
        voucherService.deleteVoucher(id);
        return ResponseEntity.ok("Đã xóa voucher thành công!");
    }

}