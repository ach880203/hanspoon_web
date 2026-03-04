package com.project.hanspoon.oneday.inquiry.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QClassInquiry is a Querydsl query type for ClassInquiry
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QClassInquiry extends EntityPathBase<ClassInquiry> {

    private static final long serialVersionUID = 396087979L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QClassInquiry classInquiry = new QClassInquiry("classInquiry");

    public final com.project.hanspoon.common.entity.QBaseTimeEntity _super = new com.project.hanspoon.common.entity.QBaseTimeEntity(this);

    public final StringPath answerContent = createString("answerContent");

    public final BooleanPath answered = createBoolean("answered");

    public final DateTimePath<java.time.LocalDateTime> answeredAt = createDateTime("answeredAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> answeredByUserId = createNumber("answeredByUserId", Long.class);

    public final StringPath category = createString("category");

    public final com.project.hanspoon.oneday.clazz.entity.QClassProduct classProduct;

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final BooleanPath hasAttachment = createBoolean("hasAttachment");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> legacyClassId = createNumber("legacyClassId", Long.class);

    public final BooleanPath legacySecret = createBoolean("legacySecret");

    public final EnumPath<com.project.hanspoon.oneday.inquiry.domain.InquiryStatus> status = createEnum("status", com.project.hanspoon.oneday.inquiry.domain.InquiryStatus.class);

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final EnumPath<com.project.hanspoon.oneday.inquiry.domain.Visibility> visibility = createEnum("visibility", com.project.hanspoon.oneday.inquiry.domain.Visibility.class);

    public QClassInquiry(String variable) {
        this(ClassInquiry.class, forVariable(variable), INITS);
    }

    public QClassInquiry(Path<? extends ClassInquiry> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QClassInquiry(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QClassInquiry(PathMetadata metadata, PathInits inits) {
        this(ClassInquiry.class, metadata, inits);
    }

    public QClassInquiry(Class<? extends ClassInquiry> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.classProduct = inits.isInitialized("classProduct") ? new com.project.hanspoon.oneday.clazz.entity.QClassProduct(forProperty("classProduct"), inits.get("classProduct")) : null;
    }

}

