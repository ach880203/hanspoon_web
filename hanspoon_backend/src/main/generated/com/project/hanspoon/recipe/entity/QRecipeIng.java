package com.project.hanspoon.recipe.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRecipeIng is a Querydsl query type for RecipeIng
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRecipeIng extends EntityPathBase<RecipeIng> {

    private static final long serialVersionUID = -2124771559L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRecipeIng recipeIng = new QRecipeIng("recipeIng");

    public final StringPath answer = createString("answer");

    public final DateTimePath<java.time.LocalDateTime> answeredAt = createDateTime("answeredAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> answeredByUserId = createNumber("answeredByUserId", Long.class);

    public final StringPath category = createString("category");

    public final StringPath content = createString("content");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isAnswered = createBoolean("isAnswered");

    public final QRecipe recipe;

    public final BooleanPath secret = createBoolean("secret");

    public final StringPath title = createString("title");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final com.project.hanspoon.common.user.entity.QUser user;

    public QRecipeIng(String variable) {
        this(RecipeIng.class, forVariable(variable), INITS);
    }

    public QRecipeIng(Path<? extends RecipeIng> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRecipeIng(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRecipeIng(PathMetadata metadata, PathInits inits) {
        this(RecipeIng.class, metadata, inits);
    }

    public QRecipeIng(Class<? extends RecipeIng> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.recipe = inits.isInitialized("recipe") ? new QRecipe(forProperty("recipe"), inits.get("recipe")) : null;
        this.user = inits.isInitialized("user") ? new com.project.hanspoon.common.user.entity.QUser(forProperty("user")) : null;
    }

}

