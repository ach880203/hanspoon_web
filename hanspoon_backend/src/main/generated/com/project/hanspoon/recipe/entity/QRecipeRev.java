package com.project.hanspoon.recipe.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRecipeRev is a Querydsl query type for RecipeRev
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRecipeRev extends EntityPathBase<RecipeRev> {

    private static final long serialVersionUID = -2124763174L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRecipeRev recipeRev = new QRecipeRev("recipeRev");

    public final StringPath answerContent = createString("answerContent");

    public final DateTimePath<java.time.LocalDateTime> answeredAt = createDateTime("answeredAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> answeredByUserId = createNumber("answeredByUserId", Long.class);

    public final StringPath content = createString("content");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final BooleanPath delFlag = createBoolean("delFlag");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> rating = createNumber("rating", Integer.class);

    public final QRecipe recipe;

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final com.project.hanspoon.common.user.entity.QUser user;

    public QRecipeRev(String variable) {
        this(RecipeRev.class, forVariable(variable), INITS);
    }

    public QRecipeRev(Path<? extends RecipeRev> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRecipeRev(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRecipeRev(PathMetadata metadata, PathInits inits) {
        this(RecipeRev.class, metadata, inits);
    }

    public QRecipeRev(Class<? extends RecipeRev> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.recipe = inits.isInitialized("recipe") ? new QRecipe(forProperty("recipe"), inits.get("recipe")) : null;
        this.user = inits.isInitialized("user") ? new com.project.hanspoon.common.user.entity.QUser(forProperty("user")) : null;
    }

}

