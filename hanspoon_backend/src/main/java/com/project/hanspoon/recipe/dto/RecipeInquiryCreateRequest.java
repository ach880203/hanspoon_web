package com.project.hanspoon.recipe.dto;

// 레시피 문의 작성 요청 DTO입니다.
public record RecipeInquiryCreateRequest(
        Long recipeId,
        String category,
        String title,
        String content,
        boolean secret
) {
}
