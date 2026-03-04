package com.project.hanspoon.shop.wish.controller;

import com.project.hanspoon.common.security.CustomUserDetails;
import com.project.hanspoon.shop.wish.dto.WishProductResponseDto;
import com.project.hanspoon.shop.wish.dto.WishToggleResponseDto;
import com.project.hanspoon.shop.wish.service.WishProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishes")
public class WishProductController {

    private final WishProductService wishProductService;

    // ✅ 내 찜 목록
    @GetMapping("/me")
    public ResponseEntity<Page<WishProductResponseDto>> myWishes(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = requireUserId(userDetails);
        return ResponseEntity.ok(wishProductService.getMyWishes(userId, page, size));
    }

    // ✅ 내가 이 상품 찜했는지
    @GetMapping("/me/products/{productId}")
    public ResponseEntity<WishToggleResponseDto> isWished(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long productId
    ) {
        Long userId = requireUserId(userDetails);
        boolean wished = wishProductService.isWished(userId, productId);
        return ResponseEntity.ok(WishToggleResponseDto.builder().productId(productId).wished(wished).build());
    }

    // ✅ 찜 추가
    @PostMapping("/me/products/{productId}")
    public ResponseEntity<WishToggleResponseDto> addWish(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long productId
    ) {
        Long userId = requireUserId(userDetails);
        return ResponseEntity.ok(wishProductService.addWish(userId, productId));
    }

    // ✅ 찜 해제
    @DeleteMapping("/me/products/{productId}")
    public ResponseEntity<WishToggleResponseDto> removeWish(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long productId
    ) {
        Long userId = requireUserId(userDetails);
        return ResponseEntity.ok(wishProductService.removeWish(userId, productId));
    }

    // ✅ 토글(프론트 버튼 하나로 처리하고 싶으면 이거 쓰면 됨)
    @PostMapping("/me/products/{productId}/toggle")
    public ResponseEntity<WishToggleResponseDto> toggleWish(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long productId
    ) {
        Long userId = requireUserId(userDetails);
        return ResponseEntity.ok(wishProductService.toggleWish(userId, productId));
    }

    private Long requireUserId(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getUser() == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return userDetails.getUser().getUserId();
    }
}
