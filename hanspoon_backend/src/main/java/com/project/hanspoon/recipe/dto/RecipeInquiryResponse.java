package com.project.hanspoon.recipe.dto;

import java.time.LocalDateTime;

// 레시피 문의 목록/상세 응답 DTO입니다.
public record RecipeInquiryResponse(
        Long inquiryId,
        Long recipeId,
        Long userId,
        String writerName,
        String recipeTitle,
        String category,
        String title,
        String content,
        boolean secret,
        boolean answered,
        String answerContent,
        Long answeredByUserId,
        String answeredByName,
        LocalDateTime answeredAt,
        boolean canAnswer,
        LocalDateTime createdAt
) {
}
