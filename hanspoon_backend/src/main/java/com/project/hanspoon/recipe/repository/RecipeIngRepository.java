package com.project.hanspoon.recipe.repository;

import com.project.hanspoon.recipe.entity.RecipeIng;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecipeIngRepository extends JpaRepository<RecipeIng, Long> {
    List<RecipeIng> findAllByRecipe_IdOrderByIdDesc(Long recipeId);

    List<RecipeIng> findAllByUser_UserIdOrderByIdDesc(Long userId);

    List<RecipeIng> findAllByOrderByIdDesc();

    Optional<RecipeIng> findByIdAndUser_UserId(Long inquiryId, Long userId);

    long countByIsAnsweredFalse();
}
