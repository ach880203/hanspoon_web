package com.project.hanspoon.oneday.completion.service;

import com.project.hanspoon.oneday.coupon.repository.ClassCouponRepository;
import com.project.hanspoon.oneday.coupon.repository.ClassUserCouponRepository;
import com.project.hanspoon.oneday.coupon.entity.ClassUserCoupon;
import com.project.hanspoon.oneday.reservation.domain.ReservationStatus;
import com.project.hanspoon.oneday.reservation.repository.ClassReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClassCompletionService {
    private static final int COMPLETION_COUPON_VALID_MONTHS = 6;

    private final ClassReservationRepository reservationRepository;
    private final ClassCouponRepository couponRepository;
    private final ClassUserCouponRepository userCouponRepository;

    @Transactional
    public int completeAndIssueCoupons(LocalDateTime now) {
        var targets = reservationRepository.findPaidToComplete(ReservationStatus.PAID, now);
        if (targets.isEmpty()) return 0;

        // 활성 쿠폰 1개를 “수강 완료 보상 쿠폰”으로 사용 (나중에 정책 바꾸기 쉬움)
        var coupon = couponRepository.findFirstByActiveTrueOrderByIdAsc().orElse(null);

        int count = 0;

        for (var r : targets) {
            if (r.getStatus() != ReservationStatus.PAID) continue;

            r.markCompleted(now);
            count++;

            if (coupon == null) continue;

            // reservationId로 중복 발급 방지(유니크 + exists로 2중 안전)
            if (userCouponRepository.existsByReservationId(r.getId())) continue;

            var issued = ClassUserCoupon.issueForMonths(
                    r.getUser().getUserId(),
                    coupon,
                    r.getId(),
                    now,
                    COMPLETION_COUPON_VALID_MONTHS
            );
            userCouponRepository.save(issued);
        }

        log.info("Completed reservations processed. count={}, now={}", count, now);
        return count;
    }
}
