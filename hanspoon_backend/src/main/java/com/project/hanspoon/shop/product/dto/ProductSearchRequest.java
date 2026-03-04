package com.project.hanspoon.shop.product.dto;

import com.project.hanspoon.shop.constant.ProductCategory;
import lombok.Getter;
import lombok.Setter;

/**
 * ProductSearchRequest
 * - “상품 목록 조회(검색/필터)” API에서
 *   query parameter들을 하나의 객체로 묶어 받기 위한 요청 DTO
 *
 * 예:
 *  /products?category=INGREDIENT&keyword=양파&minPrice=1000&maxPrice=5000
 *
 * 역할:
 * - Controller에서 @ModelAttribute(기본)로 바인딩되어 들어오며,
 * - Service/Repository에서 동적 조건(where절)을 구성할 때 사용된다.
 *
 * 설계 포인트:
 * - 모든 필드는 “선택값”이므로 null일 수 있다.
 *   (사용자가 해당 필터를 적용하지 않으면 조건에서 제외)
 */
@Getter @Setter
// 검색 파라미터는 바인딩/수정이 필요할 수 있어 setter를 두는 경우가 많음
// (예: keyword trim, min/max 보정 등)
public class ProductSearchRequest {

    /**
     * 카테고리 필터
     * - 예: ?category=INGREDIENT
     *
     * 주의:
     * - enum은 문자열이 정확히 매칭되어야 바인딩됨
     * - 잘못된 값이 오면 바인딩 예외가 발생할 수 있음(아래 리뷰 참고)
     */
    private ProductCategory category;

    /**
     * 키워드 검색어
     * - 예: ?keyword=양파
     * - 보통 상품명(name) LIKE 검색 또는 전문검색(FTS) 조건에 사용
     */
    private String keyword;

    /**
     * 최소 가격 필터
     * - 예: ?minPrice=1000
     * - Integer인 이유: 파라미터가 없을 때 null로 두어 “조건 미적용”을 표현하기 위함
     */
    private Integer minPrice;

    /**
     * 최대 가격 필터
     * - 예: ?maxPrice=5000
     * - Integer인 이유: 파라미터가 없을 때 null로 두어 “조건 미적용”을 표현하기 위함
     */
    private Integer maxPrice;
}
