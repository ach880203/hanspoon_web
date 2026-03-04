package com.project.hanspoon.shop.inquiry.service;

import com.project.hanspoon.common.user.entity.User;
import com.project.hanspoon.shop.inquiry.entity.InqProduct;
import com.project.hanspoon.shop.inquiry.dto.InquiryAnswerRequestDto;
import com.project.hanspoon.shop.inquiry.dto.InquiryCreateRequestDto;
import com.project.hanspoon.shop.inquiry.dto.InquiryResponseDto;
import com.project.hanspoon.shop.inquiry.dto.InquiryUpdateRequestDto;
import com.project.hanspoon.shop.product.entity.Product;
import com.project.hanspoon.shop.inquiry.repository.InqProductRepository;
import com.project.hanspoon.shop.product.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryProductService {

    private final InqProductRepository inqProductRepository;
    private final ProductRepository productRepository;

    @PersistenceContext
    private EntityManager em;

    // ✅ 상품별 문의 목록
    public Page<InquiryResponseDto> listByProduct(Long productId, int page, int size,
            Long viewerUserId, boolean viewerIsAdmin) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        return inqProductRepository.findByProduct_IdOrderByIdDesc(productId, pageable)
                .map(inq -> toDto(inq, viewerUserId, viewerIsAdmin));
    }

    // ✅ 내 문의 목록
    public Page<InquiryResponseDto> listMyInquiries(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return inqProductRepository.findByUser_UserIdOrderByIdDesc(userId, pageable)
                .map(this::toDto);
    }

    // ✅ [Admin] 전역 문의 목록
    public Page<InquiryResponseDto> listAllForAdmin(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return inqProductRepository.findAllByOrderByIdDesc(pageable)
                .map(this::toDto);
    }

    // ✅ 문의 등록 (내 계정)
    @Transactional
    public InquiryResponseDto create(Long userId, Long productId, InquiryCreateRequestDto req) {
        if (!StringUtils.hasText(req.getContent())) {
            throw new ResponseStatusException(BAD_REQUEST, "문의 내용은 필수입니다.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "상품이 없습니다. id=" + productId));

        User userRef = em.getReference(User.class, userId);

        boolean secret = Boolean.TRUE.equals(req.getSecret());

        InqProduct saved = inqProductRepository.save(
                InqProduct.builder()
                        .user(userRef)
                        .product(product)
                        .content(req.getContent().trim())
                        .secret(secret)
                        .answeredYn(false)
                        .build());

        return toDto(saved);
    }

    // ✅ 문의 수정 (내 문의만)
    @Transactional
    public InquiryResponseDto updateMyInquiry(Long userId, Long inqId, InquiryUpdateRequestDto req) {
        InqProduct inq = inqProductRepository.findByIdAndUser_UserId(inqId, userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "문의가 없습니다. inqId=" + inqId));

        // 답변 달린 문의 수정 막고 싶으면 주석 해제
        // if (Boolean.TRUE.equals(inq.getAnsweredYn())) {
        // throw new ResponseStatusException(BAD_REQUEST, "답변된 문의는 수정할 수 없습니다.");
        // }

        if (!inq.getUser().getUserId().equals(userId)) {
            throw new ResponseStatusException(FORBIDDEN, "내 문의만 수정할 수 있습니다.");
        }

        if (req.getContent() != null) {
            String c = req.getContent().trim();
            if (StringUtils.hasText(c))
                inq.setContent(c);
        }

        if (req.getSecret() != null) {
            inq.setSecret(Boolean.TRUE.equals(req.getSecret()));
        }

        return toDto(inq);
    }

    // ✅ 문의 삭제 (내 문의만)
    @Transactional
    public void deleteMyInquiry(Long userId, Long inqId) {
        InqProduct inq = inqProductRepository.findByIdAndUser_UserId(inqId, userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "문의가 없습니다. inqId=" + inqId));

        // 답변 달린 문의 삭제 막고 싶으면 주석 해제
        // if (Boolean.TRUE.equals(inq.getAnsweredYn())) {
        // throw new ResponseStatusException(BAD_REQUEST, "답변된 문의는 삭제할 수 없습니다.");
        // }

        inqProductRepository.delete(inq);
    }

    @Transactional
    public void deleteInquiryByAdmin(Long inqId) {
        InqProduct inq = inqProductRepository.findById(inqId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "문의가 없습니다. inqId=" + inqId));
        inqProductRepository.delete(inq);
    }

    // ✅ 답변 등록 (관리자/판매자용으로 쓰는 메서드)
    // 역할체크(ADMIN 등)는 Controller/Security에서 걸어주면 됨
    @Transactional
    public InquiryResponseDto answer(Long inqId, InquiryAnswerRequestDto req) {
        InqProduct inq = inqProductRepository.findById(inqId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "문의가 없습니다. inqId=" + inqId));

        if (!StringUtils.hasText(req.getAnswer())) {
            throw new ResponseStatusException(BAD_REQUEST, "답변 내용은 필수입니다.");
        }

        inq.setAnswer(req.getAnswer().trim());
        inq.setAnsweredYn(true);
        inq.setAnsweredAt(LocalDateTime.now());

        return toDto(inq);
    }

    private InquiryResponseDto toDto(InqProduct i) {
        return InquiryResponseDto.builder()
                .inqId(i.getId())
                .productId(i.getProduct().getId())
                .productName(i.getProduct().getName())
                .userId(i.getUser().getUserId())
                .content(i.getContent())
                .answer(i.getAnswer())
                .answeredYn(i.getAnsweredYn())
                .secret(Boolean.TRUE.equals(i.getSecret()))
                .canViewSecret(true)
                .createdAt(i.getCreatedAt())
                .updatedAt(i.getUpdatedAt())
                .answeredAt(i.getAnsweredAt())
                .build();
    }

    private InquiryResponseDto toDto(InqProduct i, Long viewerUserId, boolean viewerIsAdmin) {
        boolean secret = Boolean.TRUE.equals(i.getSecret());
        boolean isOwner = (viewerUserId != null) && i.getUser().getUserId().equals(viewerUserId);

        // ✅ 비밀글이면: 본인/관리자만 원문 노출
        boolean canSee = !secret || isOwner || viewerIsAdmin;

        String content = canSee ? i.getContent() : "비밀글입니다.";
        String answer = canSee ? i.getAnswer() : null; // 필요하면 "비밀글입니다."로 해도 됨

        return InquiryResponseDto.builder()
                .inqId(i.getId())
                .productId(i.getProduct().getId())
                .productName(i.getProduct().getName())
                .userId(i.getUser().getUserId())
                .content(content)
                .answer(answer)
                .answeredYn(i.getAnsweredYn())
                .secret(secret)
                .canViewSecret(canSee)
                .createdAt(i.getCreatedAt())
                .updatedAt(i.getUpdatedAt())
                .answeredAt(i.getAnsweredAt())
                .build();
    }
}
