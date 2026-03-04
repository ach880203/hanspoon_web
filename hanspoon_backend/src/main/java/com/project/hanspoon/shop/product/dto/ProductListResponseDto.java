package com.project.hanspoon.shop.product.dto;

import com.project.hanspoon.shop.constant.ProductCategory;
import lombok.*;

/**
 * ProductListResponseDto
 * - “상품 목록 조회 API”에서 각 상품 1개를 표현하는 응답 DTO
 * - 목록 화면(카테고리별/검색/페이징 리스트)에서 필요한 최소 필드를 담는다.
 *
 * 설계 의도:
 * - 상세 조회 DTO(ProductDetailResponseDto)보다 가볍게 구성해서
 *   목록 조회 성능(전송량/쿼리)을 최적화한다.
 * - 대표 썸네일(thumbnailUrl)만 내려 리스트 UI(카드/그리드) 렌더링을 단순화한다.
 */
@Getter
// 응답 DTO는 읽기 전용이므로 Getter만 제공
@NoArgsConstructor
// 프레임워크/테스트에서 필요할 수 있는 기본 생성자
@AllArgsConstructor
// 전체 필드 생성자: 매핑 편의
@Builder
// 빌더: 서비스/매퍼에서 조립하기 편함
public class ProductListResponseDto {

    /**
     * 상품 PK (product_id)
     * - 프론트에서 상세 페이지 이동(/products/{id}) 등에 사용
     */
    private Long id;

    /**
     * 상품 카테고리(enum)
     * - 프론트 필터/뱃지/카테고리 그룹핑 등에 사용
     */
    private ProductCategory category;

    /**
     * 상품명
     * - 목록 카드에서 제목/이름 표시
     */
    private String name;

    /**
     * 가격
     * - 목록 카드에서 가격 표시
     * - int: 원화 정수 단위 사용 시 적합
     */
    private int price;

    /**
     * 재고
     * - 목록에서 “품절” 표시, 구매 가능 여부 등에 활용
     *
     * ※ 만약 목록에서 재고를 보여주지 않는다면, 이 필드는 제외해도 됨(리뷰 참고)
     */
    private int stock;

    /**
     * 대표 썸네일 URL
     * - 리스트에서는 이미지 전체가 아니라 대표 1장만 필요하므로 별도 필드로 둠
     * - 대표 이미지가 없는 경우 null 가능
     */
    private String thumbnailUrl;
}
