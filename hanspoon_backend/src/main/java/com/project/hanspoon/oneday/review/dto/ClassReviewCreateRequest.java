package com.project.hanspoon.oneday.review.dto;

public record ClassReviewCreateRequest(
        Long reservationId,
        int rating,
        String content
) {}
