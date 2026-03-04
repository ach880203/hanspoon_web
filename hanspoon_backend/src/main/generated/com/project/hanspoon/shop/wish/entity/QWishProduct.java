package com.project.hanspoon.shop.wish.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QWishProduct is a Querydsl query type for WishProduct
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QWishProduct extends EntityPathBase<WishProduct> {

    private static final long serialVersionUID = -568661860L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QWishProduct wishProduct = new QWishProduct("wishProduct");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.project.hanspoon.shop.product.entity.QProduct product;

    public final com.project.hanspoon.common.user.entity.QUser user;

    public QWishProduct(String variable) {
        this(WishProduct.class, forVariable(variable), INITS);
    }

    public QWishProduct(Path<? extends WishProduct> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QWishProduct(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QWishProduct(PathMetadata metadata, PathInits inits) {
        this(WishProduct.class, metadata, inits);
    }

    public QWishProduct(Class<? extends WishProduct> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.product = inits.isInitialized("product") ? new com.project.hanspoon.shop.product.entity.QProduct(forProperty("product")) : null;
        this.user = inits.isInitialized("user") ? new com.project.hanspoon.common.user.entity.QUser(forProperty("user")) : null;
    }

}

