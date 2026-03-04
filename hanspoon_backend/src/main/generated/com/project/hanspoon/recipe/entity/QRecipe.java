package com.project.hanspoon.recipe.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRecipe is a Querydsl query type for Recipe
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRecipe extends EntityPathBase<Recipe> {

    private static final long serialVersionUID = -966154231L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRecipe recipe = new QRecipe("recipe");

    public final NumberPath<Double> baseServings = createNumber("baseServings", Double.class);

    public final EnumPath<com.project.hanspoon.recipe.constant.Category> category = createEnum("category", com.project.hanspoon.recipe.constant.Category.class);

    public final BooleanPath deleted = createBoolean("deleted");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath recipeImg = createString("recipeImg");

    public final ListPath<RecipeIngredientGroup, QRecipeIngredientGroup> recipeIngredientGroup = this.<RecipeIngredientGroup, QRecipeIngredientGroup>createList("recipeIngredientGroup", RecipeIngredientGroup.class, QRecipeIngredientGroup.class, PathInits.DIRECT2);

    public final ListPath<RecipeIng, QRecipeIng> recipeIngs = this.<RecipeIng, QRecipeIng>createList("recipeIngs", RecipeIng.class, QRecipeIng.class, PathInits.DIRECT2);

    public final ListPath<RecipeInstructionGroup, QRecipeInstructionGroup> recipeInstructionGroup = this.<RecipeInstructionGroup, QRecipeInstructionGroup>createList("recipeInstructionGroup", RecipeInstructionGroup.class, QRecipeInstructionGroup.class, PathInits.DIRECT2);

    public final ListPath<RecipeRev, QRecipeRev> recipeRevs = this.<RecipeRev, QRecipeRev>createList("recipeRevs", RecipeRev.class, QRecipeRev.class, PathInits.DIRECT2);

    public final ListPath<RecipeRecommendation, QRecipeRecommendation> recommendations = this.<RecipeRecommendation, QRecipeRecommendation>createList("recommendations", RecipeRecommendation.class, QRecipeRecommendation.class, PathInits.DIRECT2);

    public final NumberPath<Integer> recommendCount = createNumber("recommendCount", Integer.class);

    public final NumberPath<Integer> saltiness = createNumber("saltiness", Integer.class);

    public final NumberPath<Integer> spiciness = createNumber("spiciness", Integer.class);

    public final ListPath<RecipeRelation, QRecipeRelation> subRecipeRelations = this.<RecipeRelation, QRecipeRelation>createList("subRecipeRelations", RecipeRelation.class, QRecipeRelation.class, PathInits.DIRECT2);

    public final NumberPath<Integer> sweetness = createNumber("sweetness", Integer.class);

    public final StringPath title = createString("title");

    public final com.project.hanspoon.common.user.entity.QUser user;

    public QRecipe(String variable) {
        this(Recipe.class, forVariable(variable), INITS);
    }

    public QRecipe(Path<? extends Recipe> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRecipe(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRecipe(PathMetadata metadata, PathInits inits) {
        this(Recipe.class, metadata, inits);
    }

    public QRecipe(Class<? extends Recipe> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.project.hanspoon.common.user.entity.QUser(forProperty("user")) : null;
    }

}

