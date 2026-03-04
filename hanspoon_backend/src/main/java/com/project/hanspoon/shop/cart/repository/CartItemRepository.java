package com.project.hanspoon.shop.cart.repository;

import com.project.hanspoon.shop.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCart_IdOrderByIdDesc(Long cartId);

    Optional<CartItem> findByCart_IdAndProduct_Id(Long cartId, Long productId);

    Optional<CartItem> findByIdAndCart_Id(Long itemId, Long cartId);
}
