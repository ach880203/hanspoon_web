package com.project.hanspoon.shop.wish.repository;

import com.project.hanspoon.shop.wish.entity.WishProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishProductRepository extends JpaRepository<WishProduct, Long> {

    boolean existsByUser_UserIdAndProduct_Id(Long userId, Long productId);

    Optional<WishProduct> findByUser_UserIdAndProduct_Id(Long userId, Long productId);

    Page<WishProduct> findByUser_UserIdOrderByIdDesc(Long userId, Pageable pageable);

    List<WishProduct> findByUser_UserIdOrderByIdDesc(Long userId);

    long countByProduct_Id(Long productId);
}
