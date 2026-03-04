package com.project.hanspoon.common.banner.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BannerDto {
    private Long bannerId;
    private String eyebrow;
    private String title;
    private String period;
    private String imageSrc;
    private String imageAlt;
    private String bg;
    private String toPath;
    private String href;

    @Builder.Default
    private List<BannerBadgeDto> badges = new ArrayList<>();

    @Builder.Default
    private Integer sortOrder = 0;

    @Builder.Default
    private Boolean isActive = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
