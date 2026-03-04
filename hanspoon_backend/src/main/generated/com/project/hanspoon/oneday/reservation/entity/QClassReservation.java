package com.project.hanspoon.oneday.reservation.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QClassReservation is a Querydsl query type for ClassReservation
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QClassReservation extends EntityPathBase<ClassReservation> {

    private static final long serialVersionUID = 378276277L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QClassReservation classReservation = new QClassReservation("classReservation");

    public final com.project.hanspoon.common.entity.QBaseTimeEntity _super = new com.project.hanspoon.common.entity.QBaseTimeEntity(this);

    public final DateTimePath<java.time.LocalDateTime> canceledAt = createDateTime("canceledAt", java.time.LocalDateTime.class);

    public final StringPath cancelReason = createString("cancelReason");

    public final DateTimePath<java.time.LocalDateTime> cancelRequestedAt = createDateTime("cancelRequestedAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> completedAt = createDateTime("completedAt", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> holdExpiredAt = createDateTime("holdExpiredAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> legacyUpdatedAt = createDateTime("legacyUpdatedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> legacyUserId = createNumber("legacyUserId", Long.class);

    public final NumberPath<Long> linkedPayId = createNumber("linkedPayId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> paidAt = createDateTime("paidAt", java.time.LocalDateTime.class);

    public final com.project.hanspoon.oneday.clazz.entity.QClassSession session;

    public final EnumPath<com.project.hanspoon.oneday.reservation.domain.ReservationStatus> status = createEnum("status", com.project.hanspoon.oneday.reservation.domain.ReservationStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.project.hanspoon.common.user.entity.QUser user;

    public QClassReservation(String variable) {
        this(ClassReservation.class, forVariable(variable), INITS);
    }

    public QClassReservation(Path<? extends ClassReservation> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QClassReservation(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QClassReservation(PathMetadata metadata, PathInits inits) {
        this(ClassReservation.class, metadata, inits);
    }

    public QClassReservation(Class<? extends ClassReservation> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.session = inits.isInitialized("session") ? new com.project.hanspoon.oneday.clazz.entity.QClassSession(forProperty("session"), inits.get("session")) : null;
        this.user = inits.isInitialized("user") ? new com.project.hanspoon.common.user.entity.QUser(forProperty("user")) : null;
    }

}

