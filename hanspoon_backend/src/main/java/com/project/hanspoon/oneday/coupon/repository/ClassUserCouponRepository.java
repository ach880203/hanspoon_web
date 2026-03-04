package com.project.hanspoon.oneday.coupon.repository;

import com.project.hanspoon.oneday.coupon.entity.ClassUserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ClassUserCouponRepository extends JpaRepository<ClassUserCoupon, Long> {
    List<ClassUserCoupon> findByUserId(Long userId);

    boolean existsByReservationId(Long reservationId);

    List<ClassUserCoupon> findAllByUserIdOrderByIssuedAtDesc(Long userId);

    List<ClassUserCoupon> findAllByUserIdAndUsedAtIsNullAndExpiresAtAfterOrderByIssuedAtDesc(Long userId, LocalDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from ClassUserCoupon uc
            where uc.userId = :userId
              and (uc.usedAt is not null or uc.expiresAt <= :now)
            """)
    int deleteUnusableCouponsByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}
