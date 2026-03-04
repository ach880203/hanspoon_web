package com.project.hanspoon.shop.product.repository;

import com.project.hanspoon.shop.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * ProductImageRepository
 * - ProductImage 엔티티(product_image 테이블)에 대한 데이터 접근 계층
 * - 상품 이미지 조회는 “상세(전체 목록)”와 “목록(대표 1장)”이 요구사항이 달라서,
 *   각각에 최적화된 메서드를 분리해 둔 구성
 */
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    /**
     * 특정 상품의 이미지 목록을 정렬해서 조회
     * - repYn(대표 여부) DESC  => 대표(true)가 먼저 오도록
     * - id ASC                => 같은 그룹 내에서는 오래된(작은 id) 순으로 정렬
     *
     * 목적:
     * - 상세 화면에서 "대표 이미지가 항상 첫 번째"로 오도록 보장
     * - 프론트에서 별도 정렬 없이 그대로 렌더하기 쉬움
     *
     * 예상 SQL 개념:
     * SELECT * FROM product_image
     * WHERE product_id = ?
     * ORDER BY rep_yn DESC, product_image_id ASC;
     */
    List<ProductImage> findByProduct_IdOrderByRepYnDescIdAsc(Long productId);

    /**
     * 특정 상품의 “대표 이미지” 1장을 조회
     * - repYn = true 조건
     * - 결과가 없을 수도 있으므로 Optional
     *
     * 목적:
     * - 썸네일 URL이 필요한 곳(목록/상세 대표 표시)에서 빠르게 대표 1장을 가져오기
     *
     * 주의:
     * - “대표 이미지가 여러 장”인 데이터가 존재한다면 어떤 것이 first로 잡힐지 불명확
     *   → 대표는 1장만 유지되도록 서비스 레벨에서 규칙 강제 필요
     */
    Optional<ProductImage> findFirstByProduct_IdAndRepYnTrue(Long productId);

    /**
     * 여러 상품의 “대표 이미지(썸네일)”를 한 번에 조회
     * - product_id IN (...) AND repYn = true
     *
     * 목적:
     * - 상품 목록 조회 시 N개 상품에 대해 대표이미지를 N번 조회하면(N+1) 성능이 떨어짐
     * - 이 메서드는 대표 이미지들을 “IN 쿼리 1번”으로 가져와서 목록에 매핑하기 위함
     *
     * 예상 SQL 개념:
     * SELECT * FROM product_image
     * WHERE product_id IN (?, ?, ?, ...)
     *   AND rep_yn = 1;
     */
    List<ProductImage> findByProduct_IdInAndRepYnTrue(List<Long> productIds);
}
