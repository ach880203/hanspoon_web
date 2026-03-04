package com.project.hanspoon.recipe.dto;

import com.project.hanspoon.recipe.constant.Category;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipeFormDto {

    private Long id;
    private String title;
    private Category category;
    private double baseServings;
    private String recipeImg;
    private int sweetness;
    private int saltiness;
    private int spiciness;

    //서브 레시피
    private List<Long> subrecipe;

    @Builder.Default
    //조리 재료 리스트
    private List<IngredientGroupDto> ingredientGroup = new ArrayList<>();

    @Builder.Default
    //조리 순서 리스트
    private List<InstructionGroupDto> instructionGroup = new ArrayList<>();

}
