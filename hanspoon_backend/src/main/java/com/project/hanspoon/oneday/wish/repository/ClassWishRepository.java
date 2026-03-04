package com.project.hanspoon.oneday.wish.repository;

import com.project.hanspoon.oneday.wish.entity.ClassWish;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClassWishRepository extends JpaRepository<ClassWish, Long> {


    Optional<ClassWish> findByUserIdAndClassProduct_Id(Long userId, Long classProductId);
    boolean existsByUserIdAndClassProduct_Id(Long userId, Long classProductId);
    List<ClassWish> findAllByUserIdOrderByCreatedAtDesc(Long userId);
    void deleteByUserIdAndClassProduct_Id(Long userId, Long classProductId);
}
