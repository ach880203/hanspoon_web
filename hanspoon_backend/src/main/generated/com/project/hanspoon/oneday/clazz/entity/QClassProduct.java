package com.project.hanspoon.oneday.clazz.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QClassProduct is a Querydsl query type for ClassProduct
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QClassProduct extends EntityPathBase<ClassProduct> {

    private static final long serialVersionUID = -1682089500L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QClassProduct classProduct = new QClassProduct("classProduct");

    public final com.project.hanspoon.common.entity.QBaseTimeEntity _super = new com.project.hanspoon.common.entity.QBaseTimeEntity(this);

    public final EnumPath<com.project.hanspoon.oneday.clazz.domain.RecipeCategory> category = createEnum("category", com.project.hanspoon.oneday.clazz.domain.RecipeCategory.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath description = createString("description");

    public final StringPath detailDescription = createString("detailDescription");

    public final StringPath detailImageData = createString("detailImageData");

    public final ListPath<ClassDetailImage, QClassDetailImage> detailImages = this.<ClassDetailImage, QClassDetailImage>createList("detailImages", ClassDetailImage.class, QClassDetailImage.class, PathInits.DIRECT2);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.project.hanspoon.oneday.instructor.entity.QInstructor instructor;

    public final DateTimePath<java.time.LocalDateTime> legacyUpdatedAt = createDateTime("legacyUpdatedAt", java.time.LocalDateTime.class);

    public final EnumPath<com.project.hanspoon.oneday.clazz.domain.Level> level = createEnum("level", com.project.hanspoon.oneday.clazz.domain.Level.class);

    public final StringPath locationAddress = createString("locationAddress");

    public final NumberPath<Double> locationLat = createNumber("locationLat", Double.class);

    public final NumberPath<Double> locationLng = createNumber("locationLng", Double.class);

    public final EnumPath<com.project.hanspoon.oneday.clazz.domain.RunType> runType = createEnum("runType", com.project.hanspoon.oneday.clazz.domain.RunType.class);

    public final ListPath<ClassSession, QClassSession> session = this.<ClassSession, QClassSession>createList("session", ClassSession.class, QClassSession.class, PathInits.DIRECT2);

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QClassProduct(String variable) {
        this(ClassProduct.class, forVariable(variable), INITS);
    }

    public QClassProduct(Path<? extends ClassProduct> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QClassProduct(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QClassProduct(PathMetadata metadata, PathInits inits) {
        this(ClassProduct.class, metadata, inits);
    }

    public QClassProduct(Class<? extends ClassProduct> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.instructor = inits.isInitialized("instructor") ? new com.project.hanspoon.oneday.instructor.entity.QInstructor(forProperty("instructor"), inits.get("instructor")) : null;
    }

}

