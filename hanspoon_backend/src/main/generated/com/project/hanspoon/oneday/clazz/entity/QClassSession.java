package com.project.hanspoon.oneday.clazz.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QClassSession is a Querydsl query type for ClassSession
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QClassSession extends EntityPathBase<ClassSession> {

    private static final long serialVersionUID = 612372363L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QClassSession classSession = new QClassSession("classSession");

    public final com.project.hanspoon.common.entity.QBaseTimeEntity _super = new com.project.hanspoon.common.entity.QBaseTimeEntity(this);

    public final NumberPath<Integer> capacity = createNumber("capacity", Integer.class);

    public final QClassProduct classProduct;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> legacyUpdatedAt = createDateTime("legacyUpdatedAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> price = createNumber("price", Integer.class);

    public final NumberPath<Integer> reservedCount = createNumber("reservedCount", Integer.class);

    public final EnumPath<com.project.hanspoon.oneday.clazz.domain.SessionSlot> slot = createEnum("slot", com.project.hanspoon.oneday.clazz.domain.SessionSlot.class);

    public final DateTimePath<java.time.LocalDateTime> startAt = createDateTime("startAt", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> version = createNumber("version", Long.class);

    public QClassSession(String variable) {
        this(ClassSession.class, forVariable(variable), INITS);
    }

    public QClassSession(Path<? extends ClassSession> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QClassSession(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QClassSession(PathMetadata metadata, PathInits inits) {
        this(ClassSession.class, metadata, inits);
    }

    public QClassSession(Class<? extends ClassSession> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.classProduct = inits.isInitialized("classProduct") ? new QClassProduct(forProperty("classProduct"), inits.get("classProduct")) : null;
    }

}

