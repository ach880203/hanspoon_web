package com.project.hanspoon.recipe.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="recipe_instruction_group")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipeInstructionGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inst_group_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "recipe_id")
    @JsonIgnore
    private Recipe recipe;

    @Column(name = "group_title")
    private String title; //조리 그룹명

    private int sortOrder; //조리 단계

    @Builder.Default
    @OneToMany(mappedBy = "recipeInstructionGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeInstruction> instructions = new ArrayList<>();
}
