package com.project.hanspoon.common.user.service;

import com.project.hanspoon.common.user.entity.PointHistory;
import com.project.hanspoon.common.user.repository.PointHistoryRepository;
import com.project.hanspoon.common.user.repository.UserRepository;
import com.project.hanspoon.common.user.entity.User;
import com.project.hanspoon.common.user.dto.UserRegisterDto;
import com.project.hanspoon.common.user.dto.UserUpdateDto;
import com.project.hanspoon.oneday.reservation.repository.ClassReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.project.hanspoon.common.user.dto.UserHistoryDto;
import com.project.hanspoon.shop.order.repository.OrderRepository;
import com.project.hanspoon.common.payment.repository.PaymentRepository;
import com.project.hanspoon.shop.order.dto.OrderResponseDto;
import com.project.hanspoon.oneday.reservation.dto.ClassReservationResponseDto;
import com.project.hanspoon.common.payment.dto.PaymentDto;
import com.project.hanspoon.common.user.dto.AdminUserDetailResponse;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User 관련 비즈니스 로직을 담당하는 서비스입니다.
 *
 * 주 역할:
 * - 회원가입 및 유효성 검사
 * - 사용자 조회(이메일/ID 등)
 * - 비밀번호 재설정(임시 비밀번호 발급) — 임시 비밀번호는 인코딩되어 DB에 저장되지만, 호출자에게는 평문으로 반환됩니다.
 * - 사용자 상태 변경(활성/정지/삭제)
 * - 사용자 관련 히스토리(주문, 예약, 결제) 조회
 *
 * 트랜잭션 전략:
 * - 클래스 레벨에 @Transactional이 적용되어 있어 기본적으로 쓰기 가능 트랜잭션입니다.
 * - 읽기 전용 조회 메서드에는 @Transactional(readOnly = true)를 명시하여 성능을 일부 최적화합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
@lombok.extern.slf4j.Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrderRepository orderRepository;
    private final ClassReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final PointHistoryRepository pointHistoryRepository;

    @Transactional(readOnly = true)
    public AdminUserDetailResponse getAdminUserDetail(Long userId) {
        log.info("[Admin] Getting detail and history for user: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 상세 정보와 활동 이력을 한 번에 결합하여 반환
        UserHistoryDto history = getUserHistory(userId);

        return AdminUserDetailResponse.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .history(history)
                .build();
    }

    /**
     * 회원가입 처리
     *
     * 입력: UserRegisterDto (email, password, passwordConfirm, userName, phone,
     * address)
     * 반환: 저장된 User 엔티티
     * 예외: 이미 사용중인 이메일 -> IllegalArgumentException
     * 비밀번호 불일치 -> IllegalArgumentException
     * 주의: 비밀번호는 PasswordEncoder로 인코딩하여 저장합니다.
     */
    public User register(UserRegisterDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }
        if (!dto.getPassword().equals(dto.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        User user = User.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .userName(dto.getUserName())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .status(com.project.hanspoon.common.user.constant.UserStatus.ACTIVE)
                .role("ROLE_USER")
                .spoonCount(3000) // 신규 가입 3,000 스푼 증정
                .isDeleted(false)
                .build();

        User savedUser = userRepository.save(user);

        // 포인트 이력 추가
        PointHistory history = PointHistory.builder()
                .user(savedUser)
                .amount(3000)
                .type(com.project.hanspoon.common.user.constant.PointType.EVENT)
                .description("신규 가입 축하 3,000 스푼")
                .build();
        pointHistoryRepository.save(history);

        return savedUser;
    }

    /**
     * 이메일 중복 여부 조회
     *
     * 입력: email
     * 반환: true(존재), false(미존재)
     * 트랜잭션: 읽기 전용
     */
    @Transactional(readOnly = true)
    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 이메일로 사용자 조회
     *
     * 입력: email
     * 반환: User (존재하지 않으면 IllegalArgumentException 발생)
     * 트랜잭션: 읽기 전용
     */
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    /**
     * ID로 사용자 조회
     *
     * 입력: userId
     * 반환: User (존재하지 않으면 IllegalArgumentException 발생)
     * 트랜잭션: 읽기 전용
     */
    @Transactional(readOnly = true)
    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    /**
     * 이름과 전화번호로 이메일 찾기 (계정 찾기 기능)
     *
     * 입력: userName, phone
     * 반환: 이메일 문자열
     * 예외: 일치하는 사용자 정보가 없으면 IllegalArgumentException 발생
     * 트랜잭션: 읽기 전용
     */
    @Transactional(readOnly = true)
    public String findEmail(String userName, String phone) {
        User user = userRepository.findByUserNameAndPhoneAndIsDeletedFalse(userName, phone)
                .orElseThrow(() -> new IllegalArgumentException("일치하는 사용자 정보를 찾을 수 없습니다."));
        return user.getEmail();
    }

    /**
     * 비밀번호 초기화 및 임시 비밀번호 발급
     *
     * 입력: email, userName, phone (안전하게 사용자 식별을 위해 복수 조건 사용)
     * 반환: 임시 비밀번호(평문)
     * 부작용: DB에 인코딩된 임시 비밀번호가 저장됨
     * 예외: 정보 불일치 시 IllegalArgumentException 발생
     * 보안 주의: 이 메서드는 호출자에게 임시 비밀번호(평문)를 반환하므로 호출 흐름에서 안전하게 전달(예: 이메일 전송)해야 합니다.
     */
    public String resetPassword(String email, String userName, String phone) {
        User user = userRepository.findByEmailAndUserNameAndPhoneAndIsDeletedFalse(email, userName, phone)
                .orElseThrow(() -> new IllegalArgumentException("일치하는 사용자 정보를 찾을 수 없습니다."));

        // 임시 비밀번호 생성 (8자리 랜덤)
        String tempPassword = java.util.UUID.randomUUID().toString().substring(0, 8);
        user.setPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);
        return tempPassword;
    }

    /**
     * 사용자 목록 조회 (페이징 + 키워드 검색)
     *
     * 입력: 검색 키워드(이메일 또는 사용자명), Pageable
     * 반환: Page<User>
     * 트랜잭션: 읽기 전용
     * 엣지 케이스: 키워드가 null/빈값이면 전체 조회
     */
    @Transactional(readOnly = true)
    public Page<User> findAll(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isBlank()) {
            return userRepository.findByEmailContainingIgnoreCaseOrUserNameContainingIgnoreCase(keyword, keyword,
                    pageable);
        }
        return userRepository.findAll(pageable);
    }

    /**
     * 마지막 로그인 시간 갱신
     *
     * 입력: userId
     * 부작용: User 엔티티의 lastLogin 필드 업데이트 및 저장
     */
    public void updateLastLogin(Long userId) {
        User user = findById(userId);
        user.updateLastLogin();
        userRepository.save(user);
    }

    /**
     * 사용자 상태 변경
     *
     * 입력: userId, 상태(enum)
     * 동작: 상태가 DELETED이면 soft delete, SUSPENDED이면 suspend, 그 외에는 activate
     */
    public void updateStatus(Long userId, com.project.hanspoon.common.user.constant.UserStatus status) {
        User user = findById(userId);
        if (status == com.project.hanspoon.common.user.constant.UserStatus.DELETED) {
            user.softDelete();
        } else if (status == com.project.hanspoon.common.user.constant.UserStatus.SUSPENDED) {
            user.suspend();
        } else {
            user.activate();
        }
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserHistoryDto getUserHistory(Long userId) {
        log.info("[Admin] Getting history for user: {}", userId);

        // 1. 주문 내역
        List<OrderResponseDto> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(OrderResponseDto::fromEntity)
                .collect(Collectors.toList());
        log.info("[Admin] Found {} orders for user {}", orders.size(), userId);

        // 2. 예약 내역
        List<ClassReservationResponseDto> reservations = reservationRepository
                .findByUserId(userId).stream()
                .map(res -> ClassReservationResponseDto.builder()
                        .id(res.getId())
                        .sessionId(res.getSession() != null ? res.getSession().getId() : null)
                        .status(res.getStatus().getDescription())
                        .holdExpiredAt(res.getHoldExpiredAt())
                        .paidAt(res.getPaidAt())
                        .canceledAt(res.getCanceledAt())
                        .completedAt(res.getCompletedAt())
                        .createdAt(res.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        log.info("[Admin] Found {} reservations for user {}", reservations.size(), userId);

        // 3. 결제 내역
        List<PaymentDto> payments = paymentRepository
                .findByUserId(userId).stream()
                .map(PaymentDto::fromEntity)
                .collect(Collectors.toList());
        log.info("[Admin] Found {} payments for user {}", payments.size(), userId);

        return UserHistoryDto.builder()
                .orders(orders)
                .reservations(reservations)
                .payments(payments)
                .build();
    }

    /**
     * 사용자 정보 수정
     *
     * 입력: userId, UserUpdateDto
     * 반환: 수정된 User 엔티티
     * 예외: 비밀번호 변경 시 현재 비밀번호 불일치 -> IllegalArgumentException
     */
    public User updateUser(Long userId, UserUpdateDto dto) {
        User user = findById(userId);

        // 기본 정보 수정
        if (dto.getUserName() != null && !dto.getUserName().isBlank()) {
            user.setUserName(dto.getUserName());
        }
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getAddress() != null && !dto.getAddress().isBlank()) {
            user.setAddress(dto.getAddress());
        }

        // 비밀번호 변경 로직
        if (dto.getNewPassword() != null && !dto.getNewPassword().isBlank()) {
            if (dto.getCurrentPassword() == null || dto.getCurrentPassword().isBlank()) {
                throw new IllegalArgumentException("현재 비밀번호를 입력해주세요.");
            }
            if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
                throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
            }
            if (!dto.getNewPassword().equals(dto.getNewPasswordConfirm())) {
                throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다.");
            }
            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        }

        return userRepository.save(user);
    }
}
