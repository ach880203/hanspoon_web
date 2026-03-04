package com.project.hanspoon.oneday.api.spec;

import com.project.hanspoon.oneday.clazz.domain.Level;
import com.project.hanspoon.oneday.clazz.domain.RecipeCategory;
import com.project.hanspoon.oneday.clazz.domain.RunType;
import com.project.hanspoon.oneday.clazz.domain.SessionSlot;
import com.project.hanspoon.oneday.clazz.entity.ClassSession;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public final class ClassSessionSpecs {

    private ClassSessionSpecs() {
    }

    public static Specification<ClassSession> fetchAll() {
        return (root, query, cb) -> {
            // count query일 때 fetch 걸면 오류날 수 있어서 방어
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("classProduct", JoinType.INNER).fetch("instructor", JoinType.LEFT);
                // instructor 안에 user까지 필요하면 fetch("user") 추가 가능
            }
            return cb.conjunction();
        };
    }

    public static Specification<ClassSession> startAtFrom(LocalDateTime from) {
        return (root, query, cb) -> from == null ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("startAt"), from);
    }

    public static Specification<ClassSession> startAtTo(LocalDateTime to) {
        return (root, query, cb) -> to == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("startAt"), to);
    }

    public static Specification<ClassSession> slot(SessionSlot slot) {
        return (root, query, cb) -> slot == null ? cb.conjunction() : cb.equal(root.get("slot"), slot);
    }

    public static Specification<ClassSession> level(Level level) {
        return (root, query, cb) -> {
            if (level == null)
                return cb.conjunction();
            return cb.equal(root.get("classProduct").get("level"), level);
        };
    }

    public static Specification<ClassSession> category(RecipeCategory category) {
        return (root, query, cb) -> category == null ? cb.conjunction()
                : cb.equal(root.get("classProduct").get("category"), category);
    }

    public static Specification<ClassSession> runType(RunType runType) {
        return (root, query, cb) -> runType == null ? cb.conjunction()
                : cb.equal(root.get("classProduct").get("runType"), runType);
    }

    public static Specification<ClassSession> instructorId(Long instructorId) {
        return (root, query, cb) -> {
            if (instructorId == null)
                return cb.conjunction();
            return cb.equal(root.get("classProduct").get("instructor").get("id"), instructorId);
        };
    }

    public static Specification<ClassSession> instructorNameContains(String instructorName) {
        return (root, query, cb) -> {
            if (instructorName == null || instructorName.trim().isEmpty())
                return cb.conjunction();
            String normalizedInstructorName = "%" + instructorName.trim().toLowerCase() + "%";
            return cb.like(
                    cb.lower(root.get("classProduct").get("instructor").get("user").get("userName")),
                    normalizedInstructorName);
        };
    }

    public static Specification<ClassSession> titleContains(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty())
                return cb.conjunction();
            return cb.like(root.get("classProduct").get("title"), "%" + keyword.trim() + "%");
        };
    }

    public static Specification<ClassSession> onlyAvailable(Boolean onlyAvailable) {
        return (root, query, cb) -> {
            if (onlyAvailable == null || !onlyAvailable)
                return cb.conjunction();
            // 예약 가능 세션은 "좌석이 남아 있고" + "아직 시작 전"이어야 합니다.
            return cb.and(
                    cb.greaterThan(root.get("capacity"), root.get("reservedCount")),
                    cb.greaterThan(root.get("startAt"), LocalDateTime.now(java.time.ZoneId.of("Asia/Seoul"))));
        };
    }
}
