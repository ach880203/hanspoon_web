package com.project.hanspoon.shop.shipping.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QShippingAddress is a Querydsl query type for ShippingAddress
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QShippingAddress extends EntityPathBase<ShippingAddress> {

    private static final long serialVersionUID = -578188845L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QShippingAddress shippingAddress = new QShippingAddress("shippingAddress");

    public final StringPath address1 = createString("address1");

    public final StringPath address2 = createString("address2");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final BooleanPath isDefault = createBoolean("isDefault");

    public final StringPath label = createString("label");

    public final StringPath receiverName = createString("receiverName");

    public final StringPath receiverPhone = createString("receiverPhone");

    public final NumberPath<Long> shippingAddressId = createNumber("shippingAddressId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final com.project.hanspoon.common.user.entity.QUser user;

    public final StringPath zipCode = createString("zipCode");

    public QShippingAddress(String variable) {
        this(ShippingAddress.class, forVariable(variable), INITS);
    }

    public QShippingAddress(Path<? extends ShippingAddress> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QShippingAddress(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QShippingAddress(PathMetadata metadata, PathInits inits) {
        this(ShippingAddress.class, metadata, inits);
    }

    public QShippingAddress(Class<? extends ShippingAddress> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.project.hanspoon.common.user.entity.QUser(forProperty("user")) : null;
    }

}

