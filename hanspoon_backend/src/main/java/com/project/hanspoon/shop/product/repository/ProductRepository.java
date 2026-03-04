package com.project.hanspoon.shop.product.repository;

import com.project.hanspoon.shop.product.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * ProductRepository
 * - Product 엔티티에 대한 데이터 접근 계층(Repository)
 *
 * 확장한 인터페이스:
 * 1) JpaRepository<Product, Long>
 *    - 기본 CRUD 제공 (save, findById, findAll, deleteById 등)
 *    - 페이징/정렬(findAll(Pageable))도 포함
 *
 * 2) JpaSpecificationExecutor<Product>
 *    - Specification 기반 “동적 검색 조건”을 적용한 조회를 쉽게 할 수 있게 해줌
 *    - 예) 카테고리/키워드/가격 범위 같은 조건을 조합해서 where절을 동적으로 구성 가능
 */
public interface ProductRepository
        extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    /**
     * 비관적 락(Pessimistic Write Lock)을 걸고 상품을 조회하는 메서드
     *
     * 목적:
     * - “동시에 여러 트랜잭션이 같은 상품을 수정”하는 상황에서 데이터 꼬임 방지
     * - 대표 케이스: 재고(stock) 차감/증가, 주문 처리, 동시 결제 등
     *
     * 동작:
     * - DB 수준에서 해당 row에 쓰기 락을 잡음
     * - 보통(MySQL/InnoDB 기준) SELECT ... FOR UPDATE 로 실행됨
     * - 락을 건 트랜잭션이 끝날 때(커밋/롤백)까지 다른 트랜잭션의 수정이 대기(또는 타임아웃)
     *
     * 사용 조건:
     * - 반드시 @Transactional 범위 안에서 호출해야 의미가 있음
     *   (트랜잭션이 없으면 락이 즉시 해제되거나, 기대한 보호가 안 됨)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    // JPA Lock 모드 설정: PESSIMISTIC_WRITE (쓰기 락)
    @Query("select p from Product p where p.id = :id")
    // JPQL로 id를 기준으로 Product 1건 조회
    Optional<Product> findByIdForUpdate(@Param("id") Long id);
    // @Param으로 JPQL의 :id 바인딩
    // Optional: 없을 수도 있음을 표현 (없으면 Optional.empty())
}
