package com.project.hanspoon.shop.order.dto;

import com.project.hanspoon.shop.order.entity.OrderItem;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponseDto {
    private Long orderItemId;
    private Long productId;
    private String productName;
    private int orderPrice;
    private int quantity;
    private int lineTotal;
    private String thumbnailUrl;

    public static OrderItemResponseDto fromEntity(OrderItem item) {
        return OrderItemResponseDto.builder()
                .orderItemId(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .orderPrice(item.getOrderPrice())
                .quantity(item.getQuantity())
                .lineTotal(item.getLineTotal())
                .thumbnailUrl(item.getThumbnailUrl())
                .build();
    }
}

