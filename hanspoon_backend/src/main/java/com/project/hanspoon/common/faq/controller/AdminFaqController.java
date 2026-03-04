package com.project.hanspoon.common.faq.controller;

import com.project.hanspoon.common.response.ApiResponse;
import com.project.hanspoon.common.faq.dto.FaqDto;
import com.project.hanspoon.common.dto.PageResponse;
import com.project.hanspoon.common.faq.service.FaqService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자용 FAQ REST API Controller
 */
@RestController
@RequestMapping("/api/admin/faq")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminFaqController {

    private final FaqService faqService;

    /**
     * 목록 조회
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<PageResponse<FaqDto>>> list(@PageableDefault(size = 20) Pageable pageable) {
        Page<FaqDto> faqs = faqService.getFaqList(pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(faqs)));
    }

    /**
     * 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FaqDto>> get(@PathVariable("id") Long faqId) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(faqService.getFaq(faqId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("FAQ를 찾을 수 없습니다."));
        }
    }

    /**
     * 등록
     */
    @PostMapping
    public ResponseEntity<ApiResponse<FaqDto>> create(@Valid @RequestBody FaqDto dto) {
        try {
            return ResponseEntity.ok(ApiResponse.ok("FAQ가 등록되었습니다.", faqService.createFaq(dto)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("등록에 실패했습니다."));
        }
    }

    /**
     * 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FaqDto>> update(@PathVariable("id") Long faqId, @Valid @RequestBody FaqDto dto) {
        try {
            return ResponseEntity.ok(ApiResponse.ok("FAQ가 수정되었습니다.", faqService.updateFaq(faqId, dto)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("수정에 실패했습니다."));
        }
    }

    /**
     * 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long faqId) {
        try {
            faqService.deleteFaq(faqId);
            return ResponseEntity.ok(ApiResponse.ok("FAQ가 삭제되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("삭제에 실패했습니다."));
        }
    }
}
