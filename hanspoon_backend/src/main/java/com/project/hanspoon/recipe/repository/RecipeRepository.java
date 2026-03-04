package com.project.hanspoon.recipe.repository;

import com.project.hanspoon.recipe.constant.Category;
import com.project.hanspoon.recipe.entity.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    Page<Recipe> findByTitleContainingAndDeletedFalse(String keyword, Pageable pageable);

    Page<Recipe> findByCategoryAndTitleContainingAndDeletedFalse
            (Category category, String keyword, Pageable pageable);

    Page<Recipe> findByDeletedFalse(Pageable pageable);

    Page<Recipe> findByDeletedTrueAndCategory(Category category, Pageable pageable);

    Page<Recipe> findByDeletedTrue(Pageable pageable);

    Page<Recipe> findByUser_UserIdAndDeletedFalse(Long userId, Pageable pageable);
}
