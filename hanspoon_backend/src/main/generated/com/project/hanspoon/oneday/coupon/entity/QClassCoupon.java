package com.project.hanspoon.oneday.coupon.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QClassCoupon is a Querydsl query type for ClassCoupon
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QClassCoupon extends EntityPathBase<ClassCoupon> {

    private static final long serialVersionUID = 1257005747L;

    public static final QClassCoupon classCoupon = new QClassCoupon("classCoupon");

    public final com.project.hanspoon.common.entity.QBaseTimeEntity _super = new com.project.hanspoon.common.entity.QBaseTimeEntity(this);

    public final BooleanPath active = createBoolean("active");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final EnumPath<com.project.hanspoon.oneday.coupon.domain.DiscountType> discountType = createEnum("discountType", com.project.hanspoon.oneday.coupon.domain.DiscountType.class);

    public final NumberPath<Integer> discountValue = createNumber("discountValue", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> legacyUpdatedAt = createDateTime("legacyUpdatedAt", java.time.LocalDateTime.class);

    public final StringPath name = createString("name");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Integer> validDays = createNumber("validDays", Integer.class);

    public QClassCoupon(String variable) {
        super(ClassCoupon.class, forVariable(variable));
    }

    public QClassCoupon(Path<? extends ClassCoupon> path) {
        super(path.getType(), path.getMetadata());
    }

    public QClassCoupon(PathMetadata metadata) {
        super(ClassCoupon.class, metadata);
    }

}

