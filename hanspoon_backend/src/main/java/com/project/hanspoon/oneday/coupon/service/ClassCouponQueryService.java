package com.project.hanspoon.oneday.coupon.service;

import com.project.hanspoon.common.exception.BusinessException;
import com.project.hanspoon.oneday.coupon.dto.ClassUserCouponResponse;
import com.project.hanspoon.oneday.coupon.repository.ClassUserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClassCouponQueryService {

    private final ClassUserCouponRepository userCouponRepository;

    @Transactional
    public List<ClassUserCouponResponse> myCoupons(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException("로그인 정보가 올바르지 않습니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        // "사용 불가 쿠폰 자동 정리" 요구사항 반영:
        // 조회 시점에 만료/사용완료 쿠폰을 DB에서 정리해, 화면에 usable 쿠폰만 남도록 유지합니다.
        userCouponRepository.deleteUnusableCouponsByUserId(userId, now);

        return userCouponRepository.findAllByUserIdAndUsedAtIsNullAndExpiresAtAfterOrderByIssuedAtDesc(userId, now)
                .stream()
                .map(uc -> new ClassUserCouponResponse(
                        uc.getId(),
                        uc.getCoupon().getId(),
                        uc.getCoupon().getName(),
                        uc.getCoupon().getDiscountType(),
                        uc.getCoupon().getDiscountValue(),
                        uc.getIssuedAt(),
                        uc.getExpiresAt(),
                        uc.isUsable(now)
                ))
                .toList();
    }
}
