package com.project.hanspoon.oneday.review.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QClassReview is a Querydsl query type for ClassReview
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QClassReview extends EntityPathBase<ClassReview> {

    private static final long serialVersionUID = -1912741517L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QClassReview classReview = new QClassReview("classReview");

    public final com.project.hanspoon.common.entity.QBaseTimeEntity _super = new com.project.hanspoon.common.entity.QBaseTimeEntity(this);

    public final StringPath answerContent = createString("answerContent");

    public final DateTimePath<java.time.LocalDateTime> answeredAt = createDateTime("answeredAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> answeredByUserId = createNumber("answeredByUserId", Long.class);

    public final com.project.hanspoon.oneday.clazz.entity.QClassProduct classProduct;

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final BooleanPath delFlag = createBoolean("delFlag");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> legacyUpdatedAt = createDateTime("legacyUpdatedAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> rating = createNumber("rating", Integer.class);

    public final NumberPath<Long> reservationId = createNumber("reservationId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QClassReview(String variable) {
        this(ClassReview.class, forVariable(variable), INITS);
    }

    public QClassReview(Path<? extends ClassReview> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QClassReview(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QClassReview(PathMetadata metadata, PathInits inits) {
        this(ClassReview.class, metadata, inits);
    }

    public QClassReview(Class<? extends ClassReview> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.classProduct = inits.isInitialized("classProduct") ? new com.project.hanspoon.oneday.clazz.entity.QClassProduct(forProperty("classProduct"), inits.get("classProduct")) : null;
    }

}

