package com.project.hanspoon.oneday.reservation.dto;

/**
 * 예약 취소 요청 입력 DTO입니다.
 * 프론트에서 선택한 취소 사유/상세 입력값을 서버에 전달할 때 사용합니다.
 */
public record ReservationCancelRequest(
        String cancelReason
) {
}
