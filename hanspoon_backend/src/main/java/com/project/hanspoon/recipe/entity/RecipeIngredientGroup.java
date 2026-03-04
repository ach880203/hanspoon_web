package com.project.hanspoon.recipe.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="recipe_ingredient_group")
@SuppressWarnings("JpaDataSourceORMInspection")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipeIngredientGroup { // 재료 그룹

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "recipe_id")
    @JsonIgnore
    private Recipe recipe; //연결 레시피

    @Column(name = "group_name")
    private String name; //재료그룹명

    private int sortOrder; // 화면 노출 순서

    @Builder.Default
    @OneToMany(mappedBy = "recipeIngredientGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeIngredient> ingredients = new ArrayList<>();
}
