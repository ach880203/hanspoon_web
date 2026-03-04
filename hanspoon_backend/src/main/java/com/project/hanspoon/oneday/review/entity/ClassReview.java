package com.project.hanspoon.oneday.review.entity;

import com.project.hanspoon.common.entity.BaseTimeEntity;
import com.project.hanspoon.oneday.clazz.entity.ClassProduct;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "class_review",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_review_reservation", columnNames = {"reservation_id"})
        },
        indexes = {
                @Index(name = "idx_review_class", columnList = "class_product_id"),
                @Index(name = "idx_review_user", columnList = "user_id"),
                @Index(name = "idx_review_del_flag", columnList = "del_flag")
        }
)
public class ClassReview extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_product_id", nullable = false)
    private ClassProduct classProduct;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @Column(nullable = false)
    private int rating;

    @Column(nullable = false, length = 2000)
    private String content;

    // 관리자 답글 본문입니다.
    // null이면 아직 답글이 없는 상태로 해석합니다.
    @Column(name = "answer_content", length = 2000)
    private String answerContent;

    // 답글을 등록한 사용자 ID입니다. (관리자 계정)
    @Column(name = "answered_by_user_id")
    private Long answeredByUserId;

    // 답글 등록 시각입니다.
    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    // delFlag는 소프트 삭제 여부를 저장합니다.
    // true로 바꿔도 실제 행은 남기 때문에 삭제 이력 분석이 가능합니다.
    @Column(name = "del_flag", nullable = false, columnDefinition = "tinyint(1) default 0")
    private boolean delFlag;

    // 삭제 시각을 함께 저장해 "언제 삭제했는지"를 데이터로 남깁니다.
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Legacy DB compatibility: old schema has additional NOT NULL updatedat column.
    @Column(name = "updatedat", nullable = false)
    private LocalDateTime legacyUpdatedAt;

    private ClassReview(ClassProduct classProduct, Long userId, Long reservationId, int rating, String content) {
        this.classProduct = classProduct;
        this.userId = userId;
        this.reservationId = reservationId;
        this.rating = rating;
        this.content = content;
        this.delFlag = false;
    }

    public static ClassReview of(ClassProduct classProduct, Long userId, Long reservationId, int rating, String content) {
        return new ClassReview(classProduct, userId, reservationId, rating, content);
    }

    public void markDeleted(LocalDateTime now) {
        this.delFlag = true;
        this.deletedAt = now;
    }

    public void reactivate(ClassProduct classProduct, Long userId, int rating, String content) {
        this.classProduct = classProduct;
        this.userId = userId;
        this.rating = rating;
        this.content = content;
        this.delFlag = false;
        this.deletedAt = null;
        this.answerContent = null;
        this.answeredByUserId = null;
        this.answeredAt = null;
    }

    // 리뷰 원문 아래로 달리는 관리자 답글(대댓글)을 저장합니다.
    public void answer(String answerContent, Long answeredByUserId, LocalDateTime answeredAt) {
        this.answerContent = answerContent;
        this.answeredByUserId = answeredByUserId;
        this.answeredAt = answeredAt;
    }

    @PrePersist
    @PreUpdate
    private void syncLegacyColumns() {
        // BaseTimeEntity manages updated_at, but legacy DB also requires updatedat.
        // Keep both columns aligned to avoid NOT NULL insert/update failures.
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
        this.legacyUpdatedAt = this.updatedAt;
    }
}

