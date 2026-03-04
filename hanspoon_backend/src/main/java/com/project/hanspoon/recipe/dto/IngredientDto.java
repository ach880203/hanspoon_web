package com.project.hanspoon.recipe.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.project.hanspoon.recipe.constant.TasteType;
import com.project.hanspoon.recipe.entity.RecipeIngredient;
import com.project.hanspoon.recipe.entity.RecipeIngredientGroup;
import lombok.*;
import org.modelmapper.ModelMapper;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IngredientDto {

    private Long id;
    private String name;
    private double baseAmount;
    private double ratio; // 베이커 비율
    private String unit;
    private boolean main;

    @JsonAlias("taste")
    private TasteType tasteType;

    public static IngredientDto fromEntity(RecipeIngredient ingredient){
        return IngredientDto.builder()
                .id(ingredient.getId())
                .name(ingredient.getName())
                .baseAmount(ingredient.getBaseAmount())
                .unit(ingredient.getUnit())
                .main(ingredient.isMain())
                .tasteType(ingredient.getTasteType())
                .ratio(ingredient.getRatio())
                .build();
    }
    private static ModelMapper modelMapper = new ModelMapper();

    public static IngredientDto of(IngredientDto ingredientDto){
        return modelMapper.map(ingredientDto, IngredientDto.class);
    }
}
