package com.project.hanspoon.shop.review.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponseDto {
    private Long revId;
    private Long productId;
    private Long userId;

    private String content;
    private Integer rating;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
