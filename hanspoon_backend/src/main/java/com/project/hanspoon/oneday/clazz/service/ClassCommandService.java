package com.project.hanspoon.oneday.clazz.service;

import com.project.hanspoon.common.exception.BusinessException;
import com.project.hanspoon.oneday.clazz.dto.ClassCreateRequest;
import com.project.hanspoon.oneday.clazz.dto.ClassCreateResponse;
import com.project.hanspoon.oneday.clazz.dto.ClassDetailResponse;
import com.project.hanspoon.oneday.clazz.dto.ClassSessionCreateRequest;
import com.project.hanspoon.oneday.clazz.dto.ClassUpdateRequest;
import com.project.hanspoon.oneday.clazz.entity.ClassProduct;
import com.project.hanspoon.oneday.clazz.entity.ClassSession;
import com.project.hanspoon.oneday.clazz.repository.ClassProductRepository;
import com.project.hanspoon.oneday.clazz.repository.ClassSessionRepository;
import com.project.hanspoon.oneday.instructor.entity.Instructor;
import com.project.hanspoon.oneday.instructor.repository.InstructorRepository;
import com.project.hanspoon.oneday.reservation.repository.ClassReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClassCommandService {
    // Base64(DataURL) 기준으로 원본 이미지 약 50MB 수준을 상한으로 잡습니다.
    private static final int MAX_DETAIL_IMAGE_DATA_LENGTH = 72_000_000;
    private static final int MAX_SESSION_COUNT = 120;

    private final ClassProductRepository classProductRepository;
    private final ClassSessionRepository classSessionRepository;
    private final InstructorRepository instructorRepository;
    private final ClassReservationRepository classReservationRepository;

    public ClassCreateResponse createClass(Long actorUserId, boolean isAdmin, ClassCreateRequest req) {
        validateActor(actorUserId, isAdmin);
        validateCreateRequest(req);

        List<String> detailImages = normalizeDetailImages(req.detailImageData(), req.detailImageDataList());
        Instructor instructor = loadInstructor(req.instructorId());

        ClassProduct savedClass = classProductRepository.save(
                ClassProduct.builder()
                        .title(req.title().trim())
                        .description(trimOrEmpty(req.description()))
                        .detailDescription(trimOrEmpty(req.detailDescription()))
                        .detailImageData(detailImages.isEmpty() ? "" : detailImages.get(0))
                        .level(req.level())
                        .runType(req.runType())
                        .category(req.category())
                        .locationAddress(trimOrNull(req.locationAddress()))
                        .locationLat(req.locationLat())
                        .locationLng(req.locationLng())
                        .instructor(instructor)
                        .build()
        );

        // 상세 이미지는 별도 엔티티 컬렉션에도 반영해 상세 페이지 다중 이미지 렌더링과 동기화합니다.
        savedClass.replaceDetailImages(detailImages);

        List<Long> createdSessionIds = replaceSessions(savedClass, req.sessions());

        return new ClassCreateResponse(
                savedClass.getId(),
                savedClass.getTitle(),
                createdSessionIds.size(),
                createdSessionIds
        );
    }

    public ClassDetailResponse updateClass(Long actorUserId, boolean isAdmin, Long classId, ClassUpdateRequest req) {
        validateActor(actorUserId, isAdmin);
        validateUpdateRequest(req);

        List<String> detailImages = normalizeDetailImages(req.detailImageData(), req.detailImageDataList());

        ClassProduct target = classProductRepository.findById(classId)
                .orElseThrow(() -> new BusinessException("클래스를 찾을 수 없습니다. id=" + classId));

        // 예약 상태가 취소/만료여도 예약 레코드는 이력으로 남아 FK가 걸릴 수 있습니다.
        // 따라서 reservedCount가 아니라 "예약 이력 존재 여부" 기준으로 수정을 차단합니다.
        if (classReservationRepository.existsBySession_ClassProduct_Id(classId)) {
            throw new BusinessException("예약이 존재하는 클래스는 수정할 수 없습니다.");
        }

        Instructor instructor = loadInstructor(req.instructorId());

        target.updateInfo(
                req.title().trim(),
                trimOrEmpty(req.description()),
                trimOrEmpty(req.detailDescription()),
                detailImages.isEmpty() ? "" : detailImages.get(0),
                req.level(),
                req.runType(),
                req.category(),
                trimOrNull(req.locationAddress()),
                req.locationLat(),
                req.locationLng(),
                instructor
        );

        target.replaceDetailImages(detailImages);

        classSessionRepository.deleteByClassProductId(classId);
        replaceSessions(target, req.sessions());

        return ClassDetailResponse.from(target);
    }

    public void deleteClass(Long actorUserId, boolean isAdmin, Long classId) {
        validateActor(actorUserId, isAdmin);

        // reservedCount가 0이어도 취소/만료 예약 이력이 남아 있으면 FK 제약으로 세션 삭제가 실패합니다.
        // DB 오류(500) 대신 비즈니스 메시지(400)를 반환하도록 예약 이력 존재를 먼저 차단합니다.
        if (classReservationRepository.existsBySession_ClassProduct_Id(classId)) {
            throw new BusinessException("예약이 존재하는 클래스는 삭제할 수 없습니다.");
        }

        ClassProduct target = classProductRepository.findById(classId)
                .orElseThrow(() -> new BusinessException("클래스를 찾을 수 없습니다. id=" + classId));

        classSessionRepository.deleteByClassProductId(classId);
        classProductRepository.delete(target);
    }

    private Instructor loadInstructor(Long instructorId) {
        return instructorRepository.findById(instructorId)
                .orElseThrow(() -> new BusinessException("강사를 찾을 수 없습니다. instructorId=" + instructorId));
    }

    private List<Long> replaceSessions(ClassProduct classProduct, List<ClassSessionCreateRequest> sessionRequests) {
        List<Long> createdSessionIds = new ArrayList<>();

        for (ClassSessionCreateRequest sessionReq : sessionRequests) {
            ClassSession session = classSessionRepository.save(
                    ClassSession.builder()
                            .classProduct(classProduct)
                            .startAt(sessionReq.startAt())
                            .slot(sessionReq.slot())
                            .capacity(sessionReq.capacity())
                            .price(sessionReq.price())
                            .build()
            );
            createdSessionIds.add(session.getId());
        }

        return createdSessionIds;
    }

    private void validateActor(Long actorUserId, boolean isAdmin) {
        if (actorUserId == null || actorUserId <= 0) {
            throw new BusinessException("로그인 정보가 올바르지 않습니다.");
        }
        if (!isAdmin) {
            throw new BusinessException("관리자만 클래스를 등록/수정/삭제할 수 있습니다.");
        }
    }

    private void validateCreateRequest(ClassCreateRequest req) {
        validateCommon(
                req == null ? null : req.title(),
                req == null ? null : req.description(),
                req == null ? null : req.detailDescription(),
                req == null ? null : req.detailImageData(),
                req == null ? null : req.detailImageDataList(),
                req == null ? null : req.level(),
                req == null ? null : req.runType(),
                req == null ? null : req.category(),
                req == null ? null : req.instructorId(),
                req == null ? null : req.locationAddress(),
                req == null ? null : req.locationLat(),
                req == null ? null : req.locationLng(),
                req == null ? null : req.sessions(),
                false
        );
    }

    private void validateUpdateRequest(ClassUpdateRequest req) {
        validateCommon(
                req == null ? null : req.title(),
                req == null ? null : req.description(),
                req == null ? null : req.detailDescription(),
                req == null ? null : req.detailImageData(),
                req == null ? null : req.detailImageDataList(),
                req == null ? null : req.level(),
                req == null ? null : req.runType(),
                req == null ? null : req.category(),
                req == null ? null : req.instructorId(),
                req == null ? null : req.locationAddress(),
                req == null ? null : req.locationLat(),
                req == null ? null : req.locationLng(),
                req == null ? null : req.sessions(),
                true
        );
    }

    private void validateCommon(
            String title,
            String description,
            String detailDescription,
            String detailImageData,
            List<String> detailImageDataList,
            com.project.hanspoon.oneday.clazz.domain.Level level,
            com.project.hanspoon.oneday.clazz.domain.RunType runType,
            com.project.hanspoon.oneday.clazz.domain.RecipeCategory category,
            Long instructorId,
            String locationAddress,
            Double locationLat,
            Double locationLng,
            List<ClassSessionCreateRequest> sessions,
            boolean allowPastSessionStartAt
    ) {
        if (title == null && description == null && detailDescription == null
                && detailImageData == null && detailImageDataList == null && level == null && runType == null
                && category == null && instructorId == null
                && locationAddress == null && locationLat == null && locationLng == null
                && sessions == null) {
            throw new BusinessException("요청값이 비어 있습니다.");
        }

        if (title == null || title.isBlank()) {
            throw new BusinessException("클래스 제목은 필수입니다.");
        }
        if (title.trim().length() > 80) {
            throw new BusinessException("클래스 제목은 최대 80자입니다.");
        }

        if (description != null && description.trim().length() > 4000) {
            throw new BusinessException("요약 설명은 최대 4000자입니다.");
        }
        if (detailDescription != null && detailDescription.trim().length() > 12000) {
            throw new BusinessException("상세 설명은 최대 12000자입니다.");
        }

        List<String> normalizedDetailImages = normalizeDetailImages(detailImageData, detailImageDataList);
        if (normalizedDetailImages.size() > 10) {
            throw new BusinessException("상세 이미지는 최대 10개까지 등록할 수 있습니다.");
        }
        for (String imageData : normalizedDetailImages) {
            if (imageData.length() > MAX_DETAIL_IMAGE_DATA_LENGTH) {
                throw new BusinessException("상세 이미지 데이터가 너무 큽니다. 50MB 이하 이미지를 사용해 주세요.");
            }
        }

        if (level == null) {
            throw new BusinessException("레벨은 필수입니다.");
        }
        if (runType == null) {
            throw new BusinessException("운영 유형은 필수입니다.");
        }
        if (category == null) {
            throw new BusinessException("카테고리는 필수입니다.");
        }
        if (instructorId == null || instructorId <= 0) {
            throw new BusinessException("강사 ID는 필수입니다.");
        }
        if (locationAddress != null && locationAddress.trim().length() > 255) {
            throw new BusinessException("클래스 위치 주소는 최대 255자입니다.");
        }
        if ((locationLat == null) != (locationLng == null)) {
            throw new BusinessException("클래스 위치 좌표는 위도/경도를 함께 입력해 주세요.");
        }
        if (locationLat != null && (locationLat < -90 || locationLat > 90)) {
            throw new BusinessException("위도 값이 유효 범위를 벗어났습니다.");
        }
        if (locationLng != null && (locationLng < -180 || locationLng > 180)) {
            throw new BusinessException("경도 값이 유효 범위를 벗어났습니다.");
        }

        if (sessions == null || sessions.isEmpty()) {
            throw new BusinessException("세션은 최소 1개 이상 등록해야 합니다.");
        }
        if (sessions.size() > MAX_SESSION_COUNT) {
            throw new BusinessException("세션은 최대 120개까지 등록할 수 있습니다.");
        }

        // 세션별 오류를 빠르게 찾을 수 있도록 인덱스(prefix)를 포함해 메시지를 반환합니다.
        for (int i = 0; i < sessions.size(); i++) {
            ClassSessionCreateRequest session = sessions.get(i);
            String prefix = "sessions[" + i + "] ";

            if (session == null) {
                throw new BusinessException(prefix + "세션 값이 비어 있습니다.");
            }
            if (session.startAt() == null) {
                throw new BusinessException(prefix + "시작일시는 필수입니다.");
            }
            // 수정 화면에서는 기존 과거 회차가 함께 전달될 수 있으므로 등록일 때만 현재 이후 제약을 강제합니다.
            if (!allowPastSessionStartAt && session.startAt().isBefore(LocalDateTime.now())) {
                throw new BusinessException(prefix + "시작일시는 현재 시각 이후여야 합니다.");
            }
            if (session.slot() == null) {
                throw new BusinessException(prefix + "시간대는 필수입니다.");
            }
            if (session.capacity() == null || session.capacity() <= 0) {
                throw new BusinessException(prefix + "정원은 1 이상이어야 합니다.");
            }
            if (session.price() == null || session.price() < 0) {
                throw new BusinessException(prefix + "가격은 0 이상이어야 합니다.");
            }
        }
    }

    private String trimOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimOrNull(String value) {
        String normalized = trimOrEmpty(value);
        return normalized.isEmpty() ? null : normalized;
    }

    private List<String> normalizeDetailImages(String detailImageData, List<String> detailImageDataList) {
        List<String> result = new ArrayList<>();
        String main = trimOrEmpty(detailImageData);
        // 메인 이미지는 상세 목록이 있어도 항상 첫 번째로 유지합니다.
        if (!main.isEmpty()) {
            result.add(main);
        }

        if (detailImageDataList != null) {
            for (String imageData : detailImageDataList) {
                String normalized = trimOrEmpty(imageData);
                if (!normalized.isEmpty()) {
                    if (!result.contains(normalized)) {
                        result.add(normalized);
                    }
                }
            }
        }

        if (result.isEmpty()) {
            String single = trimOrEmpty(detailImageData);
            if (!single.isEmpty()) {
                result.add(single);
            }
        }

        return result;
    }
}
