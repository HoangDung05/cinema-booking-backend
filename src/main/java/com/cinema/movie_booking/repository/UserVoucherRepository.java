package com.cinema.movie_booking.repository;

import com.cinema.movie_booking.entity.User;
import com.cinema.movie_booking.entity.UserVoucher;
import com.cinema.movie_booking.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserVoucherRepository extends JpaRepository<UserVoucher, Integer> {
    List<UserVoucher> findByUser(User user);
    Optional<UserVoucher> findByUserAndVoucher(User user, Voucher voucher);
}
