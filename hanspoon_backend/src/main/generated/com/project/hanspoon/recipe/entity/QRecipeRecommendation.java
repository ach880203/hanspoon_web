package com.project.hanspoon.recipe.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRecipeRecommendation is a Querydsl query type for RecipeRecommendation
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRecipeRecommendation extends EntityPathBase<RecipeRecommendation> {

    private static final long serialVersionUID = -2012123102L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRecipeRecommendation recipeRecommendation = new QRecipeRecommendation("recipeRecommendation");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QRecipe recipe;

    public final com.project.hanspoon.common.user.entity.QUser user;

    public QRecipeRecommendation(String variable) {
        this(RecipeRecommendation.class, forVariable(variable), INITS);
    }

    public QRecipeRecommendation(Path<? extends RecipeRecommendation> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRecipeRecommendation(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRecipeRecommendation(PathMetadata metadata, PathInits inits) {
        this(RecipeRecommendation.class, metadata, inits);
    }

    public QRecipeRecommendation(Class<? extends RecipeRecommendation> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.recipe = inits.isInitialized("recipe") ? new QRecipe(forProperty("recipe"), inits.get("recipe")) : null;
        this.user = inits.isInitialized("user") ? new com.project.hanspoon.common.user.entity.QUser(forProperty("user")) : null;
    }

}

