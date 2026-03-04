package com.project.hanspoon.common.notice.controller;

import com.project.hanspoon.common.response.ApiResponse;
import com.project.hanspoon.common.notice.dto.NoticeDto;
import com.project.hanspoon.common.dto.PageResponse;
import com.project.hanspoon.common.notice.service.NoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자용 공지사항 REST API Controller
 */
@RestController
@RequestMapping("/api/admin/notice")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminNoticeController {

    private final NoticeService noticeService;

    /**
     * 목록 조회
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<PageResponse<NoticeDto>>> list(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<NoticeDto> notices = noticeService.getNoticeList(pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(notices)));
    }

    /**
     * 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NoticeDto>> get(@PathVariable("id") Long noticeId) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(noticeService.getNotice(noticeId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("공지사항을 찾을 수 없습니다."));
        }
    }

    /**
     * 등록
     */
    @PostMapping
    public ResponseEntity<ApiResponse<NoticeDto>> create(@Valid @RequestBody NoticeDto dto) {
        try {
            return ResponseEntity.ok(ApiResponse.ok("공지사항이 등록되었습니다.", noticeService.createNotice(dto)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("등록에 실패했습니다."));
        }
    }

    /**
     * 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NoticeDto>> update(@PathVariable("id") Long noticeId,
            @Valid @RequestBody NoticeDto dto) {
        try {
            return ResponseEntity.ok(ApiResponse.ok("공지사항이 수정되었습니다.", noticeService.updateNotice(noticeId, dto)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("수정에 실패했습니다."));
        }
    }

    /**
     * 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long noticeId) {
        try {
            noticeService.deleteNotice(noticeId);
            return ResponseEntity.ok(ApiResponse.ok("공지사항이 삭제되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("삭제에 실패했습니다."));
        }
    }
}
