package com.project.hanspoon.oneday.review.dto;

import java.time.LocalDateTime;

public record ClassReviewResponse(
        Long reviewId,
        Long classId,
        Long userId,
        Long reservationId,
        String reviewerName,
        int rating,
        String content,
        LocalDateTime createdAt,
        String answerContent,
        Long answeredByUserId,
        String answeredByName,
        LocalDateTime answeredAt,
        boolean canAnswer
) {}
