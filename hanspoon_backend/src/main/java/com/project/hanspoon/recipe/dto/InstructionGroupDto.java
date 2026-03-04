package com.project.hanspoon.recipe.dto;

import com.project.hanspoon.recipe.entity.RecipeInstruction;
import com.project.hanspoon.recipe.entity.RecipeInstructionGroup;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InstructionGroupDto {

    private Long id;
    private String title;
    private int sortOrder;

    @Builder.Default
    private List<InstructionDto> instructions = new ArrayList<>();

    public static InstructionGroupDto fromEntity(RecipeInstructionGroup entity) {
        return InstructionGroupDto.builder()
                .title(entity.getTitle())
                .sortOrder(entity.getSortOrder())
                .instructions(entity.getInstructions().stream()
                        .map(InstructionDto::fromEntity)
                        .toList())
                .build();
    }
}
