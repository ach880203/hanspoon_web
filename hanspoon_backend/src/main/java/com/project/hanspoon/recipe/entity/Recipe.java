package com.project.hanspoon.recipe.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.hanspoon.common.user.entity.User;
import com.project.hanspoon.recipe.constant.Category;
import com.project.hanspoon.recipe.dto.RecipeFormDto;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="recipe")
@SuppressWarnings("JpaDataSourceORMInspection")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Recipe { //레시피 메인

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipe_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String title; //레시피 명

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private Category category; //한식, 제과, 제빵

    @Column(columnDefinition = "DOUBLE DEFAULT 1.0")
    private double baseServings; // 기준 인분 수

    @Column(name = "recipe_img")
    private String recipeImg; // 대표 이미지 경로

    private int sweetness; //단맛
    private int saltiness; //짠맛
    private int spiciness; //매운맛

    @Builder.Default
    @Column(name = "recommend_count", columnDefinition = "INTEGER DEFAULT 0")
    private Integer recommendCount = 0;

    @Builder.Default
    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean deleted = false;

    public void delete() {
        this.deleted = true;
    }

    public void deleteReturn() {
        this.deleted = false;
    }

    @Builder.Default
    @OneToMany(mappedBy = "mainRecipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeRelation> subRecipeRelations = new ArrayList<>();

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeIngredientGroup> recipeIngredientGroup =
            new ArrayList<>(); //연결된 재료그룹 삭제

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeInstructionGroup> recipeInstructionGroup =
            new ArrayList<>(); //연결된 조리 순서 삭제

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeRev> recipeRevs =
            new ArrayList<>();

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeIng> recipeIngs  =
            new ArrayList<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeRecommendation> recommendations;

    @JsonIgnore
    @SuppressWarnings("unused")
    public List<RecipeIngredient> getAllIngredients() {
        return this.recipeIngredientGroup.stream()
                .flatMap(group -> group.getIngredients().stream())
                .toList();

    }

    public void incrementRecommendCount() {
        this.recommendCount++;
    }

    public void decrementRecommendCount() {
        if (this.recommendCount > 0) {
            this.recommendCount--;
        }
    }

    public void updateRecipeImg(String recipeImg) {
        this.recipeImg = recipeImg;
    }

    public static Recipe createRecipe(RecipeFormDto recipeFormDto, User user) {

        return Recipe.builder()
                .title(recipeFormDto.getTitle())
                .user(user)
                .category(recipeFormDto.getCategory())
                .baseServings(recipeFormDto.getBaseServings())
                .recipeImg(recipeFormDto.getRecipeImg())
                .sweetness(recipeFormDto.getSweetness())
                .saltiness(recipeFormDto.getSaltiness())
                .spiciness(recipeFormDto.getSpiciness())
                .build();

    }
}
