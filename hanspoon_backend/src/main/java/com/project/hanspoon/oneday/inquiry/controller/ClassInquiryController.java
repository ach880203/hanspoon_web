package com.project.hanspoon.oneday.inquiry.controller;

import com.project.hanspoon.common.exception.BusinessException;
import com.project.hanspoon.common.response.ApiResponse;
import com.project.hanspoon.common.security.CustomUserDetails;
import com.project.hanspoon.oneday.inquiry.dto.ClassInquiryAnswerRequest;
import com.project.hanspoon.oneday.inquiry.dto.ClassInquiryCreateRequest;
import com.project.hanspoon.oneday.inquiry.dto.ClassInquiryResponse;
import com.project.hanspoon.oneday.inquiry.service.ClassInquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oneday/inquiries")
public class ClassInquiryController {

    private final ClassInquiryService classInquiryService;

    // 문의 작성: 로그인 사용자만 가능
    @PostMapping
    public ApiResponse<ClassInquiryResponse> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ClassInquiryCreateRequest req
    ) {
        Long userId = requireUserId(userDetails);
        return ApiResponse.ok("문의가 등록되었습니다.", classInquiryService.create(userId, req));
    }

    // 문의 목록: 누구나 조회 가능 (서버에서 비밀글 마스킹 처리)
    @GetMapping
    public ApiResponse<List<ClassInquiryResponse>> list(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long viewerUserId = userDetails != null ? userDetails.getUserId() : null;
        boolean isAdmin = isAdmin(userDetails);
        return ApiResponse.ok(classInquiryService.listAll(viewerUserId, isAdmin));
    }

    @GetMapping("/me")
    public ApiResponse<List<ClassInquiryResponse>> listMine(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = requireUserId(userDetails);
        boolean isAdmin = isAdmin(userDetails);
        return ApiResponse.ok(classInquiryService.listMy(userId, isAdmin));
    }

    // 답글 등록: 작성자 또는 관리자만 가능
    @PostMapping("/{inquiryId}/answer")
    public ApiResponse<ClassInquiryResponse> answer(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long inquiryId,
            @RequestBody ClassInquiryAnswerRequest req
    ) {
        Long actorUserId = requireUserId(userDetails);
        boolean isAdmin = isAdmin(userDetails);
        return ApiResponse.ok("답글이 등록되었습니다.", classInquiryService.answer(inquiryId, actorUserId, isAdmin, req));
    }

    private Long requireUserId(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getUserId() == null) {
            throw new BusinessException("로그인 정보가 필요합니다.");
        }
        return userDetails.getUserId();
    }

    private boolean isAdmin(CustomUserDetails userDetails) {
        if (userDetails == null) {
            return false;
        }
        return userDetails.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
