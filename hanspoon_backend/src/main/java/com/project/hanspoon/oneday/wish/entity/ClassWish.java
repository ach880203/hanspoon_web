package com.project.hanspoon.oneday.wish.entity;

import com.project.hanspoon.oneday.clazz.entity.ClassProduct;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "class_wish",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_wish_member_class", columnNames = {"user_id", "class_product_id"})
        },
        indexes = {
                @Index(name = "idx_wish_member", columnList = "user_id"),
                @Index(name = "idx_wish_class_product", columnList = "class_product_id")
        }
)
public class ClassWish {

                    @Id
                    @GeneratedValue(strategy = GenerationType.IDENTITY)
                    private Long id;

                    @Column(name = "user_id", nullable = false)
                    private Long userId;

                    @ManyToOne(fetch = FetchType.LAZY, optional = false)
                    @JoinColumn(name = "class_product_id", nullable = false)
                    private ClassProduct classProduct;

                    @Column(name = "created_at", nullable = false)
                    private LocalDateTime createdAt;

                    private  ClassWish(Long userId, ClassProduct classProduct){
                        this.userId = userId;
                        this.classProduct =classProduct;
                        this.createdAt = LocalDateTime.now();
                    }

                    public  static ClassWish of(Long userId, ClassProduct classProduct){
                        return new ClassWish(userId, classProduct);
                    }
}

