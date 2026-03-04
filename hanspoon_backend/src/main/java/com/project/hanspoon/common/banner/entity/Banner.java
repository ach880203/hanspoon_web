package com.project.hanspoon.common.banner.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "banner")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "banner_id")
    private Long bannerId;

    @Column(name = "eyebrow", length = 255)
    private String eyebrow;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "period", length = 100)
    private String period;

    @Column(name = "image_src", length = 500, nullable = false)
    private String imageSrc;

    @Column(name = "image_alt", length = 255)
    private String imageAlt;

    @Column(name = "bg", length = 50)
    private String bg;

    @Column(name = "link_to", length = 255)
    private String toPath;

    @Column(name = "link_href", length = 500)
    private String href;

    @Column(name = "badges_json", columnDefinition = "TEXT")
    private String badgesJson;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
