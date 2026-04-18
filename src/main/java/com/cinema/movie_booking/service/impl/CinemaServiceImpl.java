package com.cinema.movie_booking.service.impl;

import com.cinema.movie_booking.entity.Cinema;
import com.cinema.movie_booking.repository.CinemaRepository;
import com.cinema.movie_booking.service.CinemaService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CinemaServiceImpl implements CinemaService {

    // Sử dụng private final để đảm bảo tính bất biến
    private final CinemaRepository cinemaRepository;

    // Tự viết Constructor để Spring thực hiện Dependency Injection (vì không dùng
    // Lombok)
    public CinemaServiceImpl(CinemaRepository cinemaRepository) {
        this.cinemaRepository = cinemaRepository;
    }

    @Override
    public List<Cinema> getAllCinemas() {
        return cinemaRepository.findAll();
    }

    @Override
    public Cinema getCinemaById(Integer id) {
        Optional<Cinema> cinema = cinemaRepository.findById(id);
        return cinema.orElseThrow(() -> new RuntimeException("Không tìm thấy rạp với ID: " + id));
    }

    @Override
    public Cinema createCinema(Cinema cinema) {
        return cinemaRepository.save(cinema);
    }

    @Override
    public Cinema updateCinema(Integer id, Cinema cinemaDetails) {
        Cinema cinema = getCinemaById(id);
        cinema.setName(cinemaDetails.getName());
        cinema.setAddress(cinemaDetails.getAddress());
        return cinemaRepository.save(cinema);
    }

    @Override
    public void deleteCinema(Integer id) {
        Cinema cinema = getCinemaById(id);
        cinemaRepository.delete(cinema);
    }
}