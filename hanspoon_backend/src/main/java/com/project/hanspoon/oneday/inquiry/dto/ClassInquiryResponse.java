package com.project.hanspoon.oneday.inquiry.dto;

import com.project.hanspoon.oneday.inquiry.domain.Visibility;

import java.time.LocalDateTime;

// 문의 목록/상세 응답 DTO입니다.
// 프런트에서 바로 렌더링할 수 있도록 화면에서 필요한 값을 함께 담습니다.
public record ClassInquiryResponse(
        Long inquiryId,
        Long classProductId,
        Long userId,
        String writerName,
        String category,
        String title,
        String content,
        Visibility visibility,
        boolean hasAttachment,
        boolean answered,
        String answerContent,
        Long answeredByUserId,
        LocalDateTime answeredAt,
        boolean canAnswer,
        LocalDateTime createdAt
) {
}
