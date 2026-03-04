package com.project.hanspoon.recipe.repository;

import com.project.hanspoon.recipe.entity.Recipe;
import com.project.hanspoon.recipe.entity.RecipeInstructionGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstructionGroupRepository extends JpaRepository<RecipeInstructionGroup, Long> {


    void deleteByRecipe(Recipe recipe);
}
