package com.project.hanspoon.shop.product.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * ProductImage 엔티티
 * - product_image 테이블과 매핑되는 “상품 이미지” 엔티티
 * - Product(상품) : ProductImage(이미지) = 1 : N 관계에서 N 측 엔티티
 * - 이미지 파일의 '원본명/저장명/노출 URL/대표 여부' 같은 메타 정보를 저장한다.
 */
@Entity
// JPA 엔티티로 선언 (DB row와 매핑되는 영속 객체)
@Table(name = "product_image")
// 매핑될 테이블명 지정: product_image
@Getter @Setter
// 모든 필드에 getter/setter 생성
// ※ 엔티티에서 Setter 전체 오픈은 도메인 규칙 깨질 수 있어 주의
@NoArgsConstructor
// JPA용 기본 생성자 (권장: protected)
@AllArgsConstructor
// 전체 필드 생성자 (엔티티에선 id 포함이라 위험할 수 있음)
@Builder
// 빌더 패턴으로 객체 생성 지원
// ※ 엔티티에 Builder는 쓰되 "생성 규칙"을 강제하는 방식이 더 안전(아래에서 예시)
public class ProductImage {

    /**
     * PK
     * - product_image 테이블의 기본키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // DB AUTO_INCREMENT 기반 (INSERT 후 id 확정)
    @Column(name = "product_image_id")
    private Long id;

    /**
     * 연관관계: ProductImage(N) -> Product(1)
     * - 한 상품(Product)은 여러 이미지(ProductImage)를 가질 수 있음
     */
    @ManyToOne(fetch = FetchType.LAZY)
    // ManyToOne의 기본 fetch는 EAGER인데, 실무에서는 대체로 LAZY 권장
    // LAZY: productImage.getProduct()를 실제로 호출할 때 Product를 조회(프록시) -> 불필요한 쿼리 줄임
    @JoinColumn(name = "product_id", nullable = false)
    // 외래키(FK) 컬럼명: product_id
    // nullable=false => 이미지 레코드는 반드시 어떤 Product에 속해야 함(고아 이미지 방지)
    @ToString.Exclude
    // Lombok @ToString에서 product 필드 제외
    // 이유: 연관관계 필드는 순환참조/지연로딩 프록시 접근으로 toString 시 위험(무한루프, 쿼리 발생 등)
    private Product product;

    /**
     * 원본 파일명
     * - 사용자가 업로드한 실제 파일 이름
     * - 예) "닭가슴살.jpg"
     */
    @Column(nullable = false, length = 200)
    private String originalName;

    /**
     * 저장 파일명
     * - 서버/스토리지에 저장할 때 사용하는 파일명(보통 UUID 등으로 중복 방지)
     * - 예) "a1b2c3d4-....-e9f0.jpg"
     */
    @Column(nullable = false, length = 200)
    private String storedName;

    /**
     * 노출 URL
     * - 화면에서 접근 가능한 이미지 경로
     * - 예) "/images/a1b2c3.jpg" 또는 CDN URL
     */
    @Column(nullable = false, length = 300)
    private String imgUrl;

    /**
     * 대표 이미지 여부
     * - true: 상품의 대표 이미지
     * - false: 일반 이미지
     *
     * ※ boolean은 DB에서 tinyint(1) 등으로 매핑되는 경우가 많음
     * ※ 보통은 repYn(대표여부) "Y/N" 문자열보다 boolean이 다루기 편함
     */
    @Column(nullable = false)
    private boolean repYn;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", length = 20)
    private ProductImageType imageType;
}
