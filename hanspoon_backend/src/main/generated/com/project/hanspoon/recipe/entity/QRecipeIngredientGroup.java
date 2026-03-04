package com.project.hanspoon.recipe.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRecipeIngredientGroup is a Querydsl query type for RecipeIngredientGroup
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRecipeIngredientGroup extends EntityPathBase<RecipeIngredientGroup> {

    private static final long serialVersionUID = -1363940379L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRecipeIngredientGroup recipeIngredientGroup = new QRecipeIngredientGroup("recipeIngredientGroup");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<RecipeIngredient, QRecipeIngredient> ingredients = this.<RecipeIngredient, QRecipeIngredient>createList("ingredients", RecipeIngredient.class, QRecipeIngredient.class, PathInits.DIRECT2);

    public final StringPath name = createString("name");

    public final QRecipe recipe;

    public final NumberPath<Integer> sortOrder = createNumber("sortOrder", Integer.class);

    public QRecipeIngredientGroup(String variable) {
        this(RecipeIngredientGroup.class, forVariable(variable), INITS);
    }

    public QRecipeIngredientGroup(Path<? extends RecipeIngredientGroup> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRecipeIngredientGroup(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRecipeIngredientGroup(PathMetadata metadata, PathInits inits) {
        this(RecipeIngredientGroup.class, metadata, inits);
    }

    public QRecipeIngredientGroup(Class<? extends RecipeIngredientGroup> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.recipe = inits.isInitialized("recipe") ? new QRecipe(forProperty("recipe"), inits.get("recipe")) : null;
    }

}

