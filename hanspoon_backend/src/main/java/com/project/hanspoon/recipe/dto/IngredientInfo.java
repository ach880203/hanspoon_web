package com.project.hanspoon.recipe.dto;

import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IngredientInfo {

    private double baseAmount;
    private String unit;
}
