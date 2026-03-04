package com.project.hanspoon.recipe.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRecipeIngredient is a Querydsl query type for RecipeIngredient
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRecipeIngredient extends EntityPathBase<RecipeIngredient> {

    private static final long serialVersionUID = 1392918202L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRecipeIngredient recipeIngredient = new QRecipeIngredient("recipeIngredient");

    public final NumberPath<Double> baseAmount = createNumber("baseAmount", Double.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath main = createBoolean("main");

    public final StringPath name = createString("name");

    public final NumberPath<Double> ratio = createNumber("ratio", Double.class);

    public final QRecipeIngredientGroup recipeIngredientGroup;

    public final EnumPath<com.project.hanspoon.recipe.constant.TasteType> tasteType = createEnum("tasteType", com.project.hanspoon.recipe.constant.TasteType.class);

    public final StringPath unit = createString("unit");

    public QRecipeIngredient(String variable) {
        this(RecipeIngredient.class, forVariable(variable), INITS);
    }

    public QRecipeIngredient(Path<? extends RecipeIngredient> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRecipeIngredient(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRecipeIngredient(PathMetadata metadata, PathInits inits) {
        this(RecipeIngredient.class, metadata, inits);
    }

    public QRecipeIngredient(Class<? extends RecipeIngredient> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.recipeIngredientGroup = inits.isInitialized("recipeIngredientGroup") ? new QRecipeIngredientGroup(forProperty("recipeIngredientGroup"), inits.get("recipeIngredientGroup")) : null;
    }

}

