package com.project.hanspoon.oneday.reservation.controller;

import com.project.hanspoon.common.response.ApiResponse;
import com.project.hanspoon.common.security.CustomUserDetails;
import com.project.hanspoon.common.exception.BusinessException;
import com.project.hanspoon.oneday.reservation.dto.ReservationCancelRequest;
import com.project.hanspoon.oneday.reservation.dto.ReservationResponse;
import com.project.hanspoon.oneday.reservation.service.ClassReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ClassReservationController {

    private final ClassReservationService reservationService;

    @PostMapping({"/api/oneday/sessions/{sessionId}/reservations", "/api/oneday/session/{sessionId}/reservations"})
    public ApiResponse<ReservationResponse> createHold(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.ok("홀드 예약이 생성되었습니다.", reservationService.createHold(sessionId, resolveUserId(userDetails)));
    }

    // @PostMapping("/api/oneday/reservations/{reservationId}/pay")
    // public ApiResponse<ReservationResponse> pay(
    // @PathVariable Long reservationId,
    // @AuthenticationPrincipal CustomUserDetails userDetails
    // ) {
    // return ApiResponse.ok("예약 결제가 완료되었습니다.",
    // reservationService.pay(reservationId, resolveUserId(userDetails)));
    // }

    @PostMapping({"/api/oneday/reservations/{reservationId}/cancel", "/api/mypage/reservations/{reservationId}/cancel"})
    public ApiResponse<ReservationResponse> cancel(
            @PathVariable Long reservationId,
            @RequestBody(required = false) ReservationCancelRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.ok(
                "취소 요청이 접수되었습니다.",
                reservationService.cancel(reservationId, resolveUserId(userDetails), request != null ? request.cancelReason() : null)
        );
    }

    private Long resolveUserId(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getUserId() == null) {
            throw new BusinessException("로그인 정보가 필요합니다.");
        }
        return userDetails.getUserId();
    }
}
