package com.project.hanspoon.mypage.service;

import com.project.hanspoon.common.user.entity.User;
import com.project.hanspoon.common.user.repository.NotificationRepository;
import com.project.hanspoon.common.user.repository.UserRepository;
import com.project.hanspoon.mypage.dto.MyPageSummaryDto;
import com.project.hanspoon.oneday.reservation.repository.ClassReservationRepository;
import com.project.hanspoon.shop.constant.OrderStatus;
import com.project.hanspoon.shop.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ClassReservationRepository reservationRepository;
    private final NotificationRepository notificationRepository;

    public MyPageSummaryDto getMyPageSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 진행 중인 주문 수 (PAID, CREATED, SHIPPED)
        // OrderStatus가 ENUM이므로 List로 전달
        int activeOrderCount = orderRepository.countByUser_UserIdAndStatusIn(userId, 
                List.of(OrderStatus.PAID, OrderStatus.CREATED, OrderStatus.SHIPPED));

        // 다가오는 클래스 수 (예약 확정이고, 시작 시간이 현재보다 미래인 것)
        // Repository에 메서드 추가 필요할 수 있음. 일단 countByUserIdAndStatusAndSessionStartAtAfter 같은 로직 가정
        // 현재 ClassReservationRepository에는 복잡한 쿼리가 없으므로 일단 0으로 처리하거나 Repository 확장 필요
        int upcomingClassCount = 0; 
        // TODO: ClassReservationRepository에 countUpcomingReservations 메서드 추가 후 구현

        long unreadNotiCount = notificationRepository.countByUserUserIdAndIsReadFalse(userId);

        return MyPageSummaryDto.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .spoonBalance(user.getSpoonBalance())
                .activeOrderCount(activeOrderCount)
                .upcomingClassCount(upcomingClassCount)
                .unreadNotificationCount(unreadNotiCount)
                .build();
    }
}
