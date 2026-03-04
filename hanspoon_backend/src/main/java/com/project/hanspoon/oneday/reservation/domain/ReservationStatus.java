package com.project.hanspoon.oneday.reservation.domain;

public enum ReservationStatus {
    HOLD("예약 대기"),
    PAID("예약 확정"),
    CANCEL_REQUESTED("취소 요청"),
    CANCELED("예약 취소"),
    EXPIRED("기간 만료"),
    COMPLETED("수강 완료");

    private final String description;

    ReservationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
