package com.project.hanspoon.shop.inquiry.controller;

import com.project.hanspoon.common.security.CustomUserDetails;
import com.project.hanspoon.shop.inquiry.dto.InquiryAnswerRequestDto;
import com.project.hanspoon.shop.inquiry.dto.InquiryCreateRequestDto;
import com.project.hanspoon.shop.inquiry.dto.InquiryResponseDto;
import com.project.hanspoon.shop.inquiry.dto.InquiryUpdateRequestDto;
import com.project.hanspoon.shop.inquiry.service.InquiryProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class InquiryProductController {

    private final InquiryProductService inquiryService;

    // ✅ 상품별 문의 목록 (로그인 없어도 조회 가능)
    @GetMapping("/products/{productId}/inquiries")
    public ResponseEntity<Page<InquiryResponseDto>> listByProduct(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long viewerUserId = (userDetails != null && userDetails.getUser() != null)
                ? userDetails.getUser().getUserId()
                : null;

        boolean viewerIsAdmin = isAdmin(userDetails);

        return ResponseEntity.ok(
                inquiryService.listByProduct(productId, page, size, viewerUserId, viewerIsAdmin));
    }

    private boolean isAdmin(CustomUserDetails userDetails) {
        if (userDetails == null)
            return false;
        return userDetails.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    // ✅ 내 문의 목록
    @GetMapping("/inquiries/me")
    public ResponseEntity<Page<InquiryResponseDto>> myInquiries(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = requireUserId(userDetails);
        return ResponseEntity.ok(inquiryService.listMyInquiries(userId, page, size));
    }

    // ✅ 문의 등록(내 계정)
    @PostMapping("/products/{productId}/inquiries")
    public ResponseEntity<InquiryResponseDto> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long productId,
            @RequestBody @Valid InquiryCreateRequestDto req) {
        Long userId = requireUserId(userDetails);
        return ResponseEntity.ok(inquiryService.create(userId, productId, req));
    }

    // ✅ 문의 수정(내 문의만)
    @PatchMapping("/inquiries/{inqId}")
    public ResponseEntity<InquiryResponseDto> updateMy(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long inqId,
            @RequestBody @Valid InquiryUpdateRequestDto req) {
        Long userId = requireUserId(userDetails);
        return ResponseEntity.ok(inquiryService.updateMyInquiry(userId, inqId, req));
    }

    // ✅ 문의 삭제(내 문의만)
    @DeleteMapping("/inquiries/{inqId}")
    public ResponseEntity<Void> deleteMy(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long inqId) {
        Long userId = requireUserId(userDetails);
        inquiryService.deleteMyInquiry(userId, inqId);
        return ResponseEntity.noContent().build();
    }

    // ✅ 답변 등록(관리자/판매자용)
    // 역할체크는 Security에서 제한 걸면 됨(예: ADMIN만 접근)
    @PostMapping("/inquiries/{inqId}/answer")
    public ResponseEntity<InquiryResponseDto> answer(
            @PathVariable Long inqId,
            @RequestBody @Valid InquiryAnswerRequestDto req) {
        return ResponseEntity.ok(inquiryService.answer(inqId, req));
    }

    // ✅ [Admin] 전역 문의 목록 조회
    @GetMapping("/admin/inquiries")
    public ResponseEntity<Page<InquiryResponseDto>> listAllAdmin(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (!isAdmin(userDetails)) {
            throw new ResponseStatusException(FORBIDDEN, "관리자 권한이 필요합니다.");
        }
        return ResponseEntity.ok(inquiryService.listAllForAdmin(page, size));
    }

    @DeleteMapping("/admin/inquiries/{inqId}")
    public ResponseEntity<Void> deleteAdminInquiry(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long inqId) {
        if (!isAdmin(userDetails)) {
            throw new ResponseStatusException(FORBIDDEN, "관리자 권한이 필요합니다.");
        }
        inquiryService.deleteInquiryByAdmin(inqId);
        return ResponseEntity.noContent().build();
    }

    private Long requireUserId(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getUser() == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return userDetails.getUser().getUserId();
    }
}
