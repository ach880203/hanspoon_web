package com.project.hanspoon.shop.review.repository;

import com.project.hanspoon.shop.review.entity.RevProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RevProductRepository extends JpaRepository<RevProduct, Long> {

    Page<RevProduct> findByProduct_IdOrderByIdDesc(Long productId, Pageable pageable);

    Page<RevProduct> findByUser_UserIdOrderByIdDesc(Long userId, Pageable pageable);

    Optional<RevProduct> findByIdAndUser_UserId(Long revId, Long userId);

    // ✅ (중요) countQuery 명시해서 validation 실패 방지
    @Query(
            value = """
    select r from RevProduct r
    where r.product.id = :productId
      and (:rating is null or r.rating = :rating)
      and (:keyword is null or r.content like concat('%', :keyword, '%'))
    """,
            countQuery = """
    select count(r) from RevProduct r
    where r.product.id = :productId
      and (:rating is null or r.rating = :rating)
      and (:keyword is null or r.content like concat('%', :keyword, '%'))
    """
    )
    Page<RevProduct> searchByProduct(
            @Param("productId") Long productId,
            @Param("rating") Integer rating,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // (summary용) 아래는 네가 이미 추가하려던 것 그대로 두면 됨
    long countByProduct_Id(Long productId);

    @Query("select avg(r.rating) from RevProduct r where r.product.id = :productId")
    Double avgRatingByProduct(@Param("productId") Long productId);

    @Query("select r.rating, count(r) from RevProduct r where r.product.id = :productId group by r.rating")
    List<Object[]> countGroupByRating(@Param("productId") Long productId);
}