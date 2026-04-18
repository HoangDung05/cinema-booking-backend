package com.cinema.movie_booking.service;

import com.cinema.movie_booking.entity.Room;
import java.util.List;

public interface RoomService {
    List<Room> getAllRooms();

    List<Room> getRoomsByCinema(Integer cinemaId);

    Room createRoom(Integer cinemaId, Room room);

    void deleteRoom(Integer id);

    Room getRoomById(Integer id);

    Room updateRoom(Integer id, Room roomDetails);

    Room updateRoomAdmin(Integer id, String name, Integer cinemaId);
}