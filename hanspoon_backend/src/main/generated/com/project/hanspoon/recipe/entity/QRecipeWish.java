package com.project.hanspoon.recipe.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRecipeWish is a Querydsl query type for RecipeWish
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRecipeWish extends EntityPathBase<RecipeWish> {

    private static final long serialVersionUID = -1442996144L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRecipeWish recipeWish = new QRecipeWish("recipeWish");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QRecipe recipe;

    public final com.project.hanspoon.common.user.entity.QUser user;

    public QRecipeWish(String variable) {
        this(RecipeWish.class, forVariable(variable), INITS);
    }

    public QRecipeWish(Path<? extends RecipeWish> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRecipeWish(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRecipeWish(PathMetadata metadata, PathInits inits) {
        this(RecipeWish.class, metadata, inits);
    }

    public QRecipeWish(Class<? extends RecipeWish> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.recipe = inits.isInitialized("recipe") ? new QRecipe(forProperty("recipe"), inits.get("recipe")) : null;
        this.user = inits.isInitialized("user") ? new com.project.hanspoon.common.user.entity.QUser(forProperty("user")) : null;
    }

}

