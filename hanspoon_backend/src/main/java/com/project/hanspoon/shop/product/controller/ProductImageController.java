package com.project.hanspoon.shop.product.controller;

import com.project.hanspoon.shop.product.dto.ProductImageResponseDto;
import com.project.hanspoon.shop.product.entity.ProductImageType;
import com.project.hanspoon.shop.product.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * ProductImageController
 * - 상품(Product)의 하위 리소스인 “상품 이미지(ProductImage)”를 다루는 REST 컨트롤러
 *
 * URI 설계:
 * - /api/products/{productId}/images
 *   → 특정 상품에 속한 이미지들의 컬렉션 리소스
 *
 * 제공 기능:
 * - POST   : 이미지 업로드
 * - GET    : 이미지 목록 조회
 * - DELETE : 이미지 삭제
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products/{productId}/images")
public class ProductImageController {

    private final ProductImageService productImageService;

    /**
     * ✅ 이미지 업로드 (multipart/form-data)
     *
     * form-data key:
     * - files: 이미지 파일들(여러 개 가능)
     * - repIndex: 대표 이미지 인덱스(선택, 0부터)
     *
     * 바인딩:
     * - productId: 경로 변수
     * - files: multipart 파트로 파일 리스트를 받음
     * - repIndex: 요청 파라미터(없어도 됨)
     *
     * 응답:
     * - 현재는 200 OK + 업로드된 이미지 DTO 리스트 반환
     *   (REST 관점에서 201 Created로 바꾸는 것도 고려 가능 - 리뷰 참고)
     */
    @PostMapping
    public ResponseEntity<List<ProductImageResponseDto>> upload(
            @PathVariable Long productId,
            @RequestPart("files") List<MultipartFile> files,
            @RequestParam(required = false) Integer repIndex,
            @RequestParam(defaultValue = "MAIN") ProductImageType type
    ) {
        return ResponseEntity.ok(productImageService.upload(productId, files, repIndex, type));
    }

    /**
     * ✅ 이미지 목록 조회
     *
     * 요청:
     * GET /api/products/{productId}/images
     *
     * 응답:
     * - 해당 상품의 이미지 리스트(대표가 먼저 오도록 정렬된 형태)
     */
    @GetMapping
    public ResponseEntity<List<ProductImageResponseDto>> list(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "MAIN") ProductImageType type
    ) {
        return ResponseEntity.ok(productImageService.list(productId, type));
    }

    /**
     * ✅ 이미지 삭제
     *
     * 요청:
     * DELETE /api/products/{productId}/images/{imageId}
     *
     * 처리:
     * - 서비스에서 (1) 상품 소속 검증 (2) 파일 삭제 (3) DB 삭제 (4) 대표 승격 처리
     *
     * 응답:
     * - 204 No Content (삭제 성공 시 바디 없음)
     */
    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> delete(@PathVariable Long productId, @PathVariable Long imageId) {
        productImageService.delete(productId, imageId);
        return ResponseEntity.noContent().build();
    }
}
