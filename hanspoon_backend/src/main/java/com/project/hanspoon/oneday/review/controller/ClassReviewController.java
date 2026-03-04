package com.project.hanspoon.oneday.review.controller;

import com.project.hanspoon.common.exception.BusinessException;
import com.project.hanspoon.common.response.ApiResponse;
import com.project.hanspoon.common.security.CustomUserDetails;
import com.project.hanspoon.oneday.review.dto.ClassReviewAnswerRequest;
import com.project.hanspoon.oneday.review.dto.ClassReviewCreateRequest;
import com.project.hanspoon.oneday.review.dto.ClassReviewResponse;
import com.project.hanspoon.oneday.review.service.ClassReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oneday/reviews")
public class ClassReviewController {

    private final ClassReviewService reviewService;

    @PostMapping
    public ApiResponse<ClassReviewResponse> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ClassReviewCreateRequest req
    ) {
        Long userId = resolveUserId(userDetails);
        return ApiResponse.ok("리뷰가 등록되었습니다.", reviewService.create(userId, req));
    }

    // 리뷰 전문 강사/관리자 답글 등록 API입니다.
    @PostMapping("/{reviewId}/answer")
    public ApiResponse<ClassReviewResponse> answer(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reviewId,
            @RequestBody ClassReviewAnswerRequest req
    ) {
        Long userId = resolveUserId(userDetails);
        boolean admin = hasRole(userDetails, "ROLE_ADMIN", "ADMIN");
        boolean instructor = hasRole(userDetails, "ROLE_INSTRUCTOR", "INSTRUCTOR");
        return ApiResponse.ok(
                "리뷰 답글이 등록되었습니다.",
                reviewService.answer(userId, admin, instructor, reviewId, req)
        );
    }

    @DeleteMapping("/{reviewId}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reviewId
    ) {
        Long userId = resolveUserId(userDetails);
        reviewService.delete(userId, reviewId);
        return ApiResponse.ok("리뷰가 삭제되었습니다. (소프트 삭제)", null);
    }

    @GetMapping("/classes/{classId}")
    public ApiResponse<List<ClassReviewResponse>> listByClass(
            @PathVariable Long classId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails == null ? null : userDetails.getUserId();
        boolean admin = hasRole(userDetails, "ROLE_ADMIN", "ADMIN");
        boolean instructor = hasRole(userDetails, "ROLE_INSTRUCTOR", "INSTRUCTOR");
        return ApiResponse.ok(reviewService.listByClass(classId, userId, admin, instructor));
    }

    @GetMapping("/me")
    public ApiResponse<List<ClassReviewResponse>> listMine(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = resolveUserId(userDetails);
        boolean admin = hasRole(userDetails, "ROLE_ADMIN", "ADMIN");
        return ApiResponse.ok(reviewService.listMy(userId, admin));
    }

    private Long resolveUserId(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getUserId() == null) {
            throw new BusinessException("로그인 정보가 필요합니다.");
        }
        return userDetails.getUserId();
    }

    private boolean hasRole(CustomUserDetails userDetails, String... candidates) {
        if (userDetails == null || userDetails.getAuthorities() == null) return false;
        for (GrantedAuthority authority : userDetails.getAuthorities()) {
            String role = authority.getAuthority();
            if (role == null) continue;
            for (String candidate : candidates) {
                if (candidate.equalsIgnoreCase(role)) {
                    return true;
                }
            }
        }
        return false;
    }
}
