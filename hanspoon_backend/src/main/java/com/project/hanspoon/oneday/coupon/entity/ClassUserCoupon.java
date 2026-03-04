package com.project.hanspoon.oneday.coupon.entity;

import com.project.hanspoon.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "class_user_coupon", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_coupon_reservation", columnNames = { "reservation_id" })
}, indexes = {
        @Index(name = "idx_user_coupon_user", columnList = "user_id"),
        @Index(name = "idx_user_coupon_expires", columnList = "expires_at")
})
public class ClassUserCoupon extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId; // 유저 도메인 안 건드림

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coupon_id", nullable = false)
    private ClassCoupon coupon;

    @Column(name = "reservation_id")
    private Long reservationId; // 어떤 수강완료로 발급됐는지 추적

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    private ClassUserCoupon(Long userId, ClassCoupon coupon, Long reservationId, LocalDateTime issuedAt,
            LocalDateTime expiresAt) {
        this.userId = userId;
        this.coupon = coupon;
        this.reservationId = reservationId;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }

    public static ClassUserCoupon issue(Long userId, ClassCoupon coupon, Long reservationId, LocalDateTime now) {
        return new ClassUserCoupon(
                userId,
                coupon,
                reservationId,
                now,
                now.plusDays(coupon.getValidDays()));
    }

    public static ClassUserCoupon issueForMonths(Long userId, ClassCoupon coupon, Long reservationId, LocalDateTime now, int months) {
        return new ClassUserCoupon(
                userId,
                coupon,
                reservationId,
                now,
                now.plusMonths(months));
    }

    public boolean isUsable(LocalDateTime now) {
        return usedAt == null && expiresAt.isAfter(now);
    }

    public void markUsed(LocalDateTime now) {
        this.usedAt = now;
    }
}
