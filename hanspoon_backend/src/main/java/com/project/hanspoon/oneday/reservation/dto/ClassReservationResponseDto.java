package com.project.hanspoon.oneday.reservation.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassReservationResponseDto {
    private Long id;
    private Long sessionId;
    private String status;
    private LocalDateTime holdExpiredAt;
    private LocalDateTime paidAt;
    private LocalDateTime canceledAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
}
