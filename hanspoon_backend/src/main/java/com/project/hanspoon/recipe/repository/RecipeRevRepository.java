package com.project.hanspoon.recipe.repository;

import com.project.hanspoon.recipe.entity.RecipeRev;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecipeRevRepository extends JpaRepository<RecipeRev, Long> {
    List<RecipeRev> findAllByUser_UserIdOrderByIdDesc(Long userId);

    List<RecipeRev> findAllByUser_UserIdAndDelFlagFalseOrderByIdDesc(Long userId);

    List<RecipeRev> findAllByRecipe_IdAndDelFlagFalseOrderByIdDesc(Long recipeId);

    List<RecipeRev> findAllByDelFlagFalseOrderByIdDesc();

    Optional<RecipeRev> findByIdAndDelFlagFalse(Long revId);
}
