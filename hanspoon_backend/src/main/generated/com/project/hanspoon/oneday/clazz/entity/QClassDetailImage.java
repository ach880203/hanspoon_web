package com.project.hanspoon.oneday.clazz.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QClassDetailImage is a Querydsl query type for ClassDetailImage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QClassDetailImage extends EntityPathBase<ClassDetailImage> {

    private static final long serialVersionUID = 1167799807L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QClassDetailImage classDetailImage = new QClassDetailImage("classDetailImage");

    public final QClassProduct classProduct;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imageData = createString("imageData");

    public final NumberPath<Integer> sortOrder = createNumber("sortOrder", Integer.class);

    public QClassDetailImage(String variable) {
        this(ClassDetailImage.class, forVariable(variable), INITS);
    }

    public QClassDetailImage(Path<? extends ClassDetailImage> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QClassDetailImage(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QClassDetailImage(PathMetadata metadata, PathInits inits) {
        this(ClassDetailImage.class, metadata, inits);
    }

    public QClassDetailImage(Class<? extends ClassDetailImage> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.classProduct = inits.isInitialized("classProduct") ? new QClassProduct(forProperty("classProduct"), inits.get("classProduct")) : null;
    }

}

