package com.project.hanspoon.common.faq.controller;

import com.project.hanspoon.common.response.ApiResponse;
import com.project.hanspoon.common.faq.dto.FaqDto;
import com.project.hanspoon.common.faq.service.FaqService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * FAQ REST API Controller (사용자)
 */
@RestController
@RequestMapping("/api/faq")
@RequiredArgsConstructor
public class FaqController {

    private final FaqService faqService;

    /**
     * 목록 조회
     * GET /api/faq/list?category=카테고리
     */
    @GetMapping({ "", "/list" })
    public ResponseEntity<ApiResponse<List<FaqDto>>> list(
            @RequestParam(value = "category", required = false) String category) {

        List<FaqDto> faqs;
        if (category != null && !category.isEmpty()) {
            faqs = faqService.getFaqListByCategory(category);
        } else {
            faqs = faqService.getAllFaqList();
        }
        return ResponseEntity.ok(ApiResponse.ok(faqs));
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
}
