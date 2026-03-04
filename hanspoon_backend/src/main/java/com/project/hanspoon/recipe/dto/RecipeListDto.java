package com.project.hanspoon.recipe.dto;

import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipeListDto {
    private Long id;
    private String title;
    private String recipeImg;
    private String category;
    private double averageRating;
    private int reviewCount;
    private int recommendCount;
    private String username;
    private Long userId;
}
