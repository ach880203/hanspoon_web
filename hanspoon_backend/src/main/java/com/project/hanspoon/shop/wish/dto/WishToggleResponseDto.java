package com.project.hanspoon.shop.wish.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishToggleResponseDto {
    private Long productId;
    private boolean wished; // true=찜됨, false=찜해제됨
}
