package com.project.hanspoon.recipe.dto;

import com.project.hanspoon.recipe.entity.RecipeRev;
import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RevDto {
    private Long id;
    private String content;
    private int rating;
    private String userName;

    public static RevDto fromEntity(RecipeRev rev) {
        return RevDto.builder()
                .id(rev.getId())
                .content(rev.getContent())
                .rating(rev.getRating())
                .userName(rev.getUser().getUserName())
                .build();
    }

}
