package com.project.hanspoon.recipe.repository;

import com.project.hanspoon.common.user.entity.User;
import com.project.hanspoon.recipe.entity.Recipe;
import com.project.hanspoon.recipe.entity.RecipeRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecommendationRepository extends JpaRepository<RecipeRecommendation, Long> {

    Optional<RecipeRecommendation> findByUserAndRecipe(User user, Recipe recipe);

    boolean existsByUserEmailAndRecipeId(String userEmail, Long id);
}
