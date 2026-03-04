package com.project.hanspoon.shop.product.dto;

import com.project.hanspoon.shop.constant.ProductCategory;
import lombok.*;

/**
 * ProductResponseDto
 * - 상품 정보를 “기본 형태”로 응답할 때 사용하는 Response DTO
 *
 * 주 사용처 예시:
 * 1) 상품 등록(Create) 성공 후 저장된 상품 정보 반환
 * 2) 상품 수정(Update) 성공 후 변경된 상품 정보 반환
 * 3) 내부적으로 엔티티(Product)를 외부에 그대로 노출하지 않고,
 *    필요한 값만 담아 내려주기 위한 공통 응답 모델
 *
 * 특징:
 * - 목록 DTO(ProductListResponseDto)처럼 썸네일이 없고
 * - 상세 DTO(ProductDetailResponseDto)처럼 이미지 리스트도 없음
 * → "상품 자체의 핵심 필드만" 담는 미니멀한 응답 DTO
 */
@Getter
// Response DTO는 읽기 전용이므로 보통 Getter만 제공
@NoArgsConstructor
// Jackson 직렬화/테스트 편의상 기본 생성자 제공 (상황에 따라 생략 가능)
@AllArgsConstructor
// 전체 필드 생성자: 매핑/테스트에서 편함
@Builder
// 빌더: 서비스/매퍼에서 가독성 있게 조립 가능
public class ProductResponseDto {

    /**
     * 상품 PK
     * - product_id
     */
    private Long id;

    /**
     * 상품 카테고리(enum)
     * - 프론트에서 뱃지/필터/분류 등에 사용
     */
    private ProductCategory category;

    /**
     * 상품명
     */
    private String name;

    /**
     * 가격
     * - int: 원화 정수 단위 등 소수점 없는 가격에 적합
     */
    private int price;

    /**
     * 재고
     * - 재고 수량
     */
    private int stock;
}
