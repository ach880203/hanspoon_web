package com.project.hanspoon.common.payment.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPayment is a Querydsl query type for Payment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPayment extends EntityPathBase<Payment> {

    private static final long serialVersionUID = 1994492630L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPayment payment = new QPayment("payment");

    public final DateTimePath<java.time.LocalDateTime> payDate = createDateTime("payDate", java.time.LocalDateTime.class);

    public final NumberPath<Long> payId = createNumber("payId", Long.class);

    public final ListPath<PaymentItem, QPaymentItem> paymentItems = this.<PaymentItem, QPaymentItem>createList("paymentItems", PaymentItem.class, QPaymentItem.class, PathInits.DIRECT2);

    public final StringPath portOnePaymentId = createString("portOnePaymentId");

    public final EnumPath<com.project.hanspoon.common.payment.constant.PaymentStatus> status = createEnum("status", com.project.hanspoon.common.payment.constant.PaymentStatus.class);

    public final NumberPath<Integer> totalPrice = createNumber("totalPrice", Integer.class);

    public final com.project.hanspoon.common.user.entity.QUser user;

    public QPayment(String variable) {
        this(Payment.class, forVariable(variable), INITS);
    }

    public QPayment(Path<? extends Payment> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPayment(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPayment(PathMetadata metadata, PathInits inits) {
        this(Payment.class, metadata, inits);
    }

    public QPayment(Class<? extends Payment> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.project.hanspoon.common.user.entity.QUser(forProperty("user")) : null;
    }

}

