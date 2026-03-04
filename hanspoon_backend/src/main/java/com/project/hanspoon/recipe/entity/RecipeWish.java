package com.project.hanspoon.recipe.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.hanspoon.common.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="recipe_wish")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipeWish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wish_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @ManyToOne
    @JoinColumn(name = "recipe_id")
    @JsonIgnore
    private Recipe recipe;

    public RecipeWish(Recipe recipe, User user) {
        this.recipe = recipe;
        this.user = user;
    }


}
