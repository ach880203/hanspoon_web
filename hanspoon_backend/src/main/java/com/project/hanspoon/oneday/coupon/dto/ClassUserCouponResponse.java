package com.project.hanspoon.oneday.coupon.dto;

import com.project.hanspoon.oneday.coupon.domain.DiscountType;

import java.time.LocalDateTime;

public record ClassUserCouponResponse(
        Long userCouponId,
        Long couponId,
        String name,
        DiscountType discountType,
        int discountValue,
        LocalDateTime issueAt,
        LocalDateTime expiresAt,
        boolean usable
) {
}
