package com.project.hanspoon.shop.constant;

public enum OrderStatus {
    CREATED("주문생성"),
    PAID("결제완료"),
    SHIPPED("배송중"),
    DELIVERED("배송완료"),
    CONFIRMED("구매확정"),
    CANCELED("주문취소"),
    REFUND_REQUESTED("환불요청"),
    REFUNDED("환불완료");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
