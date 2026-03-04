package com.project.hanspoon.oneday.reservation.scheduler;

import com.project.hanspoon.oneday.reservation.service.ReservationExpireService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ReservationExpireScheduler {

    private final ReservationExpireService expireService;

    // 매 1분마다 만료 처리 (개발/운영 모두 무난)
    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void expireHolds() {
        expireService.expireHolds(LocalDateTime.now());
    }
}
