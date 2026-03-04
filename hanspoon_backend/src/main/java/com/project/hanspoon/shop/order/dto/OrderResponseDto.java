package com.project.hanspoon.shop.order.dto;

import com.project.hanspoon.shop.constant.OrderStatus;
import com.project.hanspoon.shop.order.entity.Order;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDto {
    private Long id;
    private Long orderId;
    private Long cartId;
    private OrderStatus status;
    private int totalPrice;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime confirmedAt;
    private String trackingNumber;
    private LocalDateTime refundedAt;
    private String refundReason;

    private String receiverName;
    private String receiverPhone;
    private String address1;
    private String address2;

    private List<OrderItemResponseDto> items;

    public static OrderResponseDto fromEntity(Order order) {
        return OrderResponseDto.builder()
                .id(order.getId())
                .orderId(order.getId())
                .cartId(order.getCartId())
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .createdAt(order.getCreatedAt())
                .paidAt(order.getPaidAt())
                .shippedAt(order.getShippedAt())
                .deliveredAt(order.getDeliveredAt())
                .confirmedAt(order.getConfirmedAt())
                .trackingNumber(order.getTrackingNumber())
                .refundedAt(order.getRefundedAt())
                .refundReason(order.getRefundReason())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .address1(order.getAddress1())
                .address2(order.getAddress2())
                .items(order.getItems() != null ? order.getItems().stream()
                        .map(OrderItemResponseDto::fromEntity)
                        .toList() : null)
                .build();
    }
}
