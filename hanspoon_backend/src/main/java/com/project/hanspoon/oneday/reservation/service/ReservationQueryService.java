package com.project.hanspoon.oneday.reservation.service;

import com.project.hanspoon.common.exception.BusinessException;
import com.project.hanspoon.oneday.reservation.domain.ReservationStatus;
import com.project.hanspoon.oneday.reservation.dto.ReservationDetailResponse;
import com.project.hanspoon.oneday.reservation.dto.ReservationListItemResponse;
import com.project.hanspoon.oneday.reservation.entity.ClassReservation;
import com.project.hanspoon.oneday.reservation.repository.ClassReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationQueryService {

    private final ClassReservationRepository reservationRepository;

    public Page<ReservationListItemResponse> myReservationsWithFilters(
            Long userId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            ReservationStatus status,
            Pageable pageable
    ) {
        validateUserId(userId);

        Sort sortByLatest = Sort.by(Sort.Direction.DESC, "createdAt");
        List<ClassReservation> all = (status == null)
                ? reservationRepository.findAllByUser_UserId(userId, sortByLatest)
                : reservationRepository.findAllByUser_UserIdAndStatus(userId, status, sortByLatest);

        // 기존 DB 쿼리를 크게 바꾸지 않기 위해 날짜 조건은 원데이 범위에서 메모리 필터로 적용합니다.
        List<ClassReservation> filtered = all.stream()
                .filter(r -> isInRange(r.getSession().getStartAt(), startDate, endDate))
                .toList();

        int total = filtered.size();
        int fromIndex = Math.toIntExact(pageable.getOffset());
        if (fromIndex >= total) {
            return new PageImpl<>(List.of(), pageable, total);
        }
        int toIndex = Math.min(fromIndex + pageable.getPageSize(), total);

        List<ReservationListItemResponse> content = filtered.subList(fromIndex, toIndex)
                .stream()
                .map(this::toListItemResponse)
                .toList();

        return new PageImpl<>(content, pageable, total);
    }

    public ReservationDetailResponse myReservationDetail(Long userId, Long reservationId) {
        validateUserId(userId);

        var reservation = reservationRepository.findByIdAndUser_UserId(reservationId, userId)
                .orElseThrow(() -> new BusinessException("예약을 찾을 수 없습니다."));

        var session = reservation.getSession();
        var classProduct = session.getClassProduct();

        return new ReservationDetailResponse(
                reservation.getId(),
                reservation.getStatus().getDescription(),
                reservation.getHoldExpiredAt(),
                reservation.getPaidAt(),
                reservation.getCanceledAt(),
                session.getId(),
                session.getStartAt(),
                session.getSlot().name(),
                session.getCapacity(),
                session.getReservedCount(),
                session.getPrice(),
                classProduct.getId(),
                classProduct.getTitle()
        );
    }

    private ReservationListItemResponse toListItemResponse(ClassReservation reservation) {
        var session = reservation.getSession();
        var classProduct = session.getClassProduct();

        return new ReservationListItemResponse(
                reservation.getId(),
                reservation.getStatus().getDescription(),
                reservation.getHoldExpiredAt(),
                session.getId(),
                session.getStartAt(),
                session.getSlot().name(),
                session.getPrice(),
                classProduct.getId(),
                classProduct.getTitle()
        );
    }

    private boolean isInRange(LocalDateTime target, LocalDateTime startDate, LocalDateTime endDate) {
        if (target == null) {
            return false;
        }
        if (startDate != null && target.isBefore(startDate)) {
            return false;
        }
        if (endDate != null && target.isAfter(endDate)) {
            return false;
        }
        return true;
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException("로그인 정보가 올바르지 않습니다.");
        }
    }
}
