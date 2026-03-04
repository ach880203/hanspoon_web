package com.project.hanspoon.shop.review.controller;

import com.project.hanspoon.common.security.CustomUserDetails;
import com.project.hanspoon.shop.review.dto.*;
import com.project.hanspoon.shop.review.service.ReviewProductService;
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
public class ReviewProductController {

    private final ReviewProductService reviewService;

    // ✅ 상품별 후기 목록 (로그인 없어도 조회 가능)
    // 추가 파라미터: sort=BEST|LATEST, rating=1~5, keyword=검색어
    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<Page<ReviewResponseDto>> listByProduct(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "LATEST") String sort,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(
                reviewService.listByProduct(productId, page, size, sort, rating, keyword)
        );
    }

    // ✅ (추가) 쿠팡 좌측 요약용 API
    @GetMapping("/products/{productId}/reviews/summary")
    public ResponseEntity<ReviewSummaryDto> summary(
            @PathVariable Long productId
    ) {
        return ResponseEntity.ok(reviewService.summaryByProduct(productId));
    }

    // ✅ 내 후기 목록
    @GetMapping("/reviews/me")
    public ResponseEntity<Page<ReviewResponseDto>> myReviews(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = requireUserId(userDetails);
        return ResponseEntity.ok(reviewService.listMyReviews(userId, page, size));
    }

    // ✅ 후기 등록(내 계정)
    @PostMapping("/products/{productId}/reviews")
    public ResponseEntity<ReviewResponseDto> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long productId,
            @RequestBody @Valid ReviewCreateRequestDto req
    ) {
        Long userId = requireUserId(userDetails);
        return ResponseEntity.ok(reviewService.create(userId, productId, req));
    }

    // ✅ 후기 수정(내 후기만)
    @PatchMapping("/reviews/{revId}")
    public ResponseEntity<ReviewResponseDto> update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long revId,
            @RequestBody @Valid ReviewUpdateRequestDto req
    ) {
        Long userId = requireUserId(userDetails);
        return ResponseEntity.ok(reviewService.update(userId, revId, req));
    }

    // ✅ 후기 삭제(내 후기만)
    @DeleteMapping("/reviews/{revId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long revId
    ) {
        Long userId = requireUserId(userDetails);
        reviewService.delete(userId, revId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/admin/reviews/{revId}")
    public ResponseEntity<Void> deleteByAdmin(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long revId
    ) {
        if (!isAdmin(userDetails)) {
            throw new ResponseStatusException(FORBIDDEN, "관리자 권한이 필요합니다.");
        }
        reviewService.deleteByAdmin(revId);
        return ResponseEntity.noContent().build();
    }

    private boolean isAdmin(CustomUserDetails userDetails) {
        if (userDetails == null) return false;
        return userDetails.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    private Long requireUserId(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getUser() == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return userDetails.getUser().getUserId();
    }
}
