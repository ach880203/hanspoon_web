package com.project.hanspoon.recipe.dto;

import com.project.hanspoon.recipe.entity.RecipeRev;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 마이페이지 통합 리뷰 화면에서 레시피 리뷰를 표시하기 위한 전용 DTO입니다.
 * 기존 상세 DTO(RevDto)는 레시피 ID/제목이 없어 통합 리스트에서 바로 쓰기 어렵기 때문에 분리합니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyRecipeReviewDto {
    private Long revId;
    private Long recipeId;
    private String recipeTitle;
    private int rating;
    private String content;
    private String userName;
    private LocalDateTime createdAt;
    private String answerContent;
    private LocalDateTime answeredAt;

    public static MyRecipeReviewDto fromEntity(RecipeRev review) {
        return MyRecipeReviewDto.builder()
                .revId(review.getId())
                .recipeId(review.getRecipe() != null ? review.getRecipe().getId() : null)
                .recipeTitle(review.getRecipe() != null ? review.getRecipe().getTitle() : null)
                .rating(review.getRating())
                .content(review.getContent())
                .userName(review.getUser() != null ? review.getUser().getUserName() : null)
                .createdAt(review.getCreatedAt())
                .answerContent(review.getAnswerContent())
                .answeredAt(review.getAnsweredAt())
                .build();
    }
}
