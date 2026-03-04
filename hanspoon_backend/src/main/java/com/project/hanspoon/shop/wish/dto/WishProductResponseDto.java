package com.project.hanspoon.shop.wish.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishProductResponseDto {

    private Long wishId;
    private Long productId;

    private String name;
    private int price;
    private String category;     // 있으면
    private String thumbnailUrl; // 대표 이미지

    private LocalDateTime createdAt;
}
