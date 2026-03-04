package com.project.hanspoon.oneday.clazz.dto;

import com.project.hanspoon.oneday.clazz.domain.Level;
import com.project.hanspoon.oneday.clazz.domain.RecipeCategory;
import com.project.hanspoon.oneday.clazz.domain.RunType;

import java.util.List;

/**
 * 원데이 클래스 수정 요청 DTO
 *
 * create 요청과 거의 동일하지만, 수정 API 의도를 분명히 하기 위해 별도 DTO로 분리했다.
 * - classId는 URL 경로 변수로 받는다.
 * - sessions는 수정 시점의 최종 상태 전체를 전달한다.
 */
public record ClassUpdateRequest(
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
