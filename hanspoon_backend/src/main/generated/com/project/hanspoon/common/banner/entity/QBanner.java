package com.project.hanspoon.common.banner.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QBanner is a Querydsl query type for Banner
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBanner extends EntityPathBase<Banner> {

    private static final long serialVersionUID = -198933432L;

    public static final QBanner banner = new QBanner("banner");

    public final StringPath badgesJson = createString("badgesJson");

    public final NumberPath<Long> bannerId = createNumber("bannerId", Long.class);

    public final StringPath bg = createString("bg");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath eyebrow = createString("eyebrow");

    public final StringPath href = createString("href");

    public final StringPath imageAlt = createString("imageAlt");

    public final StringPath imageSrc = createString("imageSrc");

    public final BooleanPath isActive = createBoolean("isActive");

    public final StringPath period = createString("period");

    public final NumberPath<Integer> sortOrder = createNumber("sortOrder", Integer.class);

    public final StringPath title = createString("title");

    public final StringPath toPath = createString("toPath");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QBanner(String variable) {
        super(Banner.class, forVariable(variable));
    }

    public QBanner(Path<? extends Banner> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBanner(PathMetadata metadata) {
        super(Banner.class, metadata);
    }

}

