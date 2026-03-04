package com.project.hanspoon.oneday.coupon.seed;

import com.project.hanspoon.oneday.coupon.domain.DiscountType;
import com.project.hanspoon.oneday.coupon.entity.ClassCoupon;
import com.project.hanspoon.oneday.coupon.repository.ClassCouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClassCouponSeed implements ApplicationRunner {

    private final ClassCouponRepository couponRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // 활성 쿠폰이 하나라도 있으면 아무것도 안 함
        if (couponRepository.findFirstByActiveTrueOrderByIdAsc().isPresent()) {
            return;
        }

        // ✅ 고정 1장 쿠폰 생성
        ClassCoupon coupon = ClassCoupon.builder()
                .name("원데이 클래스 수강완료 쿠폰")
                .discountType(DiscountType.PERCENT)
                .discountValue(10)   // 10% 할인
                .validDays(7)        // 발급 후 7일 유효
                .active(true)
                .build();

        couponRepository.save(coupon);
        log.info("Seeded default coupon: {}", coupon.getName());
    }
}
