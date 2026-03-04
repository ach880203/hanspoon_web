package com.project.hanspoon.shop.cart.service;

import com.project.hanspoon.shop.cart.dto.*;
import com.project.hanspoon.shop.cart.entity.Cart;
import com.project.hanspoon.shop.cart.entity.CartItem;
import com.project.hanspoon.shop.product.entity.Product;
import com.project.hanspoon.shop.product.entity.ProductImage;
import com.project.hanspoon.shop.cart.repository.CartItemRepository;
import com.project.hanspoon.shop.cart.repository.CartRepository;
import com.project.hanspoon.shop.product.repository.ProductImageRepository;
import com.project.hanspoon.shop.product.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    @PersistenceContext
    private EntityManager em;

    // =========================================================
    // ✅ User 기반 (권장: 앞으로 컨트롤러/프론트는 이걸 쓰는 방향)
    // =========================================================

    /**
     * userId로 장바구니 조회, 없으면 생성
     * (Cart.user nullable=false, unique=true 인 경우 가장 안전한 방식)
     */
    @Transactional
    public CartCreateResponseDto createCart(Long userId) {
        Cart cart = getOrCreateCartByUserId(userId);
        return CartCreateResponseDto.builder().cartId(cart.getId()).build();
    }

    /**
     * userId로 담기 (cartId 필요 없음)
     */
    @Transactional
    public CartResponseDto addItemByUser(Long userId, CartItemAddRequestDto req) {
        Cart cart = getOrCreateCartByUserId(userId);
        addItemInternal(cart, req);
        return buildCartResponse(cart.getId());
    }

    /**
     * userId로 장바구니 조회
     */
    public CartResponseDto getCartByUser(Long userId) {
        Cart cart = cartRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "장바구니가 없습니다. userId=" + userId));
        return buildCartResponse(cart.getId());
    }

    /**
     * userId로 수량 변경
     */
    @Transactional
    public CartResponseDto updateQuantityByUser(Long userId, Long itemId, CartItemUpdateRequestDto req) {
        Cart cart = cartRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "장바구니가 없습니다. userId=" + userId));

        CartItem item = cartItemRepository.findByIdAndCart_Id(itemId, cart.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "장바구니 아이템이 없습니다. itemId=" + itemId));

        Product product = item.getProduct();
        if (req.getQuantity() > product.getStock()) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }

        item.changeQuantity(req.getQuantity());
        return buildCartResponse(cart.getId());
    }

    /**
     * userId로 아이템 삭제
     */
    @Transactional
    public void removeItemByUser(Long userId, Long itemId) {
        Cart cart = cartRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "장바구니가 없습니다. userId=" + userId));

        CartItem item = cartItemRepository.findByIdAndCart_Id(itemId, cart.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "장바구니 아이템이 없습니다. itemId=" + itemId));

        cartItemRepository.delete(item);
    }

    // =========================================================
    // ✅ 기존 cartId 기반 (호환용: 기존 컨트롤러 유지 시 사용)
    // =========================================================

    /**
     * ⚠️ Cart.user가 NOT NULL이면 이 메서드는 사용하면 안 됩니다.
     * 컨트롤러에서 createCart(userId)로 호출하게 바꾸세요.
     */
    @Transactional
    public CartCreateResponseDto createCart() {
        throw new IllegalStateException("Cart.user가 필수이면 createCart(userId)를 사용해야 합니다.");
    }

    @Transactional
    public CartResponseDto addItem(Long cartId, CartItemAddRequestDto req) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "장바구니가 없습니다. cartId=" + cartId));

        addItemInternal(cart, req);
        return buildCartResponse(cartId);
    }

    public CartResponseDto getCart(Long cartId) {
        cartRepository.findById(cartId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "장바구니가 없습니다. cartId=" + cartId));
        return buildCartResponse(cartId);
    }

    @Transactional
    public CartResponseDto updateQuantity(Long cartId, Long itemId, CartItemUpdateRequestDto req) {
        CartItem item = cartItemRepository.findByIdAndCart_Id(itemId, cartId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "장바구니 아이템이 없습니다. itemId=" + itemId));

        Product product = item.getProduct();
        if (req.getQuantity() > product.getStock()) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }

        item.changeQuantity(req.getQuantity());
        return buildCartResponse(cartId);
    }

    @Transactional
    public void removeItem(Long cartId, Long itemId) {
        CartItem item = cartItemRepository.findByIdAndCart_Id(itemId, cartId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "장바구니 아이템이 없습니다. itemId=" + itemId));
        cartItemRepository.delete(item);
    }

    // =========================================================
    // 내부 공통 로직
    // =========================================================

    /**
     * userId로 장바구니 없으면 생성
     */
    private Cart getOrCreateCartByUserId(Long userId) {
        return cartRepository.findByUser_UserId(userId)
                .orElseGet(() -> {
                    try {
                        var userRef = em.getReference(com.project.hanspoon.common.user.entity.User.class, userId);
                        Cart newCart = Cart.builder().user(userRef).build();
                        return cartRepository.save(newCart);
                    } catch (DataIntegrityViolationException e) {
                        // 동시에 생성 시도 -> UNIQUE 걸릴 수 있음 -> 다시 조회
                        return cartRepository.findByUser_UserId(userId)
                                .orElseThrow(() -> e);
                    }
                });
    }

    /**
     * 담기 핵심 로직(카트 엔티티를 받아서 처리)
     */
    private void addItemInternal(Cart cart, CartItemAddRequestDto req) {
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "상품이 없습니다. id=" + req.getProductId()));

        if (req.getQuantity() > product.getStock()) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }

        Long cartId = cart.getId();

        CartItem item = cartItemRepository.findByCart_IdAndProduct_Id(cartId, product.getId())
                .orElse(null);

        if (item == null) {
            cartItemRepository.save(
                    CartItem.builder()
                            .cart(cart)
                            .product(product)
                            .quantity(req.getQuantity())
                            .build()
            );
        } else {
            int newQty = item.getQuantity() + req.getQuantity();
            if (newQty > product.getStock()) {
                throw new IllegalArgumentException("재고가 부족합니다.");
            }
            item.addQuantity(req.getQuantity());
        }
    }

    /**
     * 장바구니 응답 DTO 생성(네 기존 getCart 로직을 분리)
     */
    private CartResponseDto buildCartResponse(Long cartId) {
        List<CartItem> items = cartItemRepository.findByCart_IdOrderByIdDesc(cartId);

        // 썸네일 한번에 가져오기
        List<Long> productIds = items.stream().map(i -> i.getProduct().getId()).distinct().toList();
        Map<Long, String> thumbMap = new HashMap<>();

        if (!productIds.isEmpty()) {
            List<ProductImage> reps = productImageRepository.findByProduct_IdInAndRepYnTrue(productIds);
            for (ProductImage img : reps) {
                thumbMap.put(img.getProduct().getId(), img.getImgUrl());
            }
        }

        int totalQty = 0;
        int totalPrice = 0;

        List<CartItemResponseDto> dtoList = new ArrayList<>();
        for (CartItem ci : items) {
            Product p = ci.getProduct();
            int lineTotal = p.getPrice() * ci.getQuantity();

            totalQty += ci.getQuantity();
            totalPrice += lineTotal;

            dtoList.add(CartItemResponseDto.builder()
                    .itemId(ci.getId())
                    .productId(p.getId())
                    .name(p.getName())
                    .price(p.getPrice())
                    .stock(p.getStock())
                    .quantity(ci.getQuantity())
                    .lineTotal(lineTotal)
                    .thumbnailUrl(thumbMap.get(p.getId()))
                    .build());
        }

        return CartResponseDto.builder()
                .cartId(cartId)
                .items(dtoList)
                .totalQuantity(totalQty)
                .totalPrice(totalPrice)
                .build();
    }

    public int getCartTotalQuantityByUser(Long userId) {
        try {
            CartResponseDto cart = getCartByUser(userId); // 기존 메서드 재사용
            return cart.getTotalQuantity();               // ✅ DTO에 이미 있음
        } catch (ResponseStatusException e) {
            if (e.getStatusCode().value() == NOT_FOUND.value()) return 0;
            throw e;
        }
    }
}
