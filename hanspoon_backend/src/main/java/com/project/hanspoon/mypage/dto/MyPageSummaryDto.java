package com.project.hanspoon.mypage.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyPageSummaryDto {
    private Long userId;
    private String userName;
    private String email;
    private int spoonBalance;
    private int activeOrderCount; // 진행 중인 주문 (입금대기~배송중)
    private int upcomingClassCount; // 다가오는 클래스
    private long unreadNotificationCount; // 읽지 않은 알림
}
