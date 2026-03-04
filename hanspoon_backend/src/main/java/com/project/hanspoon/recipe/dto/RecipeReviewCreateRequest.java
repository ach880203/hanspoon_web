package com.project.hanspoon.recipe.dto;

// 레시피 리뷰 작성 요청 DTO입니다.
public record RecipeReviewCreateRequest(
        Long recipeId,
        int rating,
        String content
) {
}
