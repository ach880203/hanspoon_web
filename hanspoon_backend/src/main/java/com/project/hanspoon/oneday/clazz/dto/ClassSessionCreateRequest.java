package com.project.hanspoon.oneday.clazz.dto;

import com.project.hanspoon.oneday.clazz.domain.SessionSlot;

import java.time.LocalDateTime;

/**
 * 원데이 클래스 등록 시 세션 1건을 표현하는 요청 DTO입니다.
 * 초보자 참고:
 * - 클래스 1개 안에 여러 세션(오전/오후/다른 날짜)을 넣을 수 있습니다.
 * - 프런트에서 sessions 배열로 여러 건을 보내면, 서버가 반복 저장합니다.
 */
public record ClassSessionCreateRequest(
        LocalDateTime startAt,
        SessionSlot slot,
        Integer capacity,
        Integer price
) {}
