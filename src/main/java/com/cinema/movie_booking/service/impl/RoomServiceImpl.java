package com.cinema.movie_booking.service.impl;

import com.cinema.movie_booking.entity.Room;
import com.cinema.movie_booking.entity.Seat;
import com.cinema.movie_booking.repository.RoomRepository;
import com.cinema.movie_booking.repository.CinemaRepository;
import com.cinema.movie_booking.repository.SeatRepository;
import com.cinema.movie_booking.service.RoomService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final CinemaRepository cinemaRepository;
    private final SeatRepository seatRepository;

    public RoomServiceImpl(RoomRepository roomRepository, CinemaRepository cinemaRepository, SeatRepository seatRepository) {
        this.roomRepository = roomRepository;
        this.cinemaRepository = cinemaRepository;
        this.seatRepository = seatRepository;
    }

    @Override
    public Room getRoomById(Integer id) {
        return roomRepository.findByIdWithCinema(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Phòng chiếu với ID: " + id));
    }

    @Override
    public List<Room> getAllRooms() {
        return roomRepository.findAllWithCinema();
    }

    @Override
    public List<Room> getRoomsByCinema(Integer cinemaId) {
        return roomRepository.findByCinemaId(cinemaId);
    }

    @Override
    public Room createRoom(Integer cinemaId, Room room) {
        var cinema = cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Rạp phim ID: " + cinemaId));
        room.setCinema(cinema);
        Room savedRoom = roomRepository.save(room);
        seedDefaultSeats(savedRoom);
        return savedRoom;
    }

    @Override
    public Room updateRoom(Integer id, Room roomDetails) {
        // 1. Tìm phòng cũ trong DB
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng với ID: " + id));

        // 2. Cập nhật các trường thông tin
        room.setName(roomDetails.getName());

        if (roomDetails.getCinema() != null) {
            room.setCinema(roomDetails.getCinema());
        }

        // 3. Lưu lại
        return roomRepository.save(room);
    }

    @Override
    public void deleteRoom(Integer id) {
        if (!roomRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy phòng với ID: " + id + " để xóa!");
        }
        roomRepository.deleteById(id);
    }

    @Override
    public Room updateRoomAdmin(Integer id, String name, Integer cinemaId) {
        Room room = getRoomById(id);
        room.setName(name);
        var cinema = cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy rạp với ID: " + cinemaId));
        room.setCinema(cinema);
        return roomRepository.save(room);
    }

    private void seedDefaultSeats(Room room) {
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

        seatRepository.saveAll(seats);
    }
}