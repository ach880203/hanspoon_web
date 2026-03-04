package com.project.hanspoon.shop.review.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRevProduct is a Querydsl query type for RevProduct
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRevProduct extends EntityPathBase<RevProduct> {

    private static final long serialVersionUID = 854247529L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRevProduct revProduct = new QRevProduct("revProduct");

    public final StringPath content = createString("content");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.project.hanspoon.shop.product.entity.QProduct product;

    public final NumberPath<Integer> rating = createNumber("rating", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final com.project.hanspoon.common.user.entity.QUser user;

    public QRevProduct(String variable) {
        this(RevProduct.class, forVariable(variable), INITS);
    }

    public QRevProduct(Path<? extends RevProduct> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRevProduct(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRevProduct(PathMetadata metadata, PathInits inits) {
        this(RevProduct.class, metadata, inits);
    }

    public QRevProduct(Class<? extends RevProduct> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.product = inits.isInitialized("product") ? new com.project.hanspoon.shop.product.entity.QProduct(forProperty("product")) : null;
        this.user = inits.isInitialized("user") ? new com.project.hanspoon.common.user.entity.QUser(forProperty("user")) : null;
    }

}

