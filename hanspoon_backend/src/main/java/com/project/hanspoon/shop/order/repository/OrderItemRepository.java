package com.project.hanspoon.shop.order.repository;

import com.project.hanspoon.shop.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // 여러 주문의 아이템을 한번에 가져오기 (orderId별로 정렬)
    List<OrderItem> findByOrder_IdInOrderByOrder_IdAscIdAsc(List<Long> orderIds);
}
