package com.project.hanspoon.recipe.repository;

import com.project.hanspoon.common.user.entity.User;
import com.project.hanspoon.recipe.dto.WishDto;
import com.project.hanspoon.recipe.entity.Recipe;
import com.project.hanspoon.recipe.entity.RecipeWish;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface RecipeWishesRepository extends JpaRepository<RecipeWish, Long> {
    List<RecipeWish> recipe(Recipe recipe);

    List<RecipeWish> user(User user);

    @Query("SELECT rw FROM RecipeWish rw WHERE rw.user.email = :email")
    Page<RecipeWish> findByUserEmail (@Param("email") String email, Pageable pageable);

    @Query("SELECT rw FROM RecipeWish rw WHERE rw.user.email = :email AND rw.recipe.category = :category")
    Page<RecipeWish> findByUserEmailAndCategory (@Param("email") String email, @Param("category") String category, Pageable pageable);

    void deleteByUserEmailAndId(String email, Long id);

    boolean existsByUserEmailAndRecipeId(String email, Long id);

    Optional<RecipeWish> findByUserEmailAndRecipeId(String userEmail, Long id);
}
