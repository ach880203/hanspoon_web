package com.project.hanspoon.admin.dto;

import java.time.LocalDateTime;

/**
 * 관리자 예약 목록 카드 1건을 표현하는 DTO입니다.
 *
 * 프론트(AdminReservationList.jsx)에서 바로 사용할 수 있도록
 * 화면에서 쓰는 필드명을 중심으로 구성했습니다.
 * - reservationId, status, classTitle, price, createdAt
 * - userName, userEmail, sessionStart, cancelRequestedAt, cancelReason
 */
public record AdminReservationItemDto(
        Long reservationId,
        String status,
        String statusCode,
        Long classId,
        Long sessionId,
        String classTitle,
        Integer price,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime canceledAt,
        Long userId,
        String userName,
        String userEmail,
        String userPhone,
        LocalDateTime sessionStart,
        LocalDateTime holdExpiredAt,
        LocalDateTime paidAt,
        boolean paymentCompleted,
        boolean couponIssued,
        LocalDateTime cancelRequestedAt,
        String cancelReason
) {
}
