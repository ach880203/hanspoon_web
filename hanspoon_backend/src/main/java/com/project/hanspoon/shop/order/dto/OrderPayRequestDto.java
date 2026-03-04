package com.project.hanspoon.shop.order.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPayRequestDto {
    // 예: CARD, BANK, KAKAO 등 (필수 아님)
    private String payMethod;
}

