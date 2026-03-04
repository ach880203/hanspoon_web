package com.project.hanspoon.oneday.inquiry.entity;

import com.project.hanspoon.common.entity.BaseTimeEntity;
import com.project.hanspoon.oneday.clazz.entity.ClassProduct;
import com.project.hanspoon.oneday.inquiry.domain.InquiryStatus;
import com.project.hanspoon.oneday.inquiry.domain.Visibility;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "class_inquiry",
        indexes = {
                @Index(name = "idx_class_inquiry_class", columnList = "class_product_id"),
                @Index(name = "idx_class_inquiry_user", columnList = "user_id"),
                @Index(name = "idx_class_inquiry_created", columnList = "createdAt")
        }
)
public class ClassInquiry extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_product_id", nullable = false)
    private ClassProduct classProduct;

    // Legacy DB compatibility: old schema keeps a separate class_id column (NOT NULL).
    @Column(name = "class_id", nullable = false)
    private Long legacyClassId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 4000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Visibility visibility;

    // Legacy DB compatibility: old schema uses secret bit(1) column.
    @Column(name = "secret", nullable = false)
    private boolean legacySecret;

    // Legacy DB compatibility: old schema requires explicit inquiry status enum.
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InquiryStatus status;

    @Column(name = "has_attachment", nullable = false)
    private boolean hasAttachment;

    // Legacy DB compatibility: old schema has answered tinyint(1) NOT NULL.
    @Column(name = "answered", nullable = false)
    private boolean answered;

    @Column(name = "answer_content", length = 4000)
    private String answerContent;

    @Column(name = "answered_by_user_id")
    private Long answeredByUserId;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    private ClassInquiry(
            ClassProduct classProduct,
            Long userId,
            String category,
            String title,
            String content,
            Visibility visibility,
            boolean hasAttachment
    ) {
        this.classProduct = classProduct;
        this.userId = userId;
        this.category = category;
        this.title = title;
        this.content = content;
        this.visibility = visibility;
        this.legacySecret = (visibility == Visibility.PRIVATE);
        this.status = InquiryStatus.OPEN;
        this.hasAttachment = hasAttachment;
        this.answered = false;
        this.legacyClassId = (classProduct != null ? classProduct.getId() : null);
    }

    public static ClassInquiry create(
            ClassProduct classProduct,
            Long userId,
            String category,
            String title,
            String content,
            Visibility visibility,
            boolean hasAttachment
    ) {
        return new ClassInquiry(classProduct, userId, category, title, content, visibility, hasAttachment);
    }

    public boolean isAnswered() {
        return answered;
    }

    // 문의 원문 아래에 달리는 답글(대댓글) 저장 메서드입니다.
    public void answer(String answerContent, Long answeredByUserId, LocalDateTime answeredAt) {
        this.answerContent = answerContent;
        this.answeredByUserId = answeredByUserId;
        this.answeredAt = answeredAt;
        this.answered = true;
        this.status = InquiryStatus.ANSWERED;
    }

    @PrePersist
    @PreUpdate
    private void syncLegacyColumns() {
        // Keep legacy class_id synchronized with the relation column class_product_id.
        if (this.classProduct != null) {
            this.legacyClassId = this.classProduct.getId();
        }

        // Keep legacy secret flag synchronized with the new visibility enum.
        this.legacySecret = (this.visibility == Visibility.PRIVATE);

        // Guarantee NOT NULL legacy status/answered columns even for old rows/objects.
        if (this.answerContent != null && !this.answerContent.isBlank()) {
            this.answered = true;
        }
        if (this.status == null) {
            this.status = this.answered ? InquiryStatus.ANSWERED : InquiryStatus.OPEN;
        }
    }
}
