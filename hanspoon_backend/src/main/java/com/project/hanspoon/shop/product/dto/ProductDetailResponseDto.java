package com.project.hanspoon.shop.product.dto;

import com.project.hanspoon.shop.constant.ProductCategory;
import lombok.*;

import java.util.List;

/**
 * ProductDetailResponseDto
 * - “상품 상세 조회 API”의 응답(Response) 바디에 담길 데이터 구조
 * - 엔티티(Product, ProductImage)를 그대로 노출하지 않고,
 *   화면/클라이언트에 필요한 형태로 “가공된 결과”만 내려주기 위한 DTO
 *
 * 왜 엔티티 대신 DTO를 쓰나?
 * 1) 엔티티 구조 변경이 API 스펙에 그대로 노출되는 것을 방지
 * 2) 지연로딩/순환참조 같은 문제를 API 응답에서 차단
 * 3) 화면에 필요한 데이터만 골라서 내려 성능/보안/유지보수 개선
 */
@Getter
// 응답 DTO는 보통 “읽기 전용”이므로 Getter만 두는 구성이 일반적
@NoArgsConstructor
// Jackson(역직렬화)이나 프레임워크 처리에서 기본 생성자가 필요할 수 있어 제공
@AllArgsConstructor
// 모든 필드 생성자: 테스트/매핑 편의성
@Builder
// 빌더로 필요한 값만 골라 생성할 수 있어, 서비스/매퍼에서 조립하기 편함
public class ProductDetailResponseDto {

    /**
     * 상품 PK
     * - product_id 값을 그대로 내려주는 식별자
     */
    private Long id;

    /**
     * 카테고리(enum)
     * - 클라이언트가 카테고리별 UI/필터링 처리에 사용
     */
    private ProductCategory category;

    /**
     * 상품명
     */
    private String name;

    /**
     * 가격
     * - 현재 int: 원화 정수 단위 등 “소수점 없는 가격”에 적합
     */
    private int price;

    /**
     * 재고
     * - 상세 화면에서 “품절 여부/구매 가능 수량 제한” 등에 사용
     */
    private int stock;

    private String detailContent;

    /**
     * 썸네일 URL (대표 이미지)
     * - 상세 페이지 상단/리스트 카드에서 빠르게 보여주기 위한 대표 이미지 1장
     * - images 전체를 내려도 되지만,
     *   프론트에서 “대표 1장”을 매번 찾아야 하는 부담을 줄이기 위해 별도 필드로 둠
     */
    private String thumbnailUrl;

    /**
     * 이미지 목록(상세용)
     * - 상품에 연결된 여러 이미지(대표 포함 또는 대표 제외 정책은 서버에서 결정)
     * - ProductImageResponseDto로 내려주면 엔티티 노출 없이 필요한 값만 전달 가능
     */
    private List<ProductImageResponseDto> images;

    private List<ProductImageResponseDto> detailImages;
}
