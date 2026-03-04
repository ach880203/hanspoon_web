package com.project.hanspoon.mypage.controller;

import com.project.hanspoon.common.response.ApiResponse;
import com.project.hanspoon.common.dto.PageResponse;
import com.project.hanspoon.common.security.CustomUserDetails;
import com.project.hanspoon.mypage.dto.PointHistoryDto;
import com.project.hanspoon.mypage.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mypage/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PointHistoryDto>>> getPointHistories(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<PointHistoryDto> histories = pointService.getPointHistories(userDetails.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(histories)));
    }

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<Integer>> getPointBalance(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        int balance = pointService.getPointBalance(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(balance));
    }
}
