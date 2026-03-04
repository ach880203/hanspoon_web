package com.project.hanspoon.shop.inquiry.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QInqProduct is a Querydsl query type for InqProduct
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QInqProduct extends EntityPathBase<InqProduct> {

    private static final long serialVersionUID = -1481488545L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QInqProduct inqProduct = new QInqProduct("inqProduct");

    public final StringPath answer = createString("answer");

    public final DateTimePath<java.time.LocalDateTime> answeredAt = createDateTime("answeredAt", java.time.LocalDateTime.class);

    public final BooleanPath answeredYn = createBoolean("answeredYn");

    public final StringPath content = createString("content");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.project.hanspoon.shop.product.entity.QProduct product;

    public final BooleanPath secret = createBoolean("secret");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final com.project.hanspoon.common.user.entity.QUser user;

    public QInqProduct(String variable) {
        this(InqProduct.class, forVariable(variable), INITS);
    }

    public QInqProduct(Path<? extends InqProduct> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QInqProduct(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QInqProduct(PathMetadata metadata, PathInits inits) {
        this(InqProduct.class, metadata, inits);
    }

    public QInqProduct(Class<? extends InqProduct> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.product = inits.isInitialized("product") ? new com.project.hanspoon.shop.product.entity.QProduct(forProperty("product")) : null;
        this.user = inits.isInitialized("user") ? new com.project.hanspoon.common.user.entity.QUser(forProperty("user")) : null;
    }

}

