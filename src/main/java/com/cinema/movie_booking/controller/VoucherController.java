package com.cinema.movie_booking.controller;

import com.cinema.movie_booking.entity.Voucher;
import com.cinema.movie_booking.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    /** Admin: toàn bộ voucher */
    @GetMapping("/all")
    public ResponseEntity<List<Voucher>> getAllVouchers() {
        return ResponseEntity.ok(voucherService.getAllVouchers());
    }

    /** Khách: voucher đang hiệu lực */
    @GetMapping
    public ResponseEntity<?> getActiveVouchers() {
        return ResponseEntity.ok(voucherService.getActiveVouchers());
    }

    @PostMapping
    public ResponseEntity<?> createVoucher(@RequestBody Voucher voucher) {
        try {
            voucher.setId(null);
            if (voucher.getUsedCount() == null) {
                voucher.setUsedCount(0);
            }
            if (voucher.getStatus() == null || voucher.getStatus().isBlank()) {
                voucher.setStatus("ACTIVE");
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(voucherService.createVoucher(voucher));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateVoucher(@PathVariable Integer id, @RequestBody Voucher voucher) {
        try {
            return ResponseEntity.ok(voucherService.updateVoucher(id, voucher));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVoucher(@PathVariable Integer id) {
        voucherService.deleteVoucher(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/claim")
    public ResponseEntity<?> claimVoucher(@RequestBody Map<String, String> payload) {
        try {
            String email = payload.get("email");
            String code = payload.get("code");

            if (email == null || code == null) {
                return ResponseEntity.badRequest().body("Thiều thông tin email hoặc code");
            }

            return ResponseEntity.ok(voucherService.claimVoucher(email, code));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}