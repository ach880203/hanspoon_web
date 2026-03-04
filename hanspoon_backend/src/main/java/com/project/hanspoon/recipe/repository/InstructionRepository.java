package com.project.hanspoon.recipe.repository;

import com.project.hanspoon.recipe.entity.Recipe;
import com.project.hanspoon.recipe.entity.RecipeIngredient;
import com.project.hanspoon.recipe.entity.RecipeInstruction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstructionRepository extends JpaRepository<RecipeInstruction, Long> {
}
