package com.project.hanspoon.common.user.controller;

import com.project.hanspoon.common.response.ApiResponse;
import com.project.hanspoon.common.dto.PageResponse;
import com.project.hanspoon.common.user.dto.AdminUserDetailResponse;
import com.project.hanspoon.common.user.entity.User;
import com.project.hanspoon.common.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자용 사용자 관리 Controller
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    /**
     * 전체 사용자 목록 조회 (페이지네이션)
     * GET /api/admin/users/list
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<PageResponse<User>>> getUserList(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<User> users = userService.findAll(search, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(users)));
    }

    /**
     * 사용자 상세 정보 조회
     * GET /api/admin/users/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<AdminUserDetailResponse>> getUserDetail(@PathVariable Long userId) {
        AdminUserDetailResponse response = userService.getAdminUserDetail(userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 사용자 상태 변경 (정지, 활성화 등)
     * POST /api/admin/users/{userId}/status
     */
    @PostMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable Long userId,
            @RequestParam com.project.hanspoon.common.user.constant.UserStatus status) {
        userService.updateStatus(userId, status);
        return ResponseEntity.ok(ApiResponse.ok("사용자 상태가 변경되었습니다."));
    }

    /**
     * 사용자 활동 이력 조회
     * GET /api/admin/users/{userId}/history
     */
    @GetMapping("/{userId}/history")
    public ResponseEntity<ApiResponse<com.project.hanspoon.common.user.dto.UserHistoryDto>> getUserHistory(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getUserHistory(userId)));
    }
}
