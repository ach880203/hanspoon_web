package com.project.hanspoon.oneday.coupon.repository;

import com.project.hanspoon.oneday.coupon.entity.ClassCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClassCouponRepository extends JpaRepository<ClassCoupon, Long> {
    Optional<ClassCoupon> findByName(String name);

    Optional<ClassCoupon> findFirstByActiveTrueOrderByIdAsc();
}
