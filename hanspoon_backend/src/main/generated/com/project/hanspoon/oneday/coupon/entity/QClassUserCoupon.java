package com.project.hanspoon.oneday.coupon.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QClassUserCoupon is a Querydsl query type for ClassUserCoupon
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QClassUserCoupon extends EntityPathBase<ClassUserCoupon> {

    private static final long serialVersionUID = -2098534434L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QClassUserCoupon classUserCoupon = new QClassUserCoupon("classUserCoupon");

    public final com.project.hanspoon.common.entity.QBaseTimeEntity _super = new com.project.hanspoon.common.entity.QBaseTimeEntity(this);

    public final QClassCoupon coupon;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> expiresAt = createDateTime("expiresAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> issuedAt = createDateTime("issuedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> reservationId = createNumber("reservationId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final DateTimePath<java.time.LocalDateTime> usedAt = createDateTime("usedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QClassUserCoupon(String variable) {
        this(ClassUserCoupon.class, forVariable(variable), INITS);
    }

    public QClassUserCoupon(Path<? extends ClassUserCoupon> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QClassUserCoupon(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QClassUserCoupon(PathMetadata metadata, PathInits inits) {
        this(ClassUserCoupon.class, metadata, inits);
    }

    public QClassUserCoupon(Class<? extends ClassUserCoupon> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.coupon = inits.isInitialized("coupon") ? new QClassCoupon(forProperty("coupon")) : null;
    }

}

