package com.cinema.movie_booking.service;

import java.util.List;
import java.util.Optional;

import com.cinema.movie_booking.entity.Voucher;

public interface VoucherService {
    List<Voucher> getAllVouchers();
    Voucher createVoucher(Voucher voucher);
    Voucher updateVoucher(Integer id, Voucher voucher);
    Voucher getVoucherById(Integer id);

    void deleteVoucher(Integer id);

    Optional<Voucher> getByCode(String code);

    List<Voucher> getActiveVouchers();

    List<com.cinema.movie_booking.entity.UserVoucher> getUserVouchers(String email);

    com.cinema.movie_booking.entity.UserVoucher claimVoucher(String email, String code);
}