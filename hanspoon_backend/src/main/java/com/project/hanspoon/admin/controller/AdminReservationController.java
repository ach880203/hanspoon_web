package com.project.hanspoon.admin.controller;

import com.project.hanspoon.admin.dto.AdminReservationItemDto;
import com.project.hanspoon.admin.service.AdminReservationService;
import com.project.hanspoon.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/admin/reservations", "/api/admin/reservation"})
public class AdminReservationController {

    private final AdminReservationService adminReservationService;

    @GetMapping
    public ApiResponse<List<AdminReservationItemDto>> getReservations(
            @RequestParam(required = false) String status
    ) {
        return ApiResponse.ok(adminReservationService.getReservations(status));
    }

    @GetMapping({"/cancel-requests", "/cancel_requests", "/cancelRequests"})
    public ApiResponse<List<AdminReservationItemDto>> getCancelRequests() {
        return ApiResponse.ok(adminReservationService.getCancelRequests());
    }

    @PostMapping({"/{reservationId}/approve-cancel", "/{reservationId}/approve_cancel", "/{reservationId}/approveCancel"})
    public ApiResponse<Void> approveCancel(@PathVariable Long reservationId) {
        adminReservationService.approveCancel(reservationId);
        return ApiResponse.ok("취소 승인 처리되었습니다.", null);
    }

    @PostMapping({"/{reservationId}/reject-cancel", "/{reservationId}/reject_cancel", "/{reservationId}/rejectCancel"})
    public ApiResponse<Void> rejectCancel(@PathVariable Long reservationId) {
        adminReservationService.rejectCancel(reservationId);
        return ApiResponse.ok("취소 거절 처리되었습니다.", null);
    }
}
