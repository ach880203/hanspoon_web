package com.project.hanspoon.recipe.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="recipe_instruction")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipeInstruction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inst_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "inst_group_id")
    @JsonIgnore
    private RecipeInstructionGroup recipeInstructionGroup;

    private int stepOrder; //조리 순서

    @Lob
    private String content; //조리 내용


    private String instImg; //조리 이미지
}
