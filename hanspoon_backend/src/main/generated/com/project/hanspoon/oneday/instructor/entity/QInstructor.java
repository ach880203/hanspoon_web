package com.project.hanspoon.oneday.instructor.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QInstructor is a Querydsl query type for Instructor
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QInstructor extends EntityPathBase<Instructor> {

    private static final long serialVersionUID = -295517249L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QInstructor instructor = new QInstructor("instructor");

    public final com.project.hanspoon.common.entity.QBaseTimeEntity _super = new com.project.hanspoon.common.entity.QBaseTimeEntity(this);

    public final StringPath bio = createString("bio");

    public final StringPath career = createString("career");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> legacyMemberId = createNumber("legacyMemberId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> legacyUpdatedAt = createDateTime("legacyUpdatedAt", java.time.LocalDateTime.class);

    public final StringPath profileImageData = createString("profileImageData");

    public final StringPath specialty = createString("specialty");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.project.hanspoon.common.user.entity.QUser user;

    public QInstructor(String variable) {
        this(Instructor.class, forVariable(variable), INITS);
    }

    public QInstructor(Path<? extends Instructor> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QInstructor(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QInstructor(PathMetadata metadata, PathInits inits) {
        this(Instructor.class, metadata, inits);
    }

    public QInstructor(Class<? extends Instructor> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.project.hanspoon.common.user.entity.QUser(forProperty("user")) : null;
    }

}

