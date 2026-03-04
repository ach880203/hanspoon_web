package com.project.hanspoon.shop.order.repository;

import com.project.hanspoon.shop.order.entity.Order;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {


    List<Order> findByCartId(Long cartId);

    @Query("select o from Order o where o.user.userId = :userId order by o.createdAt desc")
    List<Order> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    Page<Order> findByUser_UserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // ✅ 주문 상세(내 주문만) + items fetch
    @Query("""
        select distinct o from Order o
        left join fetch o.items
        where o.id = :orderId and o.user.userId = :userId
    """)
    Optional<Order> findWithItemsByIdAndUserId(@Param("orderId") Long orderId,
                                               @Param("userId") Long userId);

    // ✅ 상태 변경용 잠금 조회(내 주문만)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select distinct o from Order o
        left join fetch o.items
        where o.id = :orderId and o.user.userId = :userId
    """)
    Optional<Order> findWithItemsByIdAndUserIdForUpdate(@Param("orderId") Long orderId,
                                                        @Param("userId") Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select distinct o from Order o
        left join fetch o.items
        where o.id = :orderId
    """)
    Optional<Order> findWithItemsByIdForUpdate(@Param("orderId") Long orderId);

    @Query("""
        select distinct o from Order o
        left join fetch o.items
        where o.id = :orderId
    """)
    Optional<Order> findWithItemsById(@Param("orderId") Long orderId);

    // ✅ 기간 및 상태 필터링 (마이페이지용)
    @Query("""
        SELECT o FROM Order o 
        WHERE o.user.userId = :userId 
        AND o.status <> com.project.hanspoon.shop.constant.OrderStatus.CREATED
        AND (:startDate IS NULL OR o.createdAt >= :startDate) 
        AND (:endDate IS NULL OR o.createdAt <= :endDate) 
        AND (:status IS NULL OR o.status = :status)
        ORDER BY o.createdAt DESC
    """)
    Page<Order> findWithFilters(@Param("userId") Long userId, 
                                @Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate, 
                                @Param("status") com.project.hanspoon.shop.constant.OrderStatus status,
                                Pageable pageable);

    @Query("""
        SELECT o FROM Order o
        WHERE o.status <> com.project.hanspoon.shop.constant.OrderStatus.CREATED
        AND (:status IS NULL OR o.status = :status)
        AND (
          :keyword IS NULL OR :keyword = '' OR
          CONCAT('', o.id) LIKE CONCAT('%', :keyword, '%') OR
          o.receiverName LIKE CONCAT('%', :keyword, '%') OR
          o.receiverPhone LIKE CONCAT('%', :keyword, '%') OR
          o.trackingNumber LIKE CONCAT('%', :keyword, '%')
        )
        ORDER BY o.createdAt DESC
    """)
    Page<Order> findAllWithFilters(@Param("status") com.project.hanspoon.shop.constant.OrderStatus status,
                                   @Param("keyword") String keyword,
                                   Pageable pageable);

    long countByStatus(com.project.hanspoon.shop.constant.OrderStatus status);

    long countByStatusIn(java.util.List<com.project.hanspoon.shop.constant.OrderStatus> statuses);

    int countByUser_UserIdAndStatusIn(Long userId, java.util.List<com.project.hanspoon.shop.constant.OrderStatus> statuses);
}
