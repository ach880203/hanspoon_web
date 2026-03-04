package com.project.hanspoon.recipe.dto;

import com.project.hanspoon.recipe.entity.RecipeIngredient;
import com.project.hanspoon.recipe.entity.RecipeInstruction;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InstructionDto {

    private Long id;
    private int stepOrder;
    private String content;
    private String instImg;
    private boolean hasNewFile;

    public static InstructionDto fromEntity(RecipeInstruction entity){
        return InstructionDto.builder()
                .stepOrder(entity.getStepOrder())
                .content(entity.getContent())
                .instImg(entity.getInstImg())
                .hasNewFile(false)
                .build();
    }

}
