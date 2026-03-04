package com.project.hanspoon.oneday.home.dto;

import com.project.hanspoon.oneday.clazz.dto.ClassListItemResponse;

import java.util.List;

public record ClassHomeResponse(

    List<ClassListItemResponse> eventClasses,
    List<ClassListItemResponse> alwaysClasses
){ }
