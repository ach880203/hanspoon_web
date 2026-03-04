package com.project.hanspoon.oneday.reservation.entity;

import com.project.hanspoon.common.entity.BaseTimeEntity;
import com.project.hanspoon.common.user.entity.User;
import com.project.hanspoon.oneday.clazz.entity.ClassSession;
import com.project.hanspoon.oneday.reservation.domain.ReservationStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "class_reservation",
                indexes = {
                        @Index(name = "idx_reservation_session", columnList = "session_id"),
                        @Index(name = "idx_reservation_user", columnList = "member_id"),
                        @Index(name = "idx_reservation_status", columnList = "status")
                })
public class ClassReservation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", foreignKey = @ForeignKey(name = "fk_reservation_session"))
    private ClassSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private User user;

    // Legacy DB compatibility: some schemas still keep NOT NULL user_id.
    @Column(name = "user_id", nullable = false)
    private Long legacyUserId;

    // Legacy DB compatibility: old schema has additional NOT NULL updatedat column.
    @Column(name = "updatedat", nullable = false)
    private LocalDateTime legacyUpdatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(name = "hold_expired_at", nullable = false)
    private LocalDateTime holdExpiredAt;

    private LocalDateTime paidAt;
    private LocalDateTime cancelRequestedAt;
    private LocalDateTime canceledAt;
    @Column(length = 500)
    private String cancelReason;
    private Long linkedPayId;

    @Builder
    private ClassReservation(ClassSession session, User user,
                                                     ReservationStatus status, LocalDateTime holdExpiredAt){
        this.session =session;
        this.user = user;
        this.legacyUserId = (user != null ? user.getUserId() : null);
        this.status =status;
        this.holdExpiredAt = holdExpiredAt;
    }

    @PrePersist
    @PreUpdate
    private void syncLegacyColumns() {
        if (this.user != null) {
            this.legacyUserId = this.user.getUserId();
        }
        // BaseTimeEntity manages updated_at, but legacy DB also requires updatedat.
        // Keep both columns aligned to avoid NOT NULL insert/update failures.
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
        this.legacyUpdatedAt = this.updatedAt;
    }

    public boolean isExpired(LocalDateTime now) {
                return holdExpiredAt.isBefore(now);
    }

    public  void markPaid(LocalDateTime now){
        this.status = ReservationStatus.PAID;
        this.paidAt = now;
        this.cancelRequestedAt = null;
        this.cancelReason = null;
    }

    private LocalDateTime completedAt;

    public void markCancelRequested(LocalDateTime now, String reason) {
        this.status = ReservationStatus.CANCEL_REQUESTED;
        this.cancelRequestedAt = now;
        this.cancelReason = reason;
    }

    public void rejectCancelRequest() {
        this.status = ReservationStatus.PAID;
        this.cancelRequestedAt = null;
        this.cancelReason = null;
    }

    public void markCanceled(LocalDateTime now){
        this.status = ReservationStatus.CANCELED;
        this.canceledAt = now;
    }

    public void markExpired(LocalDateTime now) {
        this.status = ReservationStatus.EXPIRED;
        // 필요하면 expiredAt 같은 필드 추가 가능 (지금은 없어도 됨)
    }

    public  void markCompleted(LocalDateTime now) {
        this.status = ReservationStatus.COMPLETED;
        this.completedAt = now;
    }

    public  LocalDateTime getCompletedAt() {
        return  completedAt;
    }

    public void linkPayment(Long payId) {
        this.linkedPayId = payId;
    }

}
