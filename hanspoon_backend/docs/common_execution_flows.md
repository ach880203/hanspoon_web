# `com.project.hanspoon.common` 실행 흐름 정리

아래는 `hanspoon_back_0211/src/main/java/com/project/hanspoon/common` 패키지의 주요 기능별 실행 흐름입니다.
각 항목은 요청 시작 지점, 호출 순서(핵심 호출만), 분기 조건, 최종 반환값을 순서 번호로 정리했습니다.

---

1) 애플리케이션 시작 시 초기 관리자 계정 생성 (`DataInitializer`)
1. 요청 시작 지점: Spring Boot 애플리케이션 시작 시 `CommandLineRunner.run()` 호출
2. 호출 순서:
   1. `DataInitializer.run(...)`
   2. `userRepository.findByEmail("admin@example.com")`
   3. Optional 결과에 따라 `userRepository.save(...)`
3. 조건문 분기:
   - 존재하면: 기존 `User` 엔티티의 `setRole(...)`, `setStatus(...)` 호출 후 `save`(업데이트)
   - 없으면: `User.builder()`로 새 계정 생성(비밀번호는 `passwordEncoder.encode("admin1234")`) 후 `save`
4. 최종 반환값: 없음(프로세스 종료 시 로그 출력만). 데이터베이스에 관리자 레코드가 생성/업데이트됨.

---

2) 서버 상태/정보 조회 (`HomeController`)
1. 요청 시작 지점: HTTP `GET /api/health` 또는 `GET /api/info`
2. 호출 순서:
   1. `HomeController.health()` 또는 `HomeController.info()`
   2. 내부에서 상태/정보 Map 생성
3. 조건문 분기: 없음(항상 성공적으로 현재 상태 반환)
4. 최종 반환값: `ResponseEntity.ok(ApiResponse.success(map))` JSON 형태(성공 여부 + 데이터)

---

3) 일반 로그인 (이메일 + 비밀번호) (`AuthController.login`)
1. 요청 시작 지점: HTTP `POST /api/auth/login` (body: `LoginRequest`)
2. 호출 순서:
   1. `AuthController.login(request)`
   2. `authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password))`
   3. (스프링 시큐리티 내부) `CustomUserDetailsService.loadUserByUsername(email)` 호출 → `userRepository.findByEmailAndIsDeletedFalse(email)`
   4. 인증 성공 시 `SecurityContextHolder.setAuthentication(authentication)`
   5. `jwtTokenProvider.createToken(authentication)` → 토큰 생성
   6. `userService.updateLastLogin(user.getUserId())`
   7. `LoginResponse` 생성 후 `ResponseEntity.ok(ApiResponse.success("로그인 성공", response))`
3. 조건문 분기:
   - 인증 실패: `BadCredentialsException` → 400, `ApiResponse.error("이메일 또는 비밀번호가 올바르지 않습니다.")`
   - 계정 정지: `LockedException` → 400, 정지 메시지
   - 탈퇴: `DisabledException` → 400, 탈퇴 메시지
   - 그 외 예외: 400, 서버 오류 메시지
4. 최종 반환값: 성공 시 `ApiResponse`에 포함된 `LoginResponse` (accessToken 포함). 실패 시 `ApiResponse.error` 반환.

---

4) 회원가입 (`AuthController.register` → `UserService.register`)
1. 요청 시작 지점: HTTP `POST /api/auth/register` (body: `UserRegisterDto`)
2. 호출 순서:
   1. `AuthController.register(dto)`
   2. `UserService.register(dto)`
   3. `userRepository.existsByEmail(dto.getEmail())` (중복 검사)
   4. 비밀번호 일치 검사
   5. `passwordEncoder.encode(dto.getPassword())` 후 `userRepository.save(user)`
3. 조건문 분기:
   - 이메일 중복: `IllegalArgumentException` → 컨트롤러에서 400 반환
   - 비밀번호 불일치: `IllegalArgumentException` → 400 반환
4. 최종 반환값: 성공 시 `ApiResponse.success("회원가입이 완료되었습니다.")`

---

5) 이메일 중복 확인 (`AuthController.checkEmail`)
1. 요청 시작 지점: HTTP `GET /api/auth/check-email?email=...`
2. 호출 순서: `AuthController.checkEmail(email)` → `userService.isEmailExists(email)` → `userRepository.existsByEmail(email)`
3. 조건문 분기: 사용 가능 여부에 따라 메시지와 boolean 값 결정
4. 최종 반환값: `ApiResponse.success(message, available)` (available: true=사용가능)

---

6) OAuth2 소셜 로그인 전체 흐름 (구글/네이버/카카오)
1. 요청 시작 지점: 브라우저에서 OAuth2 로그인 시도 → 스프링 시큐리티의 OAuth2 로그인 엔드포인트 시작
2. 호출 순서 (핵심):
   1. 외부 제공자에서 인증 후 스프링으로 콜백 → `DefaultOAuth2UserService.loadUser()` 내부 호출
   2. 오버라이드된 `CustomOAuth2UserService.loadUser(userRequest)` 호출
   3. `super.loadUser(userRequest)` → 공급자에서 `attributes` 획득
   4. `extractUserInfo(provider, attributes)`로 providerId/email/name 추출 (공급자별 분기)
   5. `findOrCreateUser(provider, userInfo)`
      - 1) `userRepository.findByProviderAndProviderId(provider, providerId)`
      - 2) 없으면 `userRepository.findByEmail(email)` (기존 일반 회원인지 확인) → 있으면 provider 연동 후 save
      - 3) 없으면 신규 `User` 생성(임의 비밀번호 사용) 후 save
   6. `new CustomUserDetails(user, attributes)` 반환 → 인증 성공
   7. 성공 시 `OAuth2AuthenticationSuccessHandler.onAuthenticationSuccess()` 실행
   8. `jwtTokenProvider.createToken(authentication)` 로 JWT 생성 → 프론트엔드 리다이렉트 URL에 `token` 쿼리로 포함하여 redirect
3. 조건문 분기:
   - 지원하지 않는 provider: `OAuth2AuthenticationException`
   - 카카오의 경우 이메일 제공 권한 없음 → `kakao_<id>@kakao.local` 형태의 합성 이메일 생성
   - 기존 providerId가 있으면 기존 사용자 반환(로그인)
   - 이메일로 기존 일반 계정이 있으면 provider 연동(기존 계정에 provider 정보 붙임)
4. 최종 반환값: 브라우저 리다이렉트(프론트의 `/oauth2/redirect?token=...`) 또는 OAuth2 성공 엔드포인트에서 JSON 토큰 반환(구성에 따라)

---

7) 모든 요청에 대한 JWT 인증 필터 흐름 (`JwtAuthenticationFilter`)
1. 요청 시작 지점: 모든 HTTP 요청(필터체인 진입)
2. 호출 순서:
   1. `JwtAuthenticationFilter.doFilterInternal(request, response, chain)`
   2. `getJwtFromRequest(request)` → `Authorization` 헤더에서 `Bearer ` 토큰 추출
   3. `jwtTokenProvider.validateToken(jwt)` → 유효성 검사
   4. 유효하면 `jwtTokenProvider.getEmailFromToken(jwt)` 호출
   5. `userDetailsService.loadUserByUsername(email)` 호출 → DB 조회
   6. `UsernamePasswordAuthenticationToken` 생성 후 `SecurityContextHolder.getContext().setAuthentication(authentication)`
   7. `filterChain.doFilter(request, response)`로 다음 필터/컨트롤러 실행
3. 조건문 분기:
   - 토큰이 없거나 비어있음: 인증 과정 건너뜀(익명 요청으로 진행)
   - 토큰 검증 실패(만료/위조 등): 인증 설정 안 됨 → 이후 요청 처리 중 보호된 엔드포인트면 401 응답
4. 최종 반환값: 필터 자체는 응답을 직접 반환하지 않음(단, 토큰이 없거나 유효하지 않으면 이후에 401/403이 발생할 수 있음). 인증이 설정되면 `SecurityContext`에 인증 정보가 저장되어 컨트롤러가 인증 사용자로 동작함.

---

8) 아이디(이메일) 찾기 및 비밀번호 재설정 (`AuthController.findEmail`, `AuthController.resetPassword`)
1. 요청 시작 지점: POST `/api/auth/find-email` 또는 `/api/auth/reset-password`
2. 호출 순서 (resetPassword 예):
   1. `AuthController.resetPassword(request)`
   2. `userService.resetPassword(email, userName, phone)`
   3. `userRepository.findByEmailAndUserNameAndPhoneAndIsDeletedFalse(...)` 조회
   4. 임시 비밀번호 생성(`UUID.randomUUID().toString().substring(0,8)`)
   5. `user.setPassword(passwordEncoder.encode(tempPassword))` 및 `userRepository.save(user)`
3. 조건문 분기:
   - 정보 불일치: `IllegalArgumentException` → 400 반환
4. 최종 반환값: 성공 시 임시 비밀번호(평문)을 포함한 `ApiResponse.success(tempPassword)` (컨트롤러가 그대로 반환)

---

9) 공지사항 흐름 (사용자 조회 및 관리자 CRUD)
1. 요청 시작 지점: 사용자 `GET /api/notice/list`, `GET /api/notice/{id}` 또는 관리자 `POST /api/admin/notice`, `PUT /api/admin/notice/{id}`, `DELETE /api/admin/notice/{id}`
2. 호출 순서(목록 조회):
   1. `NoticeController.list(keyword, pageable)`
   2. `NoticeService.getNoticeList(pageable)` 또는 `searchNotices(keyword, pageable)`
   3. `noticeRepository.findAllByOrderByIsImportantDescCreatedAtDesc(pageable)` 또는 `findByTitleContaining`
   4. `toDto()`로 변환 후 `ApiResponse.success(PageResponse.of(page))` 반환
3. 조건문 분기:
   - 검색 키워드 유무에 따라 `getNoticeList` 또는 `searchNotices` 선택
   - 관리자 권한 체크 실패 시 403(스프링 시큐리티)
4. 최종 반환값: `ApiResponse.success`로 래핑된 `PageResponse<NoticeDto>` (목록) 또는 `NoticeDto`(상세)

---

10) FAQ 흐름 (사용자 조회 및 관리자 CRUD)
1. 요청 시작 지점: `GET /api/faq/list`, `GET /api/faq/{id}`, 관리자 CRUD 엔드포인트
2. 호출 순서:
   1. `FaqController.list()` → `FaqService.getAllFaqList()` 또는 `getFaqListByCategory(category)`
   2. `faqRepository.findAll()` 또는 `findByCategory(category)` → 엔티티를 DTO로 변환
3. 조건문 분기:
   - category 파라미터 유무
   - 관리자 전용은 `@PreAuthorize("hasRole('ADMIN')")` 적용
4. 최종 반환값: `ApiResponse.success`로 래핑된 `List<FaqDto>` 또는 `PageResponse<FaqDto>`

---

11) 결제 준비/검증/생성/취소 흐름 (PortOne 연동)
1. 요청 시작 지점:
   - 결제 준비: `GET /api/payment/checkout-info` 또는 `POST /api/payment/prepare`
   - 결제 검증: `POST /api/payment/verify`
   - 로컬 결제 생성: `POST /api/payment/product` 또는 `/class`
   - 결제 취소: `POST /api/payment/{payId}/cancel`
2. 호출 순서(검증 흐름):
   1. `PaymentController.verifyPayment(verifyRequest)`
   2. 인증 검사(`userDetails` 존재 여부) → `userService.findById(userId)`
   3. `PortOneService.verifyAndSavePayment(user, paymentId, request)`
      - `getPaymentFromPortOne(paymentId)` → `WebClient` 호출 → 응답 JSON 수신
      - JSON 매핑(우선 wrapper, 다음 direct mapping)
      - 금액 검증: `paidAmount.equals(request.getTotalAmount())`
      - 상태 검증: `"PAID"` 인지 확인
      - `Payment`/`PaymentItem` 생성 후 `paymentRepository.save(payment)`
   4. `PortOneDto.PaymentResult` 반환
   5. 컨트롤러는 성공 시 `ApiResponse.success("결제가 완료되었습니다.", result)` 반환, 실패 시 `ApiResponse.error(result.getMessage())`
3. 조건문 분기:
   - 인증이 없으면 401 반환
   - 포트원 응답이 null 또는 JSON 파싱 실패 → 실패 반환
   - 금액 불일치 → 실패 반환
   - 결제 상태가 `PAID`가 아니면 실패 반환
4. 최종 반환값: 성공 시 `ApiResponse.success`와 함께 `PaymentResult`(payId, paymentId, amount 등), 실패 시 `ApiResponse.error`

---

12) 관리자용 결제 목록/취소 (`AdminPaymentController`)
1. 요청 시작 지점: `GET /api/admin/payments/list`, `POST /api/admin/payments/{payId}/cancel`
2. 호출 순서:
   1. 목록 조회: `paymentService.findAll(pageable)` → `Page<Payment>` → `PaymentDto.from` 매핑 → 반환
   2. 취소: 컨트롤러 → `paymentService.cancelPayment(payId)` → 내부에서 `portOneService.cancelPayment(payId, reason)` → `paymentRepository.findById(payId)` → 상태 변경
3. 조건문 분기: 관리자 권한 체크 실패 시 403
4. 최종 반환값: 목록은 `PageResponse<PaymentDto>`, 취소는 `ApiResponse.success` 또는 `ApiResponse.error`

---

13) 사용자 활동 이력 조회 (`UserService.getUserHistory`)
1. 요청 시작 지점: `GET /api/admin/users/{userId}/history` 또는 다른 호출 지점에서 서비스 호출
2. 호출 순서:
   1. `UserService.getUserHistory(userId)`
   2. `findById(userId)` → `cartRepository.findByUser_UserId(userId)` → 주문 조회(`orderRepository.findByCartId`)
   3. 예약 조회(`reservationRepository.findAllByUser_UserId(...)`)
   4. 결제 조회(`paymentRepository.findByUser(user)`)
   5. `UserHistoryDto.builder().orders(...).reservations(...).payments(...).build()`
3. 조건문 분기: 각 리포지토리 조회가 Optional/빈 리스트인 경우 빈 리스트 반환으로 처리
4. 최종 반환값: `UserHistoryDto` 객체(컨트롤러에서 `ApiResponse.success`로 래핑하여 반환)

---

### 문서 저장
이 문서는 `docs/common_execution_flows.md`로 저장되었습니다.

추가로 특정 엔드포인트의 request/response 예시(JSON, HTTP 헤더 포함)를 원하시면 이어서 생성해 드리겠습니다.
