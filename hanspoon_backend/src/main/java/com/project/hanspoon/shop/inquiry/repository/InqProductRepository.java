package com.project.hanspoon.shop.inquiry.repository;

import com.project.hanspoon.shop.inquiry.entity.InqProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InqProductRepository extends JpaRepository<InqProduct, Long> {

    Page<InqProduct> findByProduct_IdOrderByIdDesc(Long productId, Pageable pageable);

    Page<InqProduct> findAllByOrderByIdDesc(Pageable pageable);

    Page<InqProduct> findByUser_UserIdOrderByIdDesc(Long userId, Pageable pageable);

    Optional<InqProduct> findByIdAndUser_UserId(Long inqId, Long userId);

    long countByAnsweredYnFalse();
}
