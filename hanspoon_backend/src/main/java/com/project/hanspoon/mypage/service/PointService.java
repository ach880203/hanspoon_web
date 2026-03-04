package com.project.hanspoon.mypage.service;

import com.project.hanspoon.common.user.entity.PointHistory;
import com.project.hanspoon.common.user.repository.PointHistoryRepository;
import com.project.hanspoon.mypage.dto.PointHistoryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointService {

    private final PointHistoryRepository pointHistoryRepository;
    private final com.project.hanspoon.common.user.repository.UserRepository userRepository;

    public Page<PointHistoryDto> getPointHistories(Long userId, Pageable pageable) {
        return pointHistoryRepository.findByUserUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(PointHistoryDto::fromEntity);
    }

    @Transactional
    public void usePoints(Long userId, int amount, String description, Long referenceId) {
        if (amount <= 0)
            return;

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!user.useSpoon(amount)) {
            throw new com.project.hanspoon.common.exception.BusinessException("포인트가 부족합니다.");
        }

        PointHistory history = PointHistory.builder()
                .user(user)
                .amount(-amount)
                .type(com.project.hanspoon.common.user.constant.PointType.USE_ORDER)
                .description(description)
                .referenceId(referenceId)
                .build();

        pointHistoryRepository.save(history);
    }

    public int getPointBalance(Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return user.getSpoonBalance();
    }
}
