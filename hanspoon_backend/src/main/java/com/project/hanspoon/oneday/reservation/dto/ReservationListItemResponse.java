package com.project.hanspoon.oneday.reservation.dto;

import java.time.LocalDateTime;

public record ReservationListItemResponse(
        Long reservationId,
        String status,
        LocalDateTime holdExpiredAt,

        Long sessionId,
        LocalDateTime startAt,
        String slot,
        int price,

        Long classId,
        String classTitle
) {}
