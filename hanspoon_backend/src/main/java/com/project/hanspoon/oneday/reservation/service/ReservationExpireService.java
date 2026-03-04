package com.project.hanspoon.oneday.reservation.service;

import com.project.hanspoon.oneday.clazz.entity.ClassSession;
import com.project.hanspoon.oneday.clazz.repository.ClassSessionRepository;
import com.project.hanspoon.oneday.reservation.domain.ReservationStatus;
import com.project.hanspoon.oneday.reservation.repository.ClassReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationExpireService {

    private final ClassReservationRepository reservationRepository;
    private final ClassSessionRepository sessionRepository;

    /**
     * 만료된 HOLD를 EXPIRED 처리하고 좌석을 복구한다.
     */
    @Transactional
    public int expireHolds(LocalDateTime now) {
        var targets = reservationRepository.findExpiredHolds(ReservationStatus.HOLD, now);
        if (targets.isEmpty()) return 0;

        // 세션별로 몇 개 만료되는지 모아서 좌석 복구를 한 번에 처리할 수도 있음
        // (지금은 이해를 위해 직관적인 방식으로)
        int count = 0;

        for (var r : targets) {
            // 이미 상태가 바뀐 경우 방어 (동시 실행 대비)
            if (r.getStatus() != ReservationStatus.HOLD) continue;

            // 좌석 복구: 세션 락 잡고 감소
            Long sessionId = r.getSession().getId();
            ClassSession session = sessionRepository.findByIdForUpdate(sessionId)
                    .orElseThrow(() -> new IllegalStateException("세션을 찾을 수 없습니다. id=" + sessionId));

            session.decreaseReserved();
            r.markExpired(now);

            log.debug("Expired reservation id={}, sessionId={}", r.getId(), sessionId);

            count++;
        }

        log.info("Expired holds processed. count={}, now={}", count, now);
        return count;
    }
}
