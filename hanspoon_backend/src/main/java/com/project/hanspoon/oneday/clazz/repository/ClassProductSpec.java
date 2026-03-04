package com.project.hanspoon.oneday.clazz.repository;

import com.project.hanspoon.oneday.clazz.domain.*;
import com.project.hanspoon.oneday.clazz.entity.ClassProduct;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class ClassProductSpec {

    public static Specification<ClassProduct> search(
            Level level,
            RunType runType,
            RecipeCategory category,
            Long instructorId,
            String instructorName,
            String keyword) {
        return (root, query, cb) -> {
            // 목록 조회에서 N+1 방지(Count 쿼리에는 fetch 하면 안 됨)
            if (!Long.class.equals(query.getResultType()) && !long.class.equals(query.getResultType())) {
                root.fetch("instructor", JoinType.LEFT);
                query.distinct(true);
            }

            var predicates = cb.conjunction();

            if (level != null)
                predicates = cb.and(predicates, cb.equal(root.get("level"), level));
            if (runType != null)
                predicates = cb.and(predicates, cb.equal(root.get("runType"), runType));
            if (category != null)
                predicates = cb.and(predicates, cb.equal(root.get("category"), category));
            if (instructorId != null)
                predicates = cb.and(predicates, cb.equal(root.get("instructor").get("id"), instructorId));
            if (instructorName != null && !instructorName.trim().isEmpty()) {
                String normalizedInstructorName = "%" + instructorName.trim().toLowerCase() + "%";
                predicates = cb.and(predicates,
                        cb.like(cb.lower(root.get("instructor").get("user").get("userName")), normalizedInstructorName));
            }

            if (keyword != null && !keyword.trim().isEmpty()) {
                predicates = cb.and(predicates, cb.like(root.get("title"), "%" + keyword.trim() + "%"));
            }

            return predicates;
        };
    }
}
