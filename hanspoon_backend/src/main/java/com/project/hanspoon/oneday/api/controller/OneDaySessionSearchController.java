package com.project.hanspoon.oneday.api.controller;

import com.project.hanspoon.common.response.ApiResponse;
import com.project.hanspoon.oneday.api.dto.SessionSearchResponse;
import com.project.hanspoon.oneday.api.service.OneDaySessionSearchService;
import com.project.hanspoon.oneday.clazz.domain.Level;
import com.project.hanspoon.oneday.clazz.domain.RecipeCategory;
import com.project.hanspoon.oneday.clazz.domain.RunType;
import com.project.hanspoon.oneday.clazz.domain.SessionSlot;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oneday/sessions")
public class OneDaySessionSearchController {

    private final OneDaySessionSearchService service;

    @GetMapping("/search")
    public ApiResponse<List<SessionSearchResponse>> search(
            @RequestParam(required = false) Level level,
            @RequestParam(required = false) RecipeCategory category,
            @RequestParam(required = false) RunType runType,
            @RequestParam(required = false) SessionSlot slot,
            @RequestParam(required = false) Long instructorId,
            @RequestParam(required = false) String instructorName,
            @RequestParam(required = false) Boolean onlyAvailable,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo) {
        return ApiResponse.ok(service.search(level, category, runType, slot, instructorId, instructorName, dateFrom,
                dateTo, onlyAvailable, keyword, sort));
    }
}
