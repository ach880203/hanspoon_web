package com.project.hanspoon.common.config;

import com.project.hanspoon.common.user.constant.UserStatus;
import com.project.hanspoon.common.user.entity.User;
import com.project.hanspoon.common.user.repository.UserRepository;
import com.project.hanspoon.oneday.coupon.domain.DiscountType;
import com.project.hanspoon.oneday.coupon.entity.ClassCoupon;
import com.project.hanspoon.oneday.coupon.entity.ClassUserCoupon;
import com.project.hanspoon.oneday.coupon.repository.ClassCouponRepository;
import com.project.hanspoon.oneday.coupon.repository.ClassUserCouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    //사용자 조회/저장
    private final UserRepository userRepository;
    //쿠폰 마스터 조회/저장
    private final ClassCouponRepository couponRepository;
    //사용자 쿠폰 발급 관리
    private final ClassUserCouponRepository userCouponRepository;
    //비밀번호 암호화
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        //관리자 계정 생성 or 업데이트
        User admin = userRepository.findByEmail("admin@example.com")
                .map(existingAdmin -> {
                    existingAdmin.setRole("ROLE_ADMIN");
                    existingAdmin.setStatus(UserStatus.ACTIVE);
                    existingAdmin.setSpoonCount(10000);
                    return userRepository.save(existingAdmin);
                })
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("admin@example.com")
                        .password(passwordEncoder.encode("admin1234"))
                        .userName("관리자")
                        .status(UserStatus.ACTIVE)
                        .role("ROLE_ADMIN")
                        .spoonCount(10000)
                        .isDeleted(false)
                        .build()));
        log.info("관리자 계정 준비 완료: admin@example.com / spoon=10000");

        // In some local DBs, legacy class_coupon schema has mixed timestamp columns.
        // Do not fail app startup if coupon seed insert hits that schema mismatch.
        try {
            ClassCoupon adminCoupon = couponRepository.findByName("관리자 테스트용 10% 할인")
                    .orElseGet(() -> couponRepository.save(ClassCoupon.builder()
                            .name("관리자 테스트용 10% 할인")
                            .discountType(DiscountType.PERCENT)
                            .discountValue(10)
                            .validDays(30)
                            .active(true)
                            .build()));

            boolean hasCoupon = userCouponRepository.findByUserId(admin.getUserId()).stream()
                    .anyMatch(uc -> uc.getCoupon().getId().equals(adminCoupon.getId()) && uc.getUsedAt() == null);

            if (!hasCoupon) {
                ClassUserCoupon issued = ClassUserCoupon.issue(admin.getUserId(), adminCoupon, null, LocalDateTime.now());
                userCouponRepository.save(issued);
                log.info("관리자 계정에 테스트 쿠폰 지급 완료");
            }
        } catch (Exception e) {
            log.warn("쿠폰 시딩을 건너뜁니다: {}", e.getMessage());
        }
    }
}
