package com.project.hanspoon.oneday.clazz.entity;

import com.project.hanspoon.common.entity.BaseTimeEntity;
import com.project.hanspoon.oneday.clazz.domain.Level;
import com.project.hanspoon.oneday.clazz.domain.RecipeCategory;
import com.project.hanspoon.oneday.clazz.domain.RunType;
import com.project.hanspoon.oneday.instructor.entity.Instructor;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "class_product")
public class ClassProduct extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String detailDescription;

    // Base64 Data URL 형식(예: data:image/png;base64,...)을 저장하는 상세 이미지 필드
    @Column(columnDefinition = "LONGTEXT")
    private String detailImageData;

    @OneToMany(mappedBy = "classProduct", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, id ASC")
    private List<ClassDetailImage> detailImages = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Level level;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RunType runType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecipeCategory category;

    @Column(length = 255)
    private String locationAddress;

    private Double locationLat;

    private Double locationLng;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instructor_id", foreignKey =
                    @ForeignKey(name = "fk_class_product_instructor"))
    private Instructor instructor;

    @OneToMany(mappedBy = "classProduct", cascade = CascadeType.ALL
                                , orphanRemoval = true)
    private List<ClassSession> session = new ArrayList<>();

    // Legacy DB compatibility: some schemas keep updatedat as NOT NULL.
    @Column(name = "updatedat", nullable = false)
    private LocalDateTime legacyUpdatedAt;

    @Builder
    public ClassProduct(String title, String description, String detailDescription, String detailImageData, Level level,
                        RunType runType, RecipeCategory category,
                        String locationAddress, Double locationLat, Double locationLng,
                        Instructor instructor){
        this.title = title;
        this.description = description;
        this.detailDescription = detailDescription;
        this.detailImageData = detailImageData;
        this.level = level;
        this.runType = runType;
        this.category = category;
        this.locationAddress = locationAddress;
        this.locationLat = locationLat;
        this.locationLng = locationLng;
        this.instructor = instructor;

    }

    public void updateInfo(
            String title,
            String description,
            String detailDescription,
            String detailImageData,
            Level level,
            RunType runType,
            RecipeCategory category,
            String locationAddress,
            Double locationLat,
            Double locationLng,
            Instructor instructor
    ) {
        this.title = title;
        this.description = description;
        this.detailDescription = detailDescription;
        this.detailImageData = detailImageData;
        this.level = level;
        this.runType = runType;
        this.category = category;
        this.locationAddress = locationAddress;
        this.locationLat = locationLat;
        this.locationLng = locationLng;
        this.instructor = instructor;
    }

    public void replaceDetailImages(List<String> imageDataList) {
        this.detailImages.clear();
        if (imageDataList == null || imageDataList.isEmpty()) {
            return;
        }

        int order = 0;
        for (String imageData : imageDataList) {
            if (imageData == null || imageData.isBlank()) continue;
            this.detailImages.add(ClassDetailImage.of(this, order, imageData));
            order += 1;
        }
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
