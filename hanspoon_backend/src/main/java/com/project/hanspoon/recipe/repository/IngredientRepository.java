package com.project.hanspoon.recipe.repository;

import com.project.hanspoon.recipe.dto.IngredientDto;
import com.project.hanspoon.recipe.entity.Recipe;
import com.project.hanspoon.recipe.entity.RecipeIngredient;
import com.project.hanspoon.recipe.entity.RecipeIngredientGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IngredientRepository extends JpaRepository<RecipeIngredient, Long> {

}
