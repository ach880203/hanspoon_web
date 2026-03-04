package com.project.hanspoon.shop.product.repository.spec;

import com.project.hanspoon.shop.constant.ProductCategory;
import com.project.hanspoon.shop.product.entity.Product;
import org.springframework.data.jpa.domain.Specification;

/**
 * ProductSpecifications
 * - Spring Data JPA Specification(동적 조건)을 모아둔 유틸 클래스
 *
 * 목적:
 * - 검색 조건(카테고리/키워드/가격 범위)을 “조합 가능”한 형태로 제공
 * - 서비스에서 조건이 있는 것만 spec.and(...)로 연결해 동적 where 절 구성
 *
 * 특징:
 * - 각 메서드는 Specification<Product>를 반환
 * - 반환된 Specification은 (root, query, cb) -> Predicate 형태로 조건을 정의한다.
 *
 * 용어:
 * - root  : 조회 대상 엔티티(Product)의 Root(컬럼 접근 시작점)
 * - query : CriteriaQuery (select/count 등 쿼리 구조)
 * - cb    : CriteriaBuilder (equal/like/ge/le 등 predicate 생성기)
 */
public class ProductSpecifications {

    private ProductSpecifications() {}
    // 유틸 클래스(정적 메서드만)로 사용하기 위해 인스턴스 생성 방지

    /**
     * 카테고리 일치 조건
     * - where category = :category
     */
    public static Specification<Product> categoryEq(ProductCategory category) {
        return (root, query, cb) -> cb.equal(root.get("category"), category);
    }

    /**
     * 상품명 부분 포함(부분 문자열 검색)
     * - where name like %keyword%
     *
     * 주의:
     * - 앞에 %가 붙는 패턴(%keyword%)은 일반 B-Tree 인덱스를 타기 어려워 성능이 떨어질 수 있음
     * - 키워드에 %, _ 같은 like 와일드카드 문자가 들어오면 의도치 않은 검색이 될 수 있음(리뷰 참고)
     */
    public static Specification<Product> nameContains(String keyword) {
        return (root, query, cb) -> cb.like(root.get("name"), "%" + keyword + "%");
    }

    /**
     * 최소 가격 조건
     * - where price >= :minPrice
     */
    public static Specification<Product> priceGte(Integer minPrice) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    /**
     * 최대 가격 조건
     * - where price <= :maxPrice
     */
    public static Specification<Product> priceLte(Integer maxPrice) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }
}