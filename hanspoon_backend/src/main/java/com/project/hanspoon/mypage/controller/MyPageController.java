package com.project.hanspoon.mypage.controller;

import com.project.hanspoon.common.response.ApiResponse;
import com.project.hanspoon.common.security.CustomUserDetails;
import com.project.hanspoon.mypage.dto.MyPageSummaryDto;
import com.project.hanspoon.mypage.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<MyPageSummaryDto>> getSummary(@AuthenticationPrincipal CustomUserDetails userDetails) {
        MyPageSummaryDto summary = myPageService.getMyPageSummary(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }
}
