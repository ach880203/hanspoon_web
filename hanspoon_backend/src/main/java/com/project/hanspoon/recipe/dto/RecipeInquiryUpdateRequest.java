package com.project.hanspoon.recipe.dto;

// 레시피 문의 수정 요청 DTO입니다.
public record RecipeInquiryUpdateRequest(
        String category,
        String title,
        String content,
        Boolean secret
) {
}
