package com.cinema.movie_booking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_vouchers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVoucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Ánh xạ cột user_id (Khóa ngoại trỏ tới bảng users)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User user;

    // Ánh xạ cột voucher_id (Khóa ngoại trỏ tới bảng vouchers)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Voucher voucher;

    // Ánh xạ cột is_used
    @Column(name = "is_used")
    private Boolean isUsed = false;

    // Ánh xạ cột assigned_at
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    // Ánh xạ cột used_at
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    // Gợi ý: Bạn có thể thêm hàm tự động gán ngày giờ khi tạo mới ví voucher
    @PrePersist
    protected void onCreate() {
        this.assignedAt = LocalDateTime.now();
    }
}