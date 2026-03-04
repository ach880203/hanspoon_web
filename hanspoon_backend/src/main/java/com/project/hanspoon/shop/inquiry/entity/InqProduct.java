package com.project.hanspoon.shop.inquiry.entity;

import com.project.hanspoon.common.user.entity.User;
import com.project.hanspoon.shop.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "inq_product",
        indexes = {
                @Index(name = "idx_inq_user", columnList = "user_id"),
                @Index(name = "idx_inq_product", columnList = "product_id")
        }
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InqProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inq_id")
    private Long id;

    @Lob
    private String content;

    @Lob
    private String answer;

    @Column(name = "answered_yn")
    private Boolean answeredYn;

    private Boolean secret;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false) // ← 상품 PK 컬럼명에 맞추기
    @ToString.Exclude
    private Product product;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime answeredAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.answeredYn == null) this.answeredYn = false;
        if (secret == null) secret = false;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


}
