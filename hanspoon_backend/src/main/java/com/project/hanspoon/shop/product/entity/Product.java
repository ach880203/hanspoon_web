package com.project.hanspoon.shop.product.entity;

import com.project.hanspoon.shop.constant.ProductCategory;
import jakarta.persistence.*;
import lombok.*;

/**
 * Product 엔티티
 * - DB의 product 테이블 1행(row) == Product 객체 1개
 * - “상품” 도메인의 가장 핵심 데이터를 JPA로 영속화(저장/조회/수정/삭제)하기 위한 클래스
 */
@Entity
// 이 클래스가 JPA 엔티티임을 선언 (JPA가 관리하는 영속 객체가 됨)
@Table(name = "product")
// 매핑될 테이블 이름을 명시 (기본은 클래스명 Product -> product 로 추론될 수 있지만, 명확히 지정하는 습관이 좋음)
@Getter @Setter
// Lombok: 모든 필드에 getter/setter 생성
// ※ 엔티티에 Setter를 전체 오픈하면 “아무 데서나 값 변경”이 가능해져서 유지보수/버그 추적이 어려워질 수 있음
@ToString
// Lombok: toString() 자동 생성
// ※ 추후 연관관계(@ManyToOne 등)가 생기면 지연로딩 필드 호출/순환참조 위험이 커질 수 있음
@AllArgsConstructor
// Lombok: 모든 필드를 받는 생성자 생성
// ※ 엔티티는 보통 “전체 필드 생성자”를 외부에 노출하지 않는 편이 안전함
@NoArgsConstructor
// Lombok: 파라미터 없는 기본 생성자 생성
// JPA는 프록시/리플렉션을 위해 기본 생성자가 필요함(권장 접근제어자: protected)
public class Product {

    /**
     * PK (Primary Key)
     * - product 테이블의 기본키
     * - 엔티티 식별자(영속성 컨텍스트에서 동일성 비교 기준)
     */
    @Id
    // 이 필드가 PK임을 명시
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // PK 생성 전략: IDENTITY
    // - DB에서 AUTO_INCREMENT로 생성
    // - INSERT 이후에 id가 확정되는 방식(영속성 컨텍스트 동작/배치 insert 등에 영향 가능)
    @Column(name = "product_id")
    // 컬럼명 명시: product_id
    private Long id;

    /**
     * 상품 카테고리
     * - enum(ProductCategory) 타입을 DB에 저장하기 위한 필드
     */
    @Enumerated(EnumType.STRING)
    // enum 저장 방식
    // - STRING: "FOOD", "DRINK" 같은 “문자열 이름”으로 저장 (권장)
    // - ORDINAL(기본값): 0,1,2 같은 “순서 숫자”로 저장 -> enum 순서 바뀌면 데이터 망가질 위험 큼
    @Column(nullable = false, length = 30)
    // NOT NULL 제약 + 문자열 길이 제한(STRING 저장이므로 varchar(30) 같은 형태로 매핑될 가능성이 큼)
    private ProductCategory category;

    /**
     * 상품명
     */
    @Column(nullable = false, length = 100)
    // NOT NULL 제약 + 최대 길이 100 (DB에서 varchar(100) 등으로 생성/매핑)
    private String name;

    /**
     * 가격
     * - 현재 int로 저장: “원화 정수” 같은 단위로 다루기 쉬움
     * - 소수점이 필요한 화폐라면 BigDecimal이 더 안전할 수 있음
     */
    @Column(nullable = false)
    // NOT NULL 제약
    private int price;

    /**
     * 재고 수량
     * - 상품 재고 관리의 핵심 값
     */
    @Column(nullable = false)
    // NOT NULL 제약
    private int stock;

    @Lob
    @Column(name = "detail_content", columnDefinition = "MEDIUMTEXT")
    private String detailContent;
}
