package com.project.hanspoon.recipe.dto;

import com.project.hanspoon.recipe.entity.Recipe;
import com.project.hanspoon.recipe.entity.RecipeIngredient;
import com.project.hanspoon.recipe.entity.RecipeInstruction;
import com.project.hanspoon.recipe.entity.RecipeWish;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipeDetailDto {
    private Long id;
    private Long userId;
    private String title;
    private String recipeImg;
    private double baseServings;
    private String category;
    private int sweetness;
    private int saltiness;
    private int spiciness;
    private boolean isWished;
    private Long wihsid;
    private boolean recommended; // 추천 여부
    private Integer recommendCount; // 전체 추천수

    private List<Long> subrecipe;

    private List<IngredientGroupDto> ingredientGroup;

    private List<InstructionGroupDto> instructionGroup;

    private Map<String, IngredientDto> ingredientMap;

    private List<RevDto> reviews;

    private List<IngDto> ingDtos;

    public static RecipeDetailDto fromEntity(
            Recipe recipe, boolean wished,
            RecipeWish recipeWish, boolean isRecommended) {
        Map<String, IngredientDto> ingMap = recipe.getRecipeIngredientGroup().stream()
                .flatMap(group -> group.getIngredients().stream())
                .map(IngredientDto::fromEntity)
                .collect(Collectors.toMap(
                        IngredientDto::getName,
                        ing -> ing,
                        (existing, replacement) -> existing
                ));

        return RecipeDetailDto.builder()
                .id(recipe.getId())
                .userId(recipe.getUser() != null ? recipe.getUser().getUserId(): null)
                .title(recipe.getTitle())
                .isWished(wished)
                .wihsid(recipeWish != null ? recipeWish.getId() : null)
                .recommended(isRecommended)
                .recommendCount(recipe.getRecommendCount())
                .recipeImg(recipe.getRecipeImg())
                .category(recipe.getCategory() !=null ?
                        recipe.getCategory().name() : null)
                .baseServings(recipe.getBaseServings())
                .saltiness(recipe.getSaltiness())
                .sweetness(recipe.getSweetness())
                .spiciness(recipe.getSpiciness())
                .subrecipe(recipe.getSubRecipeRelations() !=null ?
                        recipe.getSubRecipeRelations().stream()
                                .map(sub -> sub.getSubRecipe().getId())
                                .toList() : List.of())
                .ingredientGroup(recipe.getRecipeIngredientGroup().stream()
                        .map(IngredientGroupDto::fromEntity).toList())
                .instructionGroup(recipe.getRecipeInstructionGroup().stream()
                        .map(InstructionGroupDto::fromEntity).toList())
                .ingredientMap(ingMap)
                .reviews(recipe.getRecipeRevs() !=null ?
                        recipe.getRecipeRevs().stream()
                                .filter(rev -> !rev.isDelFlag())
                                .map(RevDto::fromEntity)
                                .toList() : List.of())
                .ingDtos(recipe.getRecipeIngs() !=null?
                        recipe.getRecipeIngs().stream()
                                .map(IngDto::fromEntity)
                                .toList() : List.of())
                .build();
    }
}
