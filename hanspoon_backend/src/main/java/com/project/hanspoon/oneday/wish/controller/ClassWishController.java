package com.project.hanspoon.oneday.wish.controller;

import com.project.hanspoon.common.exception.BusinessException;
import com.project.hanspoon.common.response.ApiResponse;
import com.project.hanspoon.common.security.CustomUserDetails;
import com.project.hanspoon.oneday.wish.dto.WishItemResponse;
import com.project.hanspoon.oneday.wish.dto.WishToggleResponse;
import com.project.hanspoon.oneday.wish.service.ClassWishService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/oneday/wishes")
public class ClassWishController {

    private final ClassWishService classWishService;

    @PostMapping("/toggle")
    public ApiResponse<WishToggleResponse> toggle(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long classProductId
    ) {
        Long userId = resolveUserId(userDetails);
        return ApiResponse.ok("찜 상태가 변경되었습니다.", classWishService.toggle(userId, classProductId));
    }

    @GetMapping
    public ApiResponse<List<WishItemResponse>> list(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        return ApiResponse.ok(classWishService.list(userId));
    }

    private Long resolveUserId(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getUserId() == null) {
            throw new BusinessException("로그인 정보가 필요합니다.");
        }
        return userDetails.getUserId();
    }
}
