package com.project.hanspoon.oneday.clazz.dto;

import com.project.hanspoon.oneday.clazz.domain.SessionSlot;
import com.project.hanspoon.oneday.clazz.entity.ClassSession;

import java.time.LocalDateTime;
import java.time.ZoneId;

public record SessionResponse(
        Long id,
        LocalDateTime startAt,
        SessionSlot slot,
        int capacity,
        int reservedCount,
        int remainingSeats,
        boolean full,
        boolean completed,
        int price
) {
    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");

    public static SessionResponse from(ClassSession s) {
        int remaining = s.remainingSeats();
        boolean completed = !s.getStartAt().isAfter(LocalDateTime.now(KST_ZONE));
        return new SessionResponse(
                s.getId(),
                s.getStartAt(),
                s.getSlot(),
                s.getCapacity(),
                s.getReservedCount(),
                remaining,
                remaining <= 0,
                completed,
                s.getPrice()
        );
    }
}
