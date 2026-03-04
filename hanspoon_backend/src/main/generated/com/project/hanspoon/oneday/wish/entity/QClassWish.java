package com.project.hanspoon.oneday.wish.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QClassWish is a Querydsl query type for ClassWish
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QClassWish extends EntityPathBase<ClassWish> {

    private static final long serialVersionUID = 796961299L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QClassWish classWish = new QClassWish("classWish");

    public final com.project.hanspoon.oneday.clazz.entity.QClassProduct classProduct;

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QClassWish(String variable) {
        this(ClassWish.class, forVariable(variable), INITS);
    }

    public QClassWish(Path<? extends ClassWish> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QClassWish(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QClassWish(PathMetadata metadata, PathInits inits) {
        this(ClassWish.class, metadata, inits);
    }

    public QClassWish(Class<? extends ClassWish> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.classProduct = inits.isInitialized("classProduct") ? new com.project.hanspoon.oneday.clazz.entity.QClassProduct(forProperty("classProduct"), inits.get("classProduct")) : null;
    }

}

