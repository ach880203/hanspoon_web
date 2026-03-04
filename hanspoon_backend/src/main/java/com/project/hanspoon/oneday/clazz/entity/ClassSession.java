package com.project.hanspoon.oneday.clazz.entity;

import com.project.hanspoon.common.entity.BaseTimeEntity;
import com.project.hanspoon.oneday.clazz.domain.SessionSlot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "class_session")
public class ClassSession extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_product_id", foreignKey = @ForeignKey(name = "fk_session_product"))
    private ClassProduct classProduct;

    @Column(unique = false)
    private LocalDateTime startAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SessionSlot slot;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private int reservedCount;

    @Column(nullable = false)
    private int price;

    @Version
    private Long version;

    // Legacy DB compatibility: some schemas keep updatedat as NOT NULL.
    @Column(name = "updatedat", nullable = false)
    private LocalDateTime legacyUpdatedAt;

    @Builder
    public ClassSession(ClassProduct classProduct, LocalDateTime startAt, SessionSlot slot, int capacity, int price) {
        this.classProduct = classProduct;
        this.startAt = startAt;
        this.slot = slot;
        this.capacity = capacity;
        this.price = price;
        this.reservedCount = 0;
    }

    public int remainingSeats() {
        return capacity - reservedCount;
    }

    public void increaseReserved() {
        if (remainingSeats() <= 0) {
            throw new IllegalStateException("정원이 마감되었습니다.");
        }
        this.reservedCount += 1;
    }

    public void decreaseReserved() {
        if (this.reservedCount <= 0) {
            return;
        }
        this.reservedCount -= 1;
    }

    @PrePersist
    @PreUpdate
    private void syncLegacyUpdatedAt() {
        LocalDateTime now = LocalDateTime.now();
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
        this.legacyUpdatedAt = this.updatedAt;
    }
}
