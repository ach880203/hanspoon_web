package com.project.hanspoon.shop.wish.service;

import com.project.hanspoon.common.user.entity.User;
import com.project.hanspoon.shop.wish.dto.WishProductResponseDto;
import com.project.hanspoon.shop.wish.dto.WishToggleResponseDto;
import com.project.hanspoon.shop.product.entity.Product;
import com.project.hanspoon.shop.product.entity.ProductImage;
import com.project.hanspoon.shop.wish.entity.WishProduct;
import com.project.hanspoon.shop.product.repository.ProductImageRepository;
import com.project.hanspoon.shop.product.repository.ProductRepository;
import com.project.hanspoon.shop.wish.repository.WishProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishProductService {

    private final WishProductRepository wishProductRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    @PersistenceContext
    private EntityManager em;

    // ✅ 내 찜 목록(페이지)
    public Page<WishProductResponseDto> getMyWishes(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<WishProduct> wishes = wishProductRepository.findByUser_UserIdOrderByIdDesc(userId, pageable);

        // 썸네일 한번에 가져오기
        List<Long> productIds = wishes.getContent().stream()
                .map(w -> w.getProduct().getId())
                .distinct()
                .toList();

        Map<Long, String> thumbMap = loadThumbMap(productIds);

        List<WishProductResponseDto> content = wishes.getContent().stream()
                .map(w -> toDto(w, thumbMap.get(w.getProduct().getId())))
                .toList();

        return new PageImpl<>(content, pageable, wishes.getTotalElements());
    }

    // ✅ 이 상품을 내가 찜했는지
    public boolean isWished(Long userId, Long productId) {
        return wishProductRepository.existsByUser_UserIdAndProduct_Id(userId, productId);
    }

    // ✅ 찜 추가(이미 찜이면 그대로 true 반환 - idempotent)
    @Transactional
    public WishToggleResponseDto addWish(Long userId, Long productId) {
        // 상품 존재 확인
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "상품이 없습니다. id=" + productId));

        // 이미 존재하면 그대로
        if (wishProductRepository.existsByUser_UserIdAndProduct_Id(userId, productId)) {
            return WishToggleResponseDto.builder().productId(productId).wished(true).build();
        }

        User userRef = em.getReference(User.class, userId);

        wishProductRepository.save(
                WishProduct.builder()
                        .user(userRef)
                        .product(product)
                        .build()
        );

        return WishToggleResponseDto.builder().productId(productId).wished(true).build();
    }

    // ✅ 찜 해제(없어도 그냥 false 반환 - idempotent)
    @Transactional
    public WishToggleResponseDto removeWish(Long userId, Long productId) {
        wishProductRepository.findByUser_UserIdAndProduct_Id(userId, productId)
                .ifPresent(wishProductRepository::delete);

        return WishToggleResponseDto.builder().productId(productId).wished(false).build();
    }

    // ✅ 토글(있으면 삭제, 없으면 생성)
    @Transactional
    public WishToggleResponseDto toggleWish(Long userId, Long productId) {
        Optional<WishProduct> existing = wishProductRepository.findByUser_UserIdAndProduct_Id(userId, productId);
        if (existing.isPresent()) {
            wishProductRepository.delete(existing.get());
            return WishToggleResponseDto.builder().productId(productId).wished(false).build();
        }
        return addWish(userId, productId);
    }

    // -------------------
    // 내부 유틸
    // -------------------
    private Map<Long, String> loadThumbMap(List<Long> productIds) {
        Map<Long, String> map = new HashMap<>();
        if (productIds == null || productIds.isEmpty()) return map;

        List<ProductImage> reps = productImageRepository.findByProduct_IdInAndRepYnTrue(productIds);
        for (ProductImage img : reps) {
            map.put(img.getProduct().getId(), img.getImgUrl());
        }
        return map;
    }

    private WishProductResponseDto toDto(WishProduct w, String thumbUrl) {
        Product p = w.getProduct();
        return WishProductResponseDto.builder()
                .wishId(w.getId())
                .productId(p.getId())
                .name(p.getName())
                .price(p.getPrice())
                .category(p.getCategory() != null ? String.valueOf(p.getCategory()) : null) // enum/string 상관없게
                .thumbnailUrl(thumbUrl)
                .createdAt(w.getCreatedAt())
                .build();
    }
}
