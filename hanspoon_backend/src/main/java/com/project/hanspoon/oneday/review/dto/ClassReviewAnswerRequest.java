package com.project.hanspoon.oneday.review.dto;

// 리뷰 답글(대댓글) 등록 요청 DTO입니다.
// 프런트에서 { "answerContent": "..." } 형태로 전송합니다.
public record ClassReviewAnswerRequest(
        String answerContent
) {
}
