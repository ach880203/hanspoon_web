package com.project.hanspoon.recipe.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRecipeInstructionGroup is a Querydsl query type for RecipeInstructionGroup
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRecipeInstructionGroup extends EntityPathBase<RecipeInstructionGroup> {

    private static final long serialVersionUID = 150961338L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRecipeInstructionGroup recipeInstructionGroup = new QRecipeInstructionGroup("recipeInstructionGroup");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<RecipeInstruction, QRecipeInstruction> instructions = this.<RecipeInstruction, QRecipeInstruction>createList("instructions", RecipeInstruction.class, QRecipeInstruction.class, PathInits.DIRECT2);

    public final QRecipe recipe;

    public final NumberPath<Integer> sortOrder = createNumber("sortOrder", Integer.class);

    public final StringPath title = createString("title");

    public QRecipeInstructionGroup(String variable) {
        this(RecipeInstructionGroup.class, forVariable(variable), INITS);
    }

    public QRecipeInstructionGroup(Path<? extends RecipeInstructionGroup> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRecipeInstructionGroup(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRecipeInstructionGroup(PathMetadata metadata, PathInits inits) {
        this(RecipeInstructionGroup.class, metadata, inits);
    }

    public QRecipeInstructionGroup(Class<? extends RecipeInstructionGroup> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.recipe = inits.isInitialized("recipe") ? new QRecipe(forProperty("recipe"), inits.get("recipe")) : null;
    }

}

