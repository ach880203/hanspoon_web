package com.project.hanspoon.oneday.clazz.dto;

import com.project.hanspoon.oneday.clazz.domain.Level;
import com.project.hanspoon.oneday.clazz.domain.RecipeCategory;
import com.project.hanspoon.oneday.clazz.domain.RunType;

import java.util.List;

/**
 * 원데이 클래스 등록 요청 DTO입니다.
 * 초보자 참고:
 * - 클래스 기본 정보 + 세션 목록을 한 번에 받기 위해 DTO를 분리했습니다.
 * - sessions는 최소 1개 이상 필요합니다.
 */
public record ClassCreateRequest(
        String title,
        String description,
        String detailDescription,
        String detailImageData,
        List<String> detailImageDataList,
        Level level,
        RunType runType,
        RecipeCategory category,
        Long instructorId,
        String locationAddress,
        Double locationLat,
        Double locationLng,
        List<ClassSessionCreateRequest> sessions
) {}
