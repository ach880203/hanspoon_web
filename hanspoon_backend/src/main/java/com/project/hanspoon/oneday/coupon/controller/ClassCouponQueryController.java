package com.project.hanspoon.oneday.coupon.controller;

import com.project.hanspoon.common.exception.BusinessException;
import com.project.hanspoon.common.response.ApiResponse;
import com.project.hanspoon.common.security.CustomUserDetails;
import com.project.hanspoon.oneday.coupon.dto.ClassUserCouponResponse;
import com.project.hanspoon.oneday.coupon.service.ClassCouponQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oneday/coupons")
public class ClassCouponQueryController {

    private final ClassCouponQueryService queryService;

    @GetMapping("/me")
    public ApiResponse<List<ClassUserCouponResponse>> myCoupons(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.ok(queryService.myCoupons(resolveUserId(userDetails)));
    }

    private Long resolveUserId(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getUserId() == null) {
            throw new BusinessException("로그인 정보가 필요합니다.");
        }
        return userDetails.getUserId();
    }
}
