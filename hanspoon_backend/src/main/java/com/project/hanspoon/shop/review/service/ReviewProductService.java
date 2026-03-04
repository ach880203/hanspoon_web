package com.project.hanspoon.shop.review.service;

import com.project.hanspoon.common.user.entity.User;
import com.project.hanspoon.shop.product.entity.Product;
import com.project.hanspoon.shop.product.repository.ProductRepository;
import com.project.hanspoon.shop.review.dto.*;
import com.project.hanspoon.shop.review.entity.RevProduct;
import com.project.hanspoon.shop.review.repository.RevProductRepository;
import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewProductService {

    private final RevProductRepository revProductRepository;
    private final ProductRepository productRepository;

    @PersistenceContext
    private EntityManager em;

    // ✅ 상품별 후기 목록 (+ sort/rating/keyword)
    public Page<ReviewResponseDto> listByProduct(Long productId, int page, int size,
                                                 String sort, Integer rating, String keyword) {

        // 정렬: LATEST(최신) / BEST(베스트=별점 우선)
        Sort s;
        if ("BEST".equalsIgnoreCase(sort)) {
            s = Sort.by(Sort.Direction.DESC, "rating")
                    .and(Sort.by(Sort.Direction.DESC, "id"));
        } else { // 기본 LATEST
            s = Sort.by(Sort.Direction.DESC, "id");
        }

        Pageable pageable = PageRequest.of(page, size, s);

        String kw = (StringUtils.hasText(keyword)) ? keyword.trim() : null;

        return revProductRepository.searchByProduct(productId, rating, kw, pageable)
                .map(this::toDto);
    }

    // ✅ 좌측 요약(평균/총개수/별점분포)
    public ReviewSummaryDto summaryByProduct(Long productId) {
        // 상품 존재 체크(원하면 제거 가능)
        if (!productRepository.existsById(productId)) {
            throw new ResponseStatusException(NOT_FOUND, "상품이 없습니다. id=" + productId);
        }

        long total = revProductRepository.countByProduct_Id(productId);
        Double avg = revProductRepository.avgRatingByProduct(productId);
        if (avg == null) avg = 0.0;

        Map<Integer, Long> counts = new HashMap<>();
        // 기본 0 세팅(1~5)
        for (int i = 1; i <= 5; i++) counts.put(i, 0L);

        List<Object[]> rows = revProductRepository.countGroupByRating(productId);
        for (Object[] row : rows) {
            Integer r = (Integer) row[0];
            Long c = (Long) row[1];
            if (r != null) counts.put(r, c);
        }

        // 소수점 1자리로 정리(선택)
        double avg1 = Math.round(avg * 10.0) / 10.0;

        return ReviewSummaryDto.builder()
                .avgRating(avg1)
                .totalCount(total)
                .countsByRating(counts)
                .build();
    }

    // ✅ 내 후기 목록(기존)
    public Page<ReviewResponseDto> listMyReviews(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return revProductRepository.findByUser_UserIdOrderByIdDesc(userId, pageable)
                .map(this::toDto);
    }

    // ✅ 후기 등록(기존)
    @Transactional
    public ReviewResponseDto create(Long userId, Long productId, ReviewCreateRequestDto req) {
        if (!StringUtils.hasText(req.getContent())) {
            throw new ResponseStatusException(BAD_REQUEST, "후기 내용은 필수입니다.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "상품이 없습니다. id=" + productId));

        User userRef = em.getReference(User.class, userId);

        RevProduct saved = revProductRepository.save(
                RevProduct.builder()
                        .user(userRef)
                        .product(product)
                        .content(req.getContent().trim())
                        .rating(req.getRating())
                        .build()
        );

        return toDto(saved);
    }

    // ✅ 후기 수정(기존)
    @Transactional
    public ReviewResponseDto update(Long userId, Long revId, ReviewUpdateRequestDto req) {
        RevProduct review = revProductRepository.findByIdAndUser_UserId(revId, userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "후기가 없습니다. revId=" + revId));

        if (req.getContent() != null) {
            String c = req.getContent().trim();
            if (!c.isEmpty()) review.setContent(c);
        }
        if (req.getRating() != null) {
            review.setRating(req.getRating());
        }
        return toDto(review);
    }

    // ✅ 후기 삭제(기존)
    @Transactional
    public void delete(Long userId, Long revId) {
        RevProduct review = revProductRepository.findByIdAndUser_UserId(revId, userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "후기가 없습니다. revId=" + revId));
        revProductRepository.delete(review);
    }

    @Transactional
    public void deleteByAdmin(Long revId) {
        RevProduct review = revProductRepository.findById(revId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "리뷰가 없습니다. revId=" + revId));
        revProductRepository.delete(review);
    }

    private ReviewResponseDto toDto(RevProduct r) {
        return ReviewResponseDto.builder()
                .revId(r.getId())
                .productId(r.getProduct().getId())
                .userId(r.getUser().getUserId())
                .content(r.getContent())
                .rating(r.getRating())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
