package com.project.hanspoon.oneday.reservation.dto;

import com.project.hanspoon.oneday.reservation.domain.ReservationStatus;
import com.project.hanspoon.oneday.reservation.entity.ClassReservation;

import java.time.LocalDateTime;

public record ReservationResponse(
        Long id,
        Long sessionId,
        Long userId,
        ReservationStatus status,
        LocalDateTime holdExpiredAt
) {
    public static ReservationResponse from(ClassReservation r) {
        return new ReservationResponse(
                r.getId(),
                r.getSession().getId(),
                r.getUser().getUserId(),
                r.getStatus(),
                r.getHoldExpiredAt()
        );
    }
}
