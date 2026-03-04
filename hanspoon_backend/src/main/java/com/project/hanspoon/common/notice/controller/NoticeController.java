package com.project.hanspoon.common.notice.controller;

import com.project.hanspoon.common.response.ApiResponse;
import com.project.hanspoon.common.notice.dto.NoticeDto;
import com.project.hanspoon.common.dto.PageResponse;
import com.project.hanspoon.common.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 공지사항 REST API Controller (사용자)
 */
@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    /**
     * 목록 조회
     * GET /api/notice/list?page=0&size=10&keyword=검색어
     */
    @GetMapping({ "", "/list" })
    public ResponseEntity<ApiResponse<PageResponse<NoticeDto>>> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<NoticeDto> notices;
        if (keyword != null && !keyword.isEmpty()) {
            notices = noticeService.searchNotices(keyword, pageable);
        } else {
            notices = noticeService.getNoticeList(pageable);
        }
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(notices)));
    }

    /**
     * 상세 조회
     * GET /api/notice/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NoticeDto>> view(@PathVariable("id") Long noticeId) {
        try {
            NoticeDto notice = noticeService.getNotice(noticeId);
            return ResponseEntity.ok(ApiResponse.ok(notice));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("공지사항을 찾을 수 없습니다."));
        }
    }
}
