package com.project.hanspoon.shop.order.dto;

import com.project.hanspoon.shop.constant.OrderStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderListItemDto {

    private Long orderId;
    private OrderStatus status;
    private int totalPrice;
    private LocalDateTime createdAt;
    private String receiverName;
    private String trackingNumber;

    // 목록에서 보여주기 좋은 요약 정보
    private int itemCount;
    private String firstItemName;
    private String firstItemThumbnailUrl;
}
