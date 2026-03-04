package com.project.hanspoon.common.response;

import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * ============================================================================
 * 👨‍🏫 [10년 차 백엔드 개발자 & 보안 아키텍트의 코드 해부 강의]
 * 주제: ApiResponse (팀 내 공통 통신 규격)
 * ============================================================================
 *
 * 💡 [비유]
 * "우체국의 표준 규격 택배 상자"
 * 프론트엔드가 백엔드에 요청을 보냈을 때, 무작위 형태의 데이터가 아니라
 * 항상 예측 가능한 똑같은 상자(형식)에 내용물을 담아 보내기 위한 약속입니다.
 *
 * 🔄 [내부 동작 순서 (실행 흐름)]
 * 1. 컨트롤러(Controller)에서 비즈니스 로직(Service)의 결과를 반환받음
 * 2. 결과를 클라이언트에게 보내기 직전, `ApiResponse.ok()` 또는 `fail()` 호출
 * 3. 데이터(data), 성공여부(success), 메시지(message), 발생시간(timestamp)이 하나의 규격으로 포장됨
 * 4. 포장된 Record 객체가 JSON 형태로 직렬화(Serialization)되어 프론트엔드로 전송됨
 *
 * ⚠️ [보안/운영 위험 요소 (Risk & Security)]
 * - [위험도: 🟡 MED] 타임존(Timezone) 파편화: `Asia/Seoul`로 하드코딩 되어 있습니다. 만약 글로벌 서비스를
 * 하거나, 프론트엔드에서 UTC를 기준으로 시간을 계산한다면 시간 오차가 발생할 수 있습니다. 시스템 기본 타임존이나 UTC를 사용하는 것이
 * 일반적인 Best Practice입니다.
 * - [위험도: 🟡 LOW] 에러 메시지 노출: `fail()` 메서드 사용 시, 내부 시스템의 상세 에러(예: SQL Syntax
 * Error 등)가 그대로 message 필드에 담겨 프론트엔드로 노출되지 않도록 주의해야 합니다. (공격자에게 시스템 힌트를 제공할 수
 * 있음)
 * ============================================================================
 */
public record ApiResponse<T>(
        /*
         * [의미 단위 1: 공통 응답 필드 정의]
         * - 기능: 응답 성공 여부, 실제 데이터, 메시지, 에러코드, 타임스탬프를 담는 필드들입니다.
         * - 왜 필요한지: 프론트엔드에서 `res.data.success` 와 같이 일관된 방식으로 로직(성공/실패 분기)을 처리할 수 있게
         * 해줍니다.
         * - 실행 흐름 위치: 객체가 생성될 때 불변(Immutable) 상태로 필드가 채워짐. (Record의 특징)
         */
        boolean success,
        T data,
        String message,
        String errorCode,
        OffsetDateTime timestamp) {
    // 타임존을 한국 시간(KST)으로 고정. ( UTC 변환⚠️ 향후 다국어/글로벌 서비스 시 고려 필요)
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    /*
     * [의미 단위 2: 성공(OK) 응답 팩토리 메서드]
     * - 기능: 데이터가 있는 성공 응답을 만들 때 사용하는 정적(Static) 메서드입니다.
     * - 왜 필요한지: 매번 `new ApiResponse<>(true, data, ...)` 처럼 긴 생성자를 호출하는 불편함을 줄이고
     * 가독성을 높입니다.
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, "정상 처리되었습니다.", null, OffsetDateTime.now(KST));
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, data, message, null, OffsetDateTime.now(KST));
    }

    public static <T> ApiResponse<T> ok(String message) {
        return new ApiResponse<>(true, null, message, null, OffsetDateTime.now(KST));
    }

    /*
     * [의미 단위 3: 실패/예외(FAIL) 응답 팩토리 메서드]
     * - 기능: 예외가 발생했을 때 일관된 실패 규격을 만드는 정적 메서드입니다.
     * - 왜 필요한지: GlobalExceptionHandler 등에서 에러 발생 시, 구조가 깨지지 않고 클라이언트에게 원인을 전달하기
     * 위함입니다.
     */
    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(false, null, message, "잘못된 요청입니다.", OffsetDateTime.now(KST));
    }

    public static <T> ApiResponse<T> fail(String message, String errorCode) {
        return new ApiResponse<>(false, null, message, errorCode, OffsetDateTime.now(KST));
    }

    // 기본 에러 처리용 (fail과 내부 동작은 동일)
    public static <T> ApiResponse<T> error(String message) {
        return fail(message, "잘못된 요청입니다.");
    }
}

/*
 * ============================================================================
 * 📊 [요약 테이블]
 * | 구분 | 내용 | 비고 (아키텍트 코멘트) |
 * |-------------|-----------------------------------------|--------------------
 * ------------------|
 * | 핵심 목적 | 백엔드-프론트엔드 간의 API 통신 규격 통일 | 프론트엔드의 에러 핸들링을 극도로 단순하게 만듦 |
 * | 구현 방식 | Java 14+ `record` 키워드 사용 | 불변성(Immutable) 보장 및
 * 보일러플레이트(Getter/Setter) 제거 |
 * | 주요 데이터 필드 | success, data, message, errorCode | RESTful API 표준 트렌드 반영 |
 * 
 * ✅ [배포 전 체크리스트 (Pre-Deployment Checklist)]
 * [ ] 1. 중복 클래스 제거: `common.dto.ApiResponse` 제거 완료 (단일 진실 공급원 확보 성공)
 * [ ] 2. 시간대(Timezone) 정책: 프론트엔드와 논의하여 응답의 `timestamp`를 KST로 유지할지, ISO-8601 기반
 * UTC(Z)로 내보낼지 확정할 것.
 * ============================================================================
 */
