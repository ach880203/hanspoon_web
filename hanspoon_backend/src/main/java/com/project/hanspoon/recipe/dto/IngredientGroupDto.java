package com.project.hanspoon.recipe.dto;

import com.project.hanspoon.recipe.entity.RecipeIngredientGroup;
import lombok.*;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IngredientGroupDto {

    private Long id;
    private String name;
    private int sortOrder;

    @Builder.Default
    private List<IngredientDto> ingredients = new ArrayList<>();

    public static IngredientGroupDto fromEntity(RecipeIngredientGroup recipeIngredientGroup) {
        return IngredientGroupDto.builder()
                .name(recipeIngredientGroup.getName())
                .ingredients(recipeIngredientGroup.getIngredients().stream()
                        .map(IngredientDto::fromEntity)
                        .toList())
                .build();
    }


}
