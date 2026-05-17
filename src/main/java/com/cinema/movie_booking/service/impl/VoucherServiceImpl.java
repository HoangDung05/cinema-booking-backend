package com.cinema.movie_booking.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cinema.movie_booking.entity.Voucher;
import com.cinema.movie_booking.repository.VoucherRepository;
import com.cinema.movie_booking.service.VoucherService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;

    @Override
    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }

    @Override
    public Voucher createVoucher(Voucher voucher) {
        return voucherRepository.save(voucher);
    }

    @Override
    public Voucher updateVoucher(Integer id, Voucher voucher) {
        Voucher existingVoucher = getVoucherById(id);
        existingVoucher.setCode(voucher.getCode());
        existingVoucher.setDescription(voucher.getDescription());
        existingVoucher.setDiscountType(voucher.getDiscountType());
        existingVoucher.setDiscountValue(voucher.getDiscountValue());
        existingVoucher.setMinOrderValue(voucher.getMinOrderValue());
        existingVoucher.setMaxDiscountAmount(voucher.getMaxDiscountAmount());
        existingVoucher.setStartDate(voucher.getStartDate());
        existingVoucher.setEndDate(voucher.getEndDate());
        existingVoucher.setUsageLimit(voucher.getUsageLimit());
        existingVoucher.setUsedCount(voucher.getUsedCount());
        existingVoucher.setStatus(voucher.getStatus());
        return voucherRepository.save(existingVoucher);
    }
    @Override
    public Voucher getVoucherById(Integer id) {
        return voucherRepository.findById(id).orElseThrow(() -> new RuntimeException("Voucher not found"));
    }
    @Override
    public void deleteVoucher(Integer id) {
        voucherRepository.deleteById(id);
    }

    @Override
    public Optional<Voucher> getByCode(String code) {
        return voucherRepository.findByCodeIgnoreCase(code);
    }

    @Override
    public List<Voucher> getActiveVouchers() {
        return voucherRepository.findActiveVouchers();
    }

}