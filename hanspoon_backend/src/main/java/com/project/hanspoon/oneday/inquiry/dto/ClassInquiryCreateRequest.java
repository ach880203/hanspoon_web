package com.project.hanspoon.oneday.inquiry.dto;

// 문의 작성 요청 DTO입니다.
// secret=true 이면 서버에서 Visibility.PRIVATE로 저장합니다.
public record ClassInquiryCreateRequest(
        Long classProductId,
        String category,
        String title,
        String content,
        boolean secret,
        boolean hasAttachment
) {
}
