package com.project.hanspoon.shop.cart.controller;

import com.project.hanspoon.common.security.CustomUserDetails;
import com.project.hanspoon.shop.cart.dto.*;
import com.project.hanspoon.shop.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;

    // ===============================
    // ✅ 권장: user 기반 endpoint (/me)
    // ===============================

    // 장바구니 조회(내 장바구니)
    @GetMapping("/me")
    public ResponseEntity<CartResponseDto> getMyCart(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = requireUserId(userDetails);
        return ResponseEntity.ok(cartService.getCartByUser(userId));
    }

    // 장바구니 생성/확보(내 장바구니 없으면 생성)
    @PostMapping("/me")
    public ResponseEntity<CartCreateResponseDto> createMyCart(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = requireUserId(userDetails);
        return ResponseEntity.ok(cartService.createCart(userId));
    }

    // 담기(내 장바구니에)
    @PostMapping("/me/items")
    public ResponseEntity<CartResponseDto> addItemToMyCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CartItemAddRequestDto req
    ) {
        Long userId = requireUserId(userDetails);
        return ResponseEntity.ok(cartService.addItemByUser(userId, req));
    }

    @GetMapping("/me/count")
    public ResponseEntity<CartCountResponseDto> getMyCartCount(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = requireUserId(userDetails);
        int count = cartService.getCartTotalQuantityByUser(userId);
        return ResponseEntity.ok(CartCountResponseDto.builder().count(count).build());
    }

    // 수량 변경(내 장바구니에서)
    @PatchMapping("/me/items/{itemId}")
    public ResponseEntity<CartResponseDto> updateMyItemQty(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long itemId,
            @RequestBody @Valid CartItemUpdateRequestDto req
    ) {
        Long userId = requireUserId(userDetails);
        return ResponseEntity.ok(cartService.updateQuantityByUser(userId, itemId, req));
    }

    // 삭제(내 장바구니에서)
    @DeleteMapping("/me/items/{itemId}")
    public ResponseEntity<Void> deleteMyItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long itemId
    ) {
        Long userId = requireUserId(userDetails);
        cartService.removeItemByUser(userId, itemId);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // ✅ 기존 URL 호환 (프론트 깨짐 방지용)
    // - cartId를 "무시"하고 user 기준으로 처리
    // - 응답의 cartId를 프론트가 저장하면 안정화됨
    // ==========================================

    // (기존) 장바구니 생성: POST /api/carts
    @PostMapping
    public ResponseEntity<CartCreateResponseDto> createCart(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = requireUserId(userDetails);
        return ResponseEntity.ok(cartService.createCart(userId));
    }

    // (기존) 담기: POST /api/carts/{cartId}/items
    @PostMapping("/{cartId}/items")
    public ResponseEntity<CartResponseDto> addItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long cartId, // 호환용(사용 안 함)
            @RequestBody @Valid CartItemAddRequestDto req
    ) {
        Long userId = requireUserId(userDetails);
        return ResponseEntity.ok(cartService.addItemByUser(userId, req));
    }

    // (기존) 조회: GET /api/carts/{cartId}
    @GetMapping("/{cartId}")
    public ResponseEntity<CartResponseDto> getCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long cartId // 호환용(사용 안 함)
    ) {
        Long userId = requireUserId(userDetails);
        return ResponseEntity.ok(cartService.getCartByUser(userId));
    }

    // (기존) 수량 변경: PATCH /api/carts/{cartId}/items/{itemId}
    @PatchMapping("/{cartId}/items/{itemId}")
    public ResponseEntity<CartResponseDto> updateQty(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long cartId, // 호환용(사용 안 함)
            @PathVariable Long itemId,
            @RequestBody @Valid CartItemUpdateRequestDto req
    ) {
        Long userId = requireUserId(userDetails);
        return ResponseEntity.ok(cartService.updateQuantityByUser(userId, itemId, req));
    }

    // (기존) 삭제: DELETE /api/carts/{cartId}/items/{itemId}
    @DeleteMapping("/{cartId}/items/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long cartId, // 호환용(사용 안 함)
            @PathVariable Long itemId
    ) {
        Long userId = requireUserId(userDetails);
        cartService.removeItemByUser(userId, itemId);
        return ResponseEntity.noContent().build();
    }

    // ===============================
    // 공통
    // ===============================
    private Long requireUserId(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getUser() == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return userDetails.getUser().getUserId();
    }
}
