package com.project.hanspoon.oneday.clazz.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "class_detail_image")
public class ClassDetailImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_product_id", nullable = false)
    private ClassProduct classProduct;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(columnDefinition = "LONGTEXT")
    private String imageData;

    private ClassDetailImage(ClassProduct classProduct, int sortOrder, String imageData) {
        this.classProduct = classProduct;
        this.sortOrder = sortOrder;
        this.imageData = imageData;
    }

    public static ClassDetailImage of(ClassProduct classProduct, int sortOrder, String imageData) {
        return new ClassDetailImage(classProduct, sortOrder, imageData);
    }
}

