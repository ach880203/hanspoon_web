package com.project.hanspoon.shop.wish.entity;

import com.project.hanspoon.common.user.entity.User;
import com.project.hanspoon.shop.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "wish_product",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_wish_user_product", columnNames = {"user_id", "product_id"})
        },
        indexes = {
                @Index(name = "idx_wish_user", columnList = "user_id"),
                @Index(name = "idx_wish_product", columnList = "product_id")
        }
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wish_id")
    private Long id;

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

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
