package com.project.hanspoon.common.payment.constant;

/**
 * 결제 상태 enum
 */
public enum PaymentStatus {
    PENDING("결제대기"),
    PAID("결제완료"),
    CANCELLED("결제취소"),
    FAILED("결제실패"),
    REFUNDED("환불완료");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
