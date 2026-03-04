# `com.project.hanspoon.common` 패키지 정리

이 문서는 `hanspoon_back_0211/src/main/java/com/project/hanspoon/common` 아래 파일들의 역할을 초심자도 이해하기 쉽게 정리한 것입니다.

---

## 1. config/DataInitializer.java
- 한 줄 요약: 애플리케이션 시작 시 기본 관리자 계정(admin@example.com)을 생성하거나 권한/상태를 보정합니다.
- 메서드별 역할:
  - `run(String... args)`: 서버 시작 후 실행되어 DB에서 관리자 계정 조회. 존재하면 권한과 상태를 업데이트, 없으면 새 계정 생성(비밀번호 암호화 후 저장).
- 요청→응답 흐름: HTTP 흐름이 아니라 애플리케이션 시작 이벤트 → DB 조회 → 저장(삽입/업데이트) → 로그
- 비유: 빌딩 문 여는 직원이 도착해 관리자 키가 있는지 확인하고 없으면 새로 등록하는 작업과 같습니다.
- 용어 정리: `CommandLineRunner`(서버 시작 시 실행되는 코드), `PasswordEncoder`(비밀번호를 안전하게 해시).

---

## 2. security/CustomOAuth2UserService.java
- 한 줄 요약: 구글/네이버/카카오 소셜 로그인을 처리하여 사용자 정보를 조회하거나 신규 가입 처리합니다.
- 메서드별 역할:
  - `loadUser(OAuth2UserRequest)`: 공급자에서 받은 사용자 정보로 `OAuth2User` 생성, 제공자별 정보 추출, 기존 사용자 조회 또는 생성 후 `CustomUserDetails` 반환.
  - `extractUserInfo(...)`: 제공자(google/naver/kakao)에 맞춰 이메일, 이름, 제공자Id 추출.
  - `findOrCreateUser(...)`: provider+providerId 또는 이메일로 기존 사용자 조회, 없으면 새 사용자 생성.
- 요청→응답 흐름: OAuth2 로그인 흐름에서 토큰으로 사용자정보 조회 → 프로바이더별 파싱 → DB 조회/저장 → 스프링 시큐리티용 사용자 객체 반환
- 비유: 손님(소셜 로그인 사용자)이 초대장을 들고 들어왔을 때, 기존 명단에 있는지 확인하고 없으면 등록해주는 리셉션 직원.
- 용어 정리: `OAuth2User`(소셜 로그인 공급자가 준 사용자 정보), `providerId`(공급자가 부여한 고유 ID).

---

## 3. security/CustomUserDetailsService.java
- 한 줄 요약: 이메일로 사용자를 찾아 스프링 시큐리티에 제공하는 서비스입니다.
- 메서드별 역할:
  - `loadUserByUsername(String email)`: 이메일로 활성 사용자 조회 후 `CustomUserDetails` 반환.
- 요청→응답 흐름: 인증 과정에서 이메일로 DB 조회 → 사용자 정보 로드 → 인증 처리에 사용
- 비유: 출입 통제 시스템에서 신분증 이메일을 검사해 사람 정보를 불러오는 관리자.
- 용어 정리: `UserDetailsService`(스프링 시큐리티에서 사용자 정보를 읽어오는 규약).

---

## 4. security/CustomUserDetails.java
- 한 줄 요약: `User` 엔티티를 스프링 시큐리티가 이해하는 형태(`UserDetails`, `OAuth2User`)로 감싼 어댑터 클래스입니다.
- 메서드별 역할:
  - `getAuthorities()`: 사용자 역할을 권한 목록으로 반환.
  - `getPassword()`, `getUsername()`: 인증에 필요한 필드 제공.
  - `isAccountNonLocked()`, `isEnabled()` 등: 계정 상태 검사 로직 제공.
  - 편의 메서드: `getUserId()`, `getUserName()`, `getEmail()` 등.
- 요청→응답 흐름: 인증/인가 과정에서 `User` 정보를 시큐리티가 사용할 수 있는 형태로 제공
- 비유: 실제 사람(사용자) 카드를 스프링 시큐리티라는 기계가 읽을 수 있게 번역해주는 통역사.
- 용어 정리: `GrantedAuthority`(사용자의 권한 항목), `OAuth2User`(소셜 로그인 사용자 정보 인터페이스).

---

## 5. security/OAuth2AuthenticationSuccessHandler.java
- 한 줄 요약: OAuth2 로그인 성공 후 JWT 토큰을 만들어 프론트엔드로 리다이렉트합니다.
- 메서드별 역할:
  - `onAuthenticationSuccess(...)`: 인증 성공 시 JWT 생성, 프론트엔드 리다이렉트 URL에 토큰을 붙여 전달.
- 요청→응답 흐름: 소셜 로그인 성공 → JWT 생성 → 프론트엔드의 `/oauth2/redirect`로 리다이렉트(토큰 포함)
- 비유: 로그인 창구에서 신분 확인 후 출입증(토큰)을 발급해 건물 입구로 보내주는 우편배달.
- 용어 정리: `Authentication`(인증 결과 객체), `redirect`(브라우저 리다이렉션).

---

## 6. user/entity/User.java
- 한 줄 요약: 사용자 정보를 DB에 매핑하는 JPA 엔티티입니다.
- 메서드별 역할:
  - `getSpoonBalance()`: spoonCount 널 안전 반환.
  - `updateProfile(...)`: 프로필 필드 업데이트.
  - `softDelete()`, `suspend()`, `activate()`: 상태 전환 편의 메서드.
  - `updateLastLogin()`: 마지막 로그인 시간 업데이트.
  - `addSpoon(int)`, `useSpoon(int)`: 포인트(스푼) 증가/사용 로직.
- 요청→응답 흐름: 서비스/컨트롤러에서 이 엔티티를 읽고 수정한 후 리포지토리로 저장하여 DB 반영
- 비유: 사용자의 개인 파일(레코드). 파일에 기록을 추가하거나 상태(활성/정지)를 표기하는 작업
- 용어 정리: `엔티티`(DB의 테이블 행을 코드로 나타낸 객체), `soft delete`(데이터를 실제 삭제하지 않고 삭제 표시만 함).

---

## 7. response/ApiResponse.java (record)
- 한 줄 요약: API 응답에 쓰이는 단순한 공용 레코드(resolve된 타입의 응답 포맷)입니다.
- 메서드별 역할:
  - `ok(T data)`: 성공 응답 생성
  - `fail(String message)`: 실패 응답 생성
- 요청→응답 흐름: 서비스 결과를 감싸 HTTP 응답으로 클라이언트에 반환
- 비유: 편지지를 만들어서 결과를 한 장으로 포장해 보내는 우표
- 용어 정리: `record`(불변 필드가 있는 간단한 데이터 컨테이너), 제네릭(T).

---

## 8. security/jwt/JwtAuthenticationFilter.java
- 한 줄 요약: 모든 요청에서 Authorization 헤더의 JWT를 검사해 인증 정보를 생성하는 필터입니다.
- 메서드별 역할:
  - `doFilterInternal(...)`: 요청의 헤더에서 토큰 추출 → 유효성 검사 → 유효하면 `SecurityContext`에 인증 정보 저장 → 다음 필터 실행.
  - `getJwtFromRequest(...)`: Authorization 헤더에서 Bearer 토큰을 추출.
- 요청→응답 흐름: 클라이언트 요청 → 필터에서 토큰 검사 → 인증 정보가 설정되면 이후 컨트롤러는 인증된 사용자로 동작
- 비유: 입구에서 신분증 토큰을 검사해 출입증을 발급해주는 보안 요원
- 용어 정리: `Filter`(요청/응답을 중간에서 가로채 처리), `SecurityContext`(현재 요청의 인증 상태 저장소).

---

## 9. security/jwt/JwtTokenProvider.java
- 한 줄 요약: JWT 생성과 검증을 담당하는 유틸리티 클래스입니다.
- 메서드별 역할:
  - `init()`: 시크릿 키 초기화
  - `createToken(Authentication)`: 이메일(subject)과 역할을 클레임으로 넣어 JWT 생성
  - `getEmailFromToken(String)`, `getRolesFromToken(String)`: 토큰에서 정보 추출
  - `validateToken(String)`: 토큰 유효성 검사(서명/만료 등)
- 요청→응답 흐름: 로그인 성공 시 토큰 생성 → 이후 요청에서 필터가 이 클래스에 검증을 요청
- 비유: 우체국에서 봉인을 찍어 보증하는 도장(토큰 서명)을 만들고 확인하는 도장감정관
- 용어 정리: `secretKey`(토큰 서명에 쓰이는 비밀), `claims`(토큰에 담긴 정보 조각).

---

## 10. user/service/UserService.java
- 한 줄 요약: 회원가입, 계정 조회, 비밀번호 초기화, 상태 변경, 사용자 활동 히스토리 등 사용자 관련 비즈니스 로직 집합입니다.
- 메서드별 역할 (주요 메서드만):
  - `register(UserRegisterDto)`: 회원가입, 이메일 중복 및 비밀번호 확인, 암호화 저장.
  - `isEmailExists(String)`, `findByEmail(String)`, `findById(Long)`: 조회 유틸리티
  - `findEmail(userName, phone)`: 이름+전화로 이메일 찾기
  - `resetPassword(email,userName,phone)`: 임시 비밀번호 생성 후 DB 저장(인코딩), 평문 반환
  - `findAll(String, Pageable)`: 사용자 목록(페이징+검색)
  - `updateLastLogin(Long)`, `updateStatus(Long, UserStatus)`: 상태/로그인 시간 갱신
  - `getUserHistory(Long)`: 주문/예약/결제 히스토리 조합 반환
- 요청→응답 흐름: 컨트롤러에서 DTO를 받아 유효성 검사/저장/조회 수행 → 리포지토리로 DB 작업 → 결과 반환
- 비유: 회원 관리팀의 업무 매뉴얼 — 가입 처리, 계정 검색, 비밀번호 재발급 등 실제 업무 흐름 담당
- 용어 정리: `Transactional`(데이터베이스 작업의 트랜잭션 경계), `DTO`(데이터 전달 객체).

---

## 11. user/repository/UserRepository.java
- 한 줄 요약: `User` 엔티티를 위한 JPA 리포지토리(데이터 접근 레이어)입니다.
- 메서드별 역할: 이메일로 조회, provider+providerId 조회, 삭제 표시된 사용자 제외 조회, 페이징 검색 등 여러 쿼리 메서드 제공.
- 요청→응답 흐름: 서비스가 리포지토리 메서드를 호출하면 JPA가 SQL을 생성해 DB에서 결과를 반환
- 비유: 도서관의 색인 카탈로그(이름·이메일로 책(사용자)을 찾는 인덱스)
- 용어 정리: `JpaRepository`(기본 CRUD 제공), 메서드 네이밍으로 쿼리를 만드는 스프링 데이터 기능.

---

## 12. exception/GlobalExceptionHandler.java
- 한 줄 요약: 컨트롤러에서 발생한 예외를 잡아 일관된 `ApiResponse` 형식으로 응답합니다.
- 메서드별 역할:
  - `handleBusiness(BusinessException)`: 비즈니스 예외 처리(400)
  - `handleValidation(MethodArgumentNotValidException)`: 입력 검증 실패 처리(400)
  - `handleEtc(Exception)`: 그 밖의 예외 처리(500)
- 요청→응답 흐름: 컨트롤러 예외 발생 → 이 클래스의 핸들러가 실행되어 클라이언트에 JSON 응답 반환
- 비유: 고객 상담 창구에서 문제가 생기면 표준화된 안내문으로 응대하는 콜센터 매뉴얼
- 용어 정리: `@RestControllerAdvice`(전역 예외 처리), `MethodArgumentNotValidException`(입력 DTO 검증 실패)

---

## 13. exception/BusinessException.java
- 한 줄 요약: 서비스 레이어에서 발생시키는 간단한 런타임 비즈니스 예외입니다.
- 메서드별 역할: 생성자에서 메시지를 받아 상위 RuntimeException에 전달
- 요청→응답 흐름: 서비스에서 예외 발생 → `GlobalExceptionHandler`에서 잡아 응답
- 비유: 업무 규칙 위반을 알리는 경고표지
- 용어 정리: `RuntimeException`(체크하지 않아도 되는 예외)

---

## 14. user/dto/UserRegisterDto.java
- 한 줄 요약: 회원가입 API 입력을 위한 DTO(필드 검증 어노테이션 포함)입니다.
- 메서드별 역할: (필드와 검증 규칙 정의) 이메일 형식 검증, 비밀번호 길이, 필수 항목 지정
- 요청→응답 흐름: 컨트롤러가 이 DTO를 입력으로 받아 검증 → 서비스로 전달
- 비유: 회원가입 신청서 양식
- 용어 정리: `@NotBlank`, `@Email`, `@Size`(입력 검증 어노테이션)

---

## 15. user/dto/UserHistoryDto.java
- 한 줄 요약: 사용자 활동(주문, 예약, 결제) 목록을 담는 DTO입니다.
- 메서드별 역할: 단순 필드 보유(엔티티 리스트)
- 요청→응답 흐름: 서비스에서 엔티티를 모아 이 DTO로 반환 → 컨트롤러 응답
- 비유: 한 사람의 활동 기록을 모아둔 요약 리포트
- 용어 정리: DTO(데이터 전달 객체)

---

## 16. user/dto/LoginResponse.java
- 한 줄 요약: 로그인 성공 시 반환하는 토큰/사용자 정보 DTO입니다.
- 메서드별 역할: 필드 보유(토큰, 유저ID, 이메일 등)
- 요청→응답 흐름: 인증 성공 후 컨트롤러가 이 DTO를 만들어 반환
- 비유: 로그인 영수증(토큰과 사용자 요약 정보)
- 용어 정리: accessToken(서버가 발급한 인증 토큰)

---

## 17. user/dto/LoginRequest.java
- 한 줄 요약: 로그인 요청(이메일, 비밀번호)을 담는 DTO입니다.
- 메서드별 역할: 필드 보유
- 요청→응답 흐름: 클라이언트가 이 DTO로 로그인 요청 → 인증 매니저가 검증
- 비유: 출입용 신분증(이메일+비밀번호)

---

## 18. user/dto/FindPasswordRequest.java
- 한 줄 요약: 비밀번호 찾기(재설정)용 요청 DTO입니다.
- 메서드별 역할: email, userName, phone 필드 보유
- 요청→응답 흐름: 컨트롤러가 서비스의 `resetPassword`를 호출
- 비유: 비밀번호 재발급 신청서

---

## 19. user/dto/FindIdRequest.java
- 한 줄 요약: 아이디(이메일) 찾기용 DTO (이름+전화)
- 메서드별 역할: userName, phone 보유
- 요청→응답 흐름: 컨트롤러가 서비스의 `findEmail` 호출
- 비유: 잃어버린 계정 찾기 양식

---

## 20. user/controller/AuthController.java
- 한 줄 요약: 로그인, 회원가입, 아이디/비밀번호 찾기, OAuth2 결과 반환 등 인증 관련 REST API 엔드포인트 모음입니다.
- 메서드별 역할(주요 엔드포인트):
  - `POST /api/auth/login`: 인증 매니저로 로그인, JWT 생성, 최근 로그인 기록 업데이트, `LoginResponse` 반환
  - `POST /api/auth/register`: 회원가입 요청 처리
  - `GET /api/auth/check-email`: 이메일 중복 확인
  - `GET /api/auth/me`: 현재 인증된 사용자 정보 반환
  - `GET /api/auth/oauth2/success`: OAuth2 성공 시 JWT 생성 및 사용자 정보 반환
  - `POST /api/auth/find-email`: 이름+전화로 이메일 반환
  - `POST /api/auth/reset-password`: 비밀번호 재설정(임시 비밀번호 반환)
- 요청→응답 흐름: 클라이언트 요청 → 입력 검증/서비스 호출 → 성공/실패에 따라 `ApiResponse`로 응답
- 비유: 로비 데스크(로그인/회원가입 창구)
- 용어 정리: `AuthenticationManager`(아이디/비밀번호 인증을 실제 수행하는 컴포넌트)

---

## 21. user/controller/AdminUserController.java
- 한 줄 요약: 관리자 전용 사용자 관리 API(목록, 상세, 상태 변경, 히스토리 조회).
- 메서드별 역할:
  - `GET /api/admin/users/list`: 사용자 목록(페이징)
  - `GET /api/admin/users/{userId}`: 상세 조회
  - `POST /api/admin/users/{userId}/status`: 상태 변경
  - `GET /api/admin/users/{userId}/history`: 활동 이력 조회
- 요청→응답 흐름: 관리자 요청 → 서비스 호출 → `ApiResponse`로 페이지/객체 반환
- 비유: 관리자용 사용자 관리 콘솔

---

## 22. notice/entity/Notice.java
- 한 줄 요약: 공지사항 정보를 담는 JPA 엔티티입니다.
- 메서드별 역할: `isNew()`는 작성된 지 24시간 이내인지 판별
- 요청→응답 흐름: 서비스가 엔티티를 읽고 DTO로 변환해 반환
- 비유: 공지 게시판의 한 게시글

---

## 23. notice/service/NoticeService.java
- 한 줄 요약: 공지사항 목록 조회, 상세, 생성, 수정, 삭제, 검색 기능을 제공하는 서비스입니다.
- 메서드별 역할(주요):
  - `getNoticeList(Pageable)`, `searchNotices(...)`: 페이징된 공지 목록 반환
  - `getNotice(Long)`: 상세 조회
  - `createNotice(NoticeDto)`, `updateNotice(...)`, `deleteNotice(...)`: CRUD
  - `toDto(Notice)`: 엔티티 → DTO 변환
- 요청→응답 흐름: 컨트롤러가 호출 → 리포지토리로 DB작업 → DTO 반환
- 비유: 회사의 공지 게시판 운영팀

---

## 24. notice/controller/AdminNoticeController.java
- 한 줄 요약: 관리자용 공지사항 관리 API(등록/수정/삭제/목록 등)
- 메서드별 역할: CRUD 엔드포인트 제공, `@PreAuthorize`로 ADMIN 권한 필요
- 요청→응답 흐름: 관리자 요청 → 서비스 호출 → 결과 반환

---

## 25. notice/controller/NoticeController.java
- 한 줄 요약: 사용자용 공지사항 조회 API(목록/상세)
- 메서드별 역할: 목록 조회(검색 가능), 상세 조회
- 요청→응답 흐름: 사용자 요청 → 서비스에서 DTO 반환 → 응답

---

## 26. notice/repository/NoticeRepository.java
- 한 줄 요약: `Notice` 엔티티를 위한 데이터 접근 레이어입니다. 제목 검색, 중요 공지 조회, 정렬된 전체 조회 메서드 제공

---

## 27. notice/dto/NoticeDto.java
- 한 줄 요약: 공지사항 API 응답/요청에 사용하는 DTO입니다. 제목/내용/중요여부/조회수/작성일 포함

---

## 28. payment/service/PortOneService.java
- 한 줄 요약: PortOne(결제사) API와 통신해 결제 정보 검증, DB 저장, 취소 등을 처리합니다.
- 메서드별 역할:
  - `verifyAndSavePayment(User, paymentId, PaymentRequest)`: 포트원에서 결제 조회 → 금액/상태 검증 → DB 저장
  - `getPaymentFromPortOne(String)`: WebClient로 PortOne API 호출 및 JSON 파싱
  - `cancelPayment(Long, String)`: 결제 취소 처리(로컬 상태 변경)
  - `generateOrderId()`: 임시 주문ID 생성
- 요청→응답 흐름: 프론트의 결제 검증 요청 → 포트원 API 호출 → 검증 후 DB 저장 → 결과 반환
- 비유: 외부 은행에 전화를 걸어 결제 내역을 확인한 뒤 회사 시스템에 기록하는 회계 담당자
- 용어 정리: `WebClient`(비동기 HTTP 클라이언트), `ObjectMapper`(JSON ↔ 객체 매핑).

---

## 29. payment/service/PaymentService.java
- 한 줄 요약: 내부 결제 생성/조회/취소 비즈니스 로직을 담당합니다.
- 메서드별 역할: 결제 생성, 상품/클래스별 결제 생성, 취소, 상세/히스토리 조회 등
- 요청→응답 흐름: 컨트롤러가 요청 → 포트원 연동 또는 로컬 DB 작업 → 결과 반환
- 비유: 결제 처리팀의 내부 결제 기록 담당자

---

## 30. user/constant/UserStatus.java
- 한 줄 요약: 사용자 상태를 나열한 Enum(정상/정지/탈퇴)

---

## 31. payment/repository/PaymentItemRepository.java
- 한 줄 요약: 결제 항목(PaymentItem) 관련 DB 접근 메서드를 제공합니다.

---

## 32. payment/repository/PaymentRepository.java
- 한 줄 요약: 결제(Payment) 엔티티의 DB 접근 레이어로 사용자별/상태별/기간별 조회 메서드를 제공합니다.

---

## 33. payment/entity/PaymentItem.java
- 한 줄 요약: 결제의 개별 항목을 나타내는 엔티티(상품/클래스 구분)
- 메서드별 역할: `createForProduct`, `createForClass` 편의 생성자

---

## 34. payment/entity/Payment.java
- 한 줄 요약: 결제 정보를 담는 엔티티(총액, 상태, 결제일, 항목 목록)
- 메서드별 역할: `addPaymentItem`(항목 추가, 연관관계 설정), `cancelPayment`(상태 변경)

---

## 35. payment/controller/PaymentController.java
- 한 줄 요약: 사용자용 결제 관련 REST API(결제 준비, 검증, 생성, 히스토리 등)
- 메서드별 역할(주요):
  - `GET /api/payment/checkout-info`: 결제 준비 정보 제공
  - `POST /api/payment/verify`: 포트원 검증 및 저장
  - `POST /api/payment/product`, `/class`: 로컬 결제 생성
  - `POST /api/payment/{payId}/cancel`: 결제 취소
  - `GET /api/payment/history`: 사용자 결제 히스토리
- 요청→응답 흐름: 클라이언트 요청 → 인증 확인 → 서비스 호출(PortOne 연동 포함) → ApiResponse 반환
- 비유: 쇼핑몰의 결제 창구(주문 생성, 결제 검증 담당)

---

## 36. payment/dto/PaymentDto.java
- 한 줄 요약: 결제 정보를 API로 전달할 때 쓰는 DTO(항목 포함)
- 메서드별 역할: `fromEntity`/`from` 변환 유틸

---

## 37. payment/controller/AdminPaymentController.java
- 한 줄 요약: 관리자용 결제 목록 조회 및 환불(취소) API
- 메서드별 역할: 목록 조회, 취소 엔드포인트

---

## 38. payment/dto/PortOneDto.java
- 한 줄 요약: PortOne API와 주고받는 여러 DTO(요청/응답/래퍼 등)를 정의한 클래스
- 주요 역할: 결제 요청/검증/준비 응답/결과 DTO 제공
- 비유: 은행 송금에 쓰는 표준 양식들을 모아둔 파일

---

## 39. entity/BaseTimeEntity.java
- 한 줄 요약: 생성/수정 시각 자동 기록을 위한 상속용 MappedSuperclass
- 메서드별 역할: `createdAt`, `updatedAt` 필드 제공(자동 채움)
- 비유: 모든 문서에 자동으로 찍히는 생성/수정 도장

---

## 40. dto/PageResponse.java
- 한 줄 요약: 페이징 결과를 클라이언트에 전달할 때 사용하는 공통 DTO
- 메서드별 역할: `of(Page<T>)`로 스프링 `Page`를 변환

---

## 41. controller/HomeController.java
- 한 줄 요약: 서버 상태/정보를 확인하는 간단한 공통 API 제공
- 메서드별 역할: `GET /api/health`, `GET /api/info`
- 요청→응답 흐름: 호출 시 즉시 상태/정보 반환
- 비유: 가게 앞의 영업 상태 안내판

---

## 42. dto/ApiResponse.java
- 한 줄 요약: 모든 컨트롤러에서 사용하는 공통 응답 포맷(성공/실패 메시지 포함)
- 메서드별 역할: `success`/`error` 생성 팩토리 메서드 제공
- 비유: 표준화된 응답 봉투

---

## 43. faq/service/FaqService.java
- 한 줄 요약: FAQ의 목록/상세/생성/수정/삭제 비즈니스 로직 제공

---

## 44. payment/constant/PaymentStatus.java
- 한 줄 요약: 결제 상태를 정의한 Enum

---

## 45. faq/repository/FaqRepository.java
- 한 줄 요약: FAQ 엔티티의 DB 조회 메서드(카테고리/질문 검색 등)

---

## 46. faq/dto/FaqDto.java
- 한 줄 요약: FAQ 전송용 DTO(카테고리/질문/답변)

---

## 47. faq/entity/Faq.java
- 한 줄 요약: FAQ 테이블 매핑 엔티티

---

## 48. faq/controller/FaqController.java
- 한 줄 요약: 사용자용 FAQ 조회 API(목록/상세)

---

## 49. faq/controller/AdminFaqController.java
- 한 줄 요약: 관리자용 FAQ 관리 API(목록/상세/등록/수정/삭제)

---

## 50. config/MethodSecurityConfig.java
- 한 줄 요약: `@PreAuthorize` 기반 메서드 보안을 활성화하는 설정(개발 프로필에서는 비활성화)
- 비유: 관리자 전용 구역에만 출입 통제기를 켜는 설정

---

## 51. config/SecurityConfig.java
- 한 줄 요약: 애플리케이션의 Spring Security 전반 설정(JWT 필터, OAuth2, CORS 등)
- 주요 설정 요약:
  - CSRF 비활성화, 세션 스테이트리스, JWT 필터 추가
  - `/api/auth/**` 공개, `/api/admin/**`는 ADMIN 권한 필요
  - OAuth2 로그인 구성(사용자 서비스, 성공 핸들러)
  - CORS 허용 설정
- 비유: 건물의 출입 통제 규칙(어떤 문은 누구나, 어떤 문은 관리자만 통과)

---

## 52. config/PortOneConfig.java
- 한 줄 요약: PortOne 결제 설정(프로퍼티 바인딩)과 `WebClient` 빈을 제공합니다.
- 메서드별 역할: `portOneWebClient()` 빈 생성
- 비유: 결제사와 통신하는 전용 전화선 설치 설정

---

## 53. config/PasswordEncoderConfig.java
- 한 줄 요약: `PasswordEncoder`(BCrypt) 빈을 등록하는 설정
- 비유: 비밀번호를 안전하게 봉인하는 도구 제공

---

### 마무리
- 모든 파일의 간단한 정리를 `docs/common_package_summary.md`에 저장했습니다.
- 원하시면 각 파일별 설명을 더 상세하게(예: 메서드 내부 로직 단계별 분해) 확장하거나, 특정 파일에 대해 예제 요청/테스트 케이스를 추가해 드리겠습니다.
