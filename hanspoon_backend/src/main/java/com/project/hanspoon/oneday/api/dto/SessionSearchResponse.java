package com.project.hanspoon.oneday.api.dto;

import com.project.hanspoon.oneday.clazz.domain.Level;
import com.project.hanspoon.oneday.clazz.domain.RecipeCategory;
import com.project.hanspoon.oneday.clazz.domain.RunType;
import com.project.hanspoon.oneday.clazz.domain.SessionSlot;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record SessionSearchResponse(
        Long sessionId,
        LocalDateTime startAt,
        SessionSlot slot,
        int price,
        int capacity,
        int reservedCount,
        boolean full,
        boolean completed,
        boolean available,

        Long classId,
        String classTitle,
        Level level,
        RunType runType,
        RecipeCategory category,

        Long instructorId,
        String instructorName
) { }
