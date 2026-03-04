package com.project.hanspoon.recipe.dto;

import com.project.hanspoon.recipe.entity.RecipeIng;
import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IngDto {

    private Long id;
    private String username;
    private String content;
    private String answer;
    private boolean isAnswered;

    public static IngDto fromEntity(RecipeIng ing) {
        return IngDto.builder()
                .id(ing.getId())
                .username(ing.getUser().getUserName())
                .answer(ing.getAnswer())
                .content(ing.getContent())
                .isAnswered(ing.isAnswered())
                .build();

    }
}
