package com.project.hanspoon.oneday.clazz.dto;

import java.util.List;

/**
 * 클래스 등록 완료 응답 DTO입니다.
 * 초보자 참고:
 * - 등록 결과를 화면에서 바로 보여줄 수 있도록
 *   생성된 classId와 생성된 sessionId 목록을 함께 반환합니다.
 */
public record ClassCreateResponse(
        Long classId,
        String title,
        int createdSessionCount,
        List<Long> sessionIds
) {}
