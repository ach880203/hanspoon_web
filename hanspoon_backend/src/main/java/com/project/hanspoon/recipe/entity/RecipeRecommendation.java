package com.project.hanspoon.recipe.entity;

import com.project.hanspoon.common.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="recipe_recommendation")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipeRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @JoinColumn(name = "recipe_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Recipe recipe;
}
