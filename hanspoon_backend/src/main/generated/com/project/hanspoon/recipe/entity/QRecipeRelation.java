package com.project.hanspoon.recipe.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRecipeRelation is a Querydsl query type for RecipeRelation
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRecipeRelation extends EntityPathBase<RecipeRelation> {

    private static final long serialVersionUID = 1333259557L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRecipeRelation recipeRelation = new QRecipeRelation("recipeRelation");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QRecipe mainRecipe;

    public final QRecipe subRecipe;

    public QRecipeRelation(String variable) {
        this(RecipeRelation.class, forVariable(variable), INITS);
    }

    public QRecipeRelation(Path<? extends RecipeRelation> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRecipeRelation(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRecipeRelation(PathMetadata metadata, PathInits inits) {
        this(RecipeRelation.class, metadata, inits);
    }

    public QRecipeRelation(Class<? extends RecipeRelation> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.mainRecipe = inits.isInitialized("mainRecipe") ? new QRecipe(forProperty("mainRecipe"), inits.get("mainRecipe")) : null;
        this.subRecipe = inits.isInitialized("subRecipe") ? new QRecipe(forProperty("subRecipe"), inits.get("subRecipe")) : null;
    }

}

