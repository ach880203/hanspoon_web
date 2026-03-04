package com.project.hanspoon.oneday.reservation.dto;

import java.time.LocalDateTime;

public record ReservationDetailResponse(
        Long reservationId,
        String status,
        LocalDateTime holdExpiredAt,
        LocalDateTime paidAt,
        LocalDateTime canceledAt,

        Long sessionId,
        LocalDateTime startAt,
        String slot,
        int capacity,
        int reservedCount,
        int price,

        Long classId,
        String classTitle
) {
}
