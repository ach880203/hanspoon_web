package com.project.hanspoon.recipe.entity;

import com.project.hanspoon.recipe.constant.TasteType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="recipe_ingredient")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipeIngredient { //상세 재료

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ingre_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private RecipeIngredientGroup recipeIngredientGroup; //소속 재료 그룹

    @Column(nullable = false)
    private String name; //재료명

    @Column(nullable = false)
    private double baseAmount; // 1인분 기준 용량

    private double ratio; // 베이커 비율


    private String unit; // 단위

    @Enumerated(EnumType.STRING)
    private TasteType tasteType; //취향조절


    @Column(nullable = false,
            columnDefinition = "boolean default false")
    private boolean main; //베이커퍼센트 기준이 되는 재료
}
