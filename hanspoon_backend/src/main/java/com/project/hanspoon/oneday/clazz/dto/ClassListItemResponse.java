package com.project.hanspoon.oneday.clazz.dto;

import com.project.hanspoon.oneday.clazz.domain.Level;
import com.project.hanspoon.oneday.clazz.domain.RecipeCategory;
import com.project.hanspoon.oneday.clazz.domain.RunType;
import com.project.hanspoon.oneday.clazz.entity.ClassProduct;

public record ClassListItemResponse(
        Long id,
        String title,
        String mainImageData,
        Level level,
        RunType runType,
        RecipeCategory category,
        Long instructorId,
        String instructorName
) {
    public static ClassListItemResponse from(ClassProduct p) {
        Long instructorId = (p.getInstructor() != null) ? p.getInstructor().getId() : null;
        String instructorName = (p.getInstructor() != null && p.getInstructor().getUser() != null)
                ? p.getInstructor().getUser().getUserName()
                : null;

        return new ClassListItemResponse(
                p.getId(),
                p.getTitle(),
                p.getDetailImageData(),
                p.getLevel(),
                p.getRunType(),
                p.getCategory(),
                instructorId,
                instructorName
        );
    }
}
