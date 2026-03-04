package com.project.hanspoon.oneday.reservation.controller;

import com.project.hanspoon.common.exception.BusinessException;
import com.project.hanspoon.common.response.ApiResponse;
import com.project.hanspoon.common.security.CustomUserDetails;
import com.project.hanspoon.oneday.reservation.domain.ReservationStatus;
import com.project.hanspoon.oneday.reservation.dto.ReservationDetailResponse;
import com.project.hanspoon.oneday.reservation.dto.ReservationListItemResponse;
import com.project.hanspoon.oneday.reservation.service.ReservationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/oneday/reservations", "/api/mypage/reservations", "/api/mypage/class-reservations"})
public class ReservationQueryController {

    private final ReservationQueryService queryService;

    @GetMapping
    public ApiResponse<Page<ReservationListItemResponse>> myReservation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String status,
            @PageableDefault(sort = "session.startAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long userId = resolveUserId(userDetails);
        LocalDateTime start = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime end = (endDate != null) ? endDate.atTime(23, 59, 59) : null;
        return ApiResponse.ok(queryService.myReservationsWithFilters(userId, start, end, parseStatus(status), pageable));
    }

    @GetMapping("/{reservationId}")
    public ApiResponse<ReservationDetailResponse> myReservationDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reservationId
    ) {
        Long userId = resolveUserId(userDetails);
        return ApiResponse.ok(queryService.myReservationDetail(userId, reservationId));
    }

    private Long resolveUserId(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getUserId() == null) {
            throw new BusinessException("로그인 정보가 필요합니다.");
        }
        return userDetails.getUserId();
    }

    private ReservationStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        String normalized = status.trim();
        for (ReservationStatus value : ReservationStatus.values()) {
            if (value.name().equalsIgnoreCase(normalized) || value.getDescription().equals(normalized)) {
                return value;
            }
        }

        throw new BusinessException("지원하지 않는 예약 상태입니다: " + status);
    }
}
