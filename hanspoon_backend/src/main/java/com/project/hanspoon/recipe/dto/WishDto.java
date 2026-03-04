package com.project.hanspoon.recipe.dto;

import com.project.hanspoon.recipe.entity.Recipe;
import com.project.hanspoon.recipe.entity.RecipeWish;
import lombok.*;
import lombok.extern.log4j.Log4j2;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WishDto {
    private Long id;
    private Long userId;
    private Long wishId;
    private String title;
    private String mainImage;

    public WishDto(RecipeWish recipeWish, Recipe recipe) {
        this.id = recipe.getId();
        this.wishId = recipeWish.getId();
        this.userId = recipeWish.getUser().getUserId();
        this.title = recipe.getTitle();
        this.mainImage = recipe.getRecipeImg();
    }

}
