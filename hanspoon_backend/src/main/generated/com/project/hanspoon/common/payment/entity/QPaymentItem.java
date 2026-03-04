package com.project.hanspoon.common.payment.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPaymentItem is a Querydsl query type for PaymentItem
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPaymentItem extends EntityPathBase<PaymentItem> {

    private static final long serialVersionUID = -1023992055L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPaymentItem paymentItem = new QPaymentItem("paymentItem");

    public final NumberPath<Long> classId = createNumber("classId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath itemName = createString("itemName");

    public final QPayment payment;

    public final NumberPath<Long> productId = createNumber("productId", Long.class);

    public final NumberPath<Integer> quantity = createNumber("quantity", Integer.class);

    public QPaymentItem(String variable) {
        this(PaymentItem.class, forVariable(variable), INITS);
    }

    public QPaymentItem(Path<? extends PaymentItem> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPaymentItem(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPaymentItem(PathMetadata metadata, PathInits inits) {
        this(PaymentItem.class, metadata, inits);
    }

    public QPaymentItem(Class<? extends PaymentItem> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.payment = inits.isInitialized("payment") ? new QPayment(forProperty("payment"), inits.get("payment")) : null;
    }

}

