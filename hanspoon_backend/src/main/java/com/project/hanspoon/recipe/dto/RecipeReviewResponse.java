package com.project.hanspoon.recipe.dto;

import java.time.LocalDateTime;

// 레시피 리뷰 목록/상세 응답 DTO입니다.
public record RecipeReviewResponse(
        Long reviewId,
        Long recipeId,
        Long userId,
        String reviewerName,
        String recipeTitle,
        int rating,
        String content,
        LocalDateTime createdAt,
        String answerContent,
        Long answeredByUserId,
        String answeredByName,
        LocalDateTime answeredAt,
        boolean canAnswer
) {
}
