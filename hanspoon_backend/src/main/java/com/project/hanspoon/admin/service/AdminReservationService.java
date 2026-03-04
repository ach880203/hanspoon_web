package com.project.hanspoon.admin.service;

import com.project.hanspoon.admin.dto.AdminReservationItemDto;
import com.project.hanspoon.common.exception.BusinessException;
import com.project.hanspoon.common.payment.constant.PaymentStatus;
import com.project.hanspoon.common.payment.repository.PaymentRepository;
import com.project.hanspoon.common.payment.service.PortOneService;
import com.project.hanspoon.oneday.clazz.repository.ClassSessionRepository;
import com.project.hanspoon.oneday.coupon.repository.ClassUserCouponRepository;
import com.project.hanspoon.oneday.reservation.domain.ReservationStatus;
import com.project.hanspoon.oneday.reservation.entity.ClassReservation;
import com.project.hanspoon.oneday.reservation.repository.ClassReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReservationService {
    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");

    private final ClassReservationRepository reservationRepository;
    private final ClassUserCouponRepository classUserCouponRepository;
    private final ClassSessionRepository classSessionRepository;
    private final PortOneService portOneService;
    private final PaymentRepository paymentRepository;

    /**
     * 관리자 예약 목록 조회
     *
     * status 파라미터 규칙:
     * - null/blank/ALL: 전체
     * - HOLD/PAID/CANCEL_REQUESTED/CANCELED/EXPIRED/COMPLETED: 해당 상태만
     */
    public List<AdminReservationItemDto> getReservations(String status) {
        List<ClassReservation> rows;
        ReservationStatus parsed = parseStatus(status);
        if (parsed == null) {
            rows = reservationRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        } else {
            rows = reservationRepository.findByStatus(parsed).stream()
                    .sorted(Comparator.comparing(ClassReservation::getCreatedAt,
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .toList();
        }

        return rows.stream().map(this::toDto).toList();
    }

    /**
     * 취소 요청 목록 조회
     */
    public List<AdminReservationItemDto> getCancelRequests() {
        return reservationRepository.findByStatus(ReservationStatus.CANCEL_REQUESTED).stream()
                .sorted(Comparator.comparing(ClassReservation::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toDto)
                .toList();
    }

    /**
     * 취소 요청 승인 시점에만 실제 취소/환불을 수행합니다.
     */
    @Transactional
    public void approveCancel(Long reservationId) {
        ClassReservation reservation = reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new BusinessException("예약을 찾을 수 없습니다. id=" + reservationId));

        if (reservation.getStatus() != ReservationStatus.CANCEL_REQUESTED) {
            throw new BusinessException("취소 요청 상태의 예약만 승인할 수 있습니다.");
        }

        var session = classSessionRepository.findByIdForUpdate(reservation.getSession().getId())
                .orElseThrow(() -> new BusinessException("세션을 찾을 수 없습니다."));

        LocalDateTime now = LocalDateTime.now(KST_ZONE);
        if (!session.getStartAt().isAfter(now)) {
            throw new BusinessException("클래스 시작 이후에는 취소 승인 및 환불이 불가합니다.");
        }

        Long payId = resolvePayId(reservation);
        if (payId == null) {
            throw new BusinessException("결제 정보를 찾지 못해 자동 환불을 진행할 수 없습니다.");
        }

        portOneService.cancelPayment(payId, buildRefundReason(reservation));
        session.decreaseReserved();
        reservation.markCanceled(now);
    }

    @Transactional
    public void rejectCancel(Long reservationId) {
        ClassReservation reservation = reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new BusinessException("예약을 찾을 수 없습니다. id=" + reservationId));

        if (reservation.getStatus() != ReservationStatus.CANCEL_REQUESTED) {
            throw new BusinessException("취소 요청 상태의 예약만 거절할 수 있습니다.");
        }

        reservation.rejectCancelRequest();
    }

    private AdminReservationItemDto toDto(ClassReservation r) {
        var session = r.getSession();
        var clazz = (session != null ? session.getClassProduct() : null);
        var user = r.getUser();

        boolean paymentCompleted = r.getPaidAt() != null;
        boolean couponIssued = classUserCouponRepository.existsByReservationId(r.getId());

        return new AdminReservationItemDto(
                r.getId(),
                r.getStatus() != null ? r.getStatus().getDescription() : "-",
                r.getStatus() != null ? r.getStatus().name() : "-",
                clazz != null ? clazz.getId() : null,
                session != null ? session.getId() : null,
                clazz != null ? clazz.getTitle() : null,
                session != null ? session.getPrice() : null,
                r.getCreatedAt(),
                r.getUpdatedAt(),
                r.getCanceledAt(),
                user != null ? user.getUserId() : null,
                user != null ? user.getUserName() : null,
                user != null ? user.getEmail() : null,
                user != null ? user.getPhone() : null,
                session != null ? session.getStartAt() : null,
                r.getHoldExpiredAt(),
                r.getPaidAt(),
                paymentCompleted,
                couponIssued,
                r.getCancelRequestedAt(),
                r.getCancelReason()
        );
    }

    /**
     * 기존 데이터(연결 payId 미보유)를 위해 결제 테이블에서 보정 조회를 제공합니다.
     * PaymentItem.classId 컬럼은 "클래스 ID"라는 이름이지만 실제로는 세션 ID를 저장하고 있습니다.
     */
    private Long resolvePayId(ClassReservation reservation) {
        if (reservation.getLinkedPayId() != null) {
            return reservation.getLinkedPayId();
        }

        var user = reservation.getUser();
        var session = reservation.getSession();
        if (user == null || session == null) {
            return null;
        }

        return paymentRepository.findClassPaymentsByUserAndSessionAndStatus(
                user.getUserId(),
                session.getId(),
                PaymentStatus.PAID
        ).stream().findFirst().map(p -> p.getPayId()).orElse(null);
    }

    private String buildRefundReason(ClassReservation reservation) {
        String base = "관리자 승인 취소(예약ID:" + reservation.getId() + ")";
        String reason = reservation.getCancelReason();
        if (reason == null || reason.isBlank()) {
            return base;
        }
        return base + " - " + reason.trim();
    }

    private ReservationStatus parseStatus(String status) {
        String normalized = trim(status);
        if (normalized == null || normalized.isBlank() || "ALL".equalsIgnoreCase(normalized)) {
            return null;
        }
        try {
            return ReservationStatus.valueOf(normalized.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("지원하지 않는 예약 상태입니다: " + status);
        }
    }

    private String trim(String v) {
        return v == null ? null : v.trim();
    }
}
