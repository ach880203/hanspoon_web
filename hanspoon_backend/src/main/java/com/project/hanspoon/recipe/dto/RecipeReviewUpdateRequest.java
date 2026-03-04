package com.project.hanspoon.recipe.dto;

// 레시피 리뷰 수정 요청 DTO입니다.
public record RecipeReviewUpdateRequest(
        Integer rating,
        String content
) {
}
