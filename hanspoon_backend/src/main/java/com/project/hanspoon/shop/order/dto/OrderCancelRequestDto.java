package com.project.hanspoon.shop.order.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCancelRequestDto {
    // PAID -> REFUNDED 일 때 저장할 사유 (CREATED 취소면 무시 가능)
    private String reason;
}
