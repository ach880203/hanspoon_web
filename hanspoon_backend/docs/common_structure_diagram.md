# `com.project.hanspoon.common` 구조도 및 흐름

이 문서는 `hanspoon_back_0211/src/main/java/com/project/hanspoon/common` 패키지의 클래스 관계, 의존성 방향, 요청 처리 흐름을 간단한 ASCII 다이어그램과 함께 정리한 문서입니다.

---

## 1. 클래스 관계 설명 (요약)
- Controller 계층: 클라이언트 요청 진입점
  - 예: `AuthController`, `PaymentController`, `NoticeController`, `FaqController`, `AdminUserController`, `AdminPaymentController`, `AdminNoticeController`, `AdminFaqController`, `HomeController`
- Service 계층: 비즈니스 로직
  - 예: `UserService`, `PaymentService`, `PortOneService`, `NoticeService`, `FaqService`
- Repository 계층: 데이터 접근(JPA)
  - 예: `UserRepository`, `PaymentRepository`, `NoticeRepository`, `FaqRepository`, `PaymentItemRepository`
- Entity: DB 매핑 모델
  - 예: `User`, `Payment`, `PaymentItem`, `Notice`, `Faq`, `BaseTimeEntity`
- DTO / Response: 컨트롤러와 클라이언트 간 데이터 포맷
  - 예: `LoginRequest`, `LoginResponse`, `UserRegisterDto`, `PaymentDto`, `NoticeDto`, `FaqDto`, `PageResponse`, `ApiResponse`
- Security: 인증·인가 관련
  - `CustomUserDetailsService`, `CustomUserDetails`, `CustomOAuth2UserService`, `OAuth2AuthenticationSuccessHandler`, `JwtTokenProvider`, `JwtAuthenticationFilter`
- Config: 설정/빈
  - `SecurityConfig`, `MethodSecurityConfig`, `PortOneConfig`, `PasswordEncoderConfig`, `DataInitializer`
- Exception: 전역 예외 처리
  - `BusinessException`, `GlobalExceptionHandler`

관계 요약: Controller -> Service -> Repository -> Entity(저장/조회). Security는 필터/핸들러 형태로 횡단적(공통) 의존을 가짐.

---

## 2. 의존성 방향 설명
- 기본 방향: Controller → Service → Repository → Database(Entity)
- DTO 및 ApiResponse는 Controller ↔ 클라이언트 입출력용으로 사용
- 인증 흐름:
  - `SecurityConfig` → Security 필터체인 등록
  - `JwtAuthenticationFilter` → `JwtTokenProvider`, `CustomUserDetailsService`
  - 인증 시 `CustomUserDetailsService` → `UserRepository` → DB
- OAuth2 흐름:
  - 스프링 OAuth2 infra → `CustomOAuth2UserService` → `UserRepository` → (필요 시) `CustomUserDetails` 반환 → `OAuth2AuthenticationSuccessHandler` → `JwtTokenProvider`
- 외부 API:
  - `PortOneService` → `WebClient`(외부 결제 API) → 응답을 파싱 → `PaymentRepository`로 저장

의존성 규칙(권장 관찰 포인트):
- 서비스는 리포지토리와 DTO를 사용하되 컨트롤러에 의존하지 않음
- 리포지토리는 엔티티 외에는 의존하지 않음
- 보안 컴포넌트(필터/핸들러)는 서비스/리포지토리를 호출하여 사용자 정보를 조회할 수 있음

---

## 3. 요청 처리 흐름 (핵심 시나리오 요약)
아래는 대표 시나리오별 핵심 호출 순서와 분기, 최종 반환값입니다.

A) 일반 API 요청(인증 필요 예: 결제 히스토리 조회)
1. Client → HTTP 요청 (Authorization: Bearer <token> 가능)
2. 스프링 필터체인 진입 (`SecurityFilterChain`)
3. `JwtAuthenticationFilter.doFilterInternal`
   - Authorization 헤더에서 JWT 추출
   - `JwtTokenProvider.validateToken(jwt)` 호출
   - 유효하면 `JwtTokenProvider.getEmailFromToken(jwt)` 호출
   - `CustomUserDetailsService.loadUserByUsername(email)` 호출 → `UserRepository` 조회
   - `SecurityContextHolder`에 인증정보 저장
4. 컨트롤러(`PaymentController`) 실행 → `paymentService.getPaymentHistory(...)` 호출
5. `PaymentService` → `PaymentRepository` → DB 조회
6. 결과 DTO 변환 후 `ApiResponse`로 응답

조건 분기: 토큰 없음/유효하지 않음 → 인증 미설정 → 보호된 리소스 접근 시 401/403 반환

최종 반환값: `ApiResponse.success(PageResponse<PaymentDto>)` 또는 인증 오류 응답

B) 이메일 로그인
1. Client → `POST /api/auth/login` (body: `LoginRequest`)
2. `AuthController.login` → `AuthenticationManager.authenticate(...)`
3. 내부적으로 `CustomUserDetailsService.loadUserByUsername(email)` 호출 → DB 조회
4. 인증 성공 → `JwtTokenProvider.createToken(authentication)` 호출
5. `userService.updateLastLogin(userId)` 호출
6. `AuthController`가 `LoginResponse` (accessToken 포함) 반환

조건 분기: 인증 실패(잘못된 자격증명, 정지 계정, 탈퇴) → 예외 처리 및 에러 메시지 반환

최종 반환값: `ApiResponse.success("로그인 성공", LoginResponse)` 또는 `ApiResponse.error(...)`

C) OAuth2 소셜 로그인
1. 브라우저 → OAuth2 제공자 → 인증 성공 시 스프링 콜백
2. `CustomOAuth2UserService.loadUser(userRequest)` 실행
   - `super.loadUser(userRequest)`로 attributes 획득
   - `extractUserInfo(provider, attributes)`로 표준화된 정보 추출
   - `findOrCreateUser(provider, userInfo)` 실행
     - provider+providerId로 조회 → 있으면 반환
     - 이메일로 조회 → 있으면 provider 연동 후 저장
     - 없으면 신규 User 생성(임의 비밀번호) 및 저장
   - `CustomUserDetails` 반환
3. `OAuth2AuthenticationSuccessHandler.onAuthenticationSuccess` 호출
   - `JwtTokenProvider.createToken(authentication)` 호출
   - 프론트 리다이렉트 URL에 `token` 포함하여 리다이렉트

분기: 카카오 등에서 이메일 미제공 시 합성 이메일 생성(예: kakao_<id>@kakao.local)

최종 반환값: 브라우저 리다이렉트(URL: /oauth2/redirect?token=...)

D) 결제 검증 (PortOne)
1. Client → `POST /api/payment/verify` (body: `PaymentVerifyRequest`)
2. `PaymentController.verifyPayment` → 인증 확인
3. `PortOneService.verifyAndSavePayment(user, paymentId, request)` 호출
   - `getPaymentFromPortOne(paymentId)` → `portOneWebClient`로 외부 API 호출
   - JSON 응답을 파싱(Wrapper 우선 → Direct)
   - 금액 비교: 응답.amount.total == request.amount ?
     - 아니면 실패 반환
   - 상태 확인: status == "PAID" ?
     - 아니면 실패 반환
   - `Payment`/`PaymentItem` 엔티티 생성 및 `paymentRepository.save(payment)`
4. `PortOneDto.PaymentResult` 반환 → 컨트롤러가 `ApiResponse`로 응답

분기: 인증 없음(401), 포트원 응답 누락/파싱 실패, 금액 불일치, 미결제 상태 → 실패 응답

최종 반환값: 성공 시 `ApiResponse.success("결제가 완료되었습니다.", PaymentResult)`

---

## 4. 간단한 ASCII 다이어그램

전체 개요
```
[Client]
   |
   v
[SecurityFilterChain] <-- SecurityConfig
   |
   +--> [JwtAuthenticationFilter] --> [JwtTokenProvider]
   |                                   ^
   |                                   |
   |                              [CustomUserDetailsService]
   v
[Controller] --> [Service] --> [Repository] --> [Database]
   |               |              ^
   |               |              |
   |               +--> [External API] (PortOne via WebClient)
   |
   +--> DTOs/ApiResponse (입출력)
```

인증 / 로그인 상세
```
[Client Login Request]
   |
   v
[AuthController]
   |
   +--> AuthenticationManager.authenticate()
            |
            v
  [CustomUserDetailsService] -> [UserRepository] -> DB(User)
            |
            v
   JwtTokenProvider.createToken()
            |
            v
   Response(LoginResponse w/ token)
```

OAuth2 상세
```
[OAuth2 Provider] --> (callback) --> [CustomOAuth2UserService.loadUser]
                                     |
                         extractUserInfo & findOrCreateUser
                                     |
                             [UserRepository] -> DB(User)
                                     |
                         return CustomUserDetails -> Security
                                     |
                     OAuth2AuthenticationSuccessHandler -> JwtTokenProvider
                                     |
                         Redirect to Frontend with token
```

---

## 5. 참고 및 권장 점검 포인트
- 컨트롤러에서 직접 비즈니스 로직을 두지 않고 서비스로 위임했는지 확인하세요.
- `SecurityConfig`의 URL 권한 설정과 `@PreAuthorize` 등이 의도한 대로 작동하는지 테스트하세요.
- 외부 API 응답 포맷이 바뀌면 `PortOneService.getPaymentFromPortOne`의 파싱 로직을 점검하세요.

---

문서 저장 위치: `docs/common_structure_diagram.md`

원하시면 이 문서를 PlantUML 형식으로 변환하거나, 특정 시나리오(예: 결제 검증)의 시퀀스 다이어그램을 더 상세히 만들어 파일로 추가해 드리겠습니다.