package com.project.hanspoon.recipe.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRecipeInstruction is a Querydsl query type for RecipeInstruction
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRecipeInstruction extends EntityPathBase<RecipeInstruction> {

    private static final long serialVersionUID = -1658929979L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRecipeInstruction recipeInstruction = new QRecipeInstruction("recipeInstruction");

    public final StringPath content = createString("content");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath instImg = createString("instImg");

    public final QRecipeInstructionGroup recipeInstructionGroup;

    public final NumberPath<Integer> stepOrder = createNumber("stepOrder", Integer.class);

    public QRecipeInstruction(String variable) {
        this(RecipeInstruction.class, forVariable(variable), INITS);
    }

    public QRecipeInstruction(Path<? extends RecipeInstruction> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRecipeInstruction(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRecipeInstruction(PathMetadata metadata, PathInits inits) {
        this(RecipeInstruction.class, metadata, inits);
    }

    public QRecipeInstruction(Class<? extends RecipeInstruction> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.recipeInstructionGroup = inits.isInitialized("recipeInstructionGroup") ? new QRecipeInstructionGroup(forProperty("recipeInstructionGroup"), inits.get("recipeInstructionGroup")) : null;
    }

}

