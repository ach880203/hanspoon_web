package com.project.hanspoon.oneday.api.service;

import com.project.hanspoon.oneday.api.dto.SessionSearchResponse;
import com.project.hanspoon.oneday.api.spec.ClassSessionSpecs;
import com.project.hanspoon.oneday.clazz.entity.ClassSession;
import com.project.hanspoon.oneday.clazz.repository.ClassSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OneDaySessionSearchService {

    private final ClassSessionRepository classSessionRepository;
    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");

    public List<SessionSearchResponse> search(
            com.project.hanspoon.oneday.clazz.domain.Level level,
            com.project.hanspoon.oneday.clazz.domain.RecipeCategory category,
            com.project.hanspoon.oneday.clazz.domain.RunType runType,
            com.project.hanspoon.oneday.clazz.domain.SessionSlot slot,
            Long instructorId,
            String instructorName,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            Boolean onlyAvailable,
            String keyword,
            String sort) {
        // Specification.where(...)는 최신 Spring Data JPA에서 제거 예정이라
        // 기본 스펙(fetchAll)에서 시작해 and(...)로 조건을 누적한다.
        Specification<ClassSession> spec = ClassSessionSpecs.fetchAll()
                .and(ClassSessionSpecs.level(level))
                .and(ClassSessionSpecs.category(category))
                .and(ClassSessionSpecs.runType(runType))
                .and(ClassSessionSpecs.slot(slot))
                .and(ClassSessionSpecs.instructorId(instructorId))
                .and(ClassSessionSpecs.instructorNameContains(instructorName))
                .and(ClassSessionSpecs.startAtFrom(dateFrom))
                .and(ClassSessionSpecs.startAtTo(dateTo))
                .and(ClassSessionSpecs.titleContains(keyword))
                .and(ClassSessionSpecs.onlyAvailable(onlyAvailable));

        Sort s = toSort(sort);

        return classSessionRepository.findAll(spec, s).stream()
                .map(this::toResponse)
                .toList();
    }

    private Sort toSort(String sort) {
        if (sort == null || sort.isBlank())
            return Sort.by(Sort.Direction.ASC, "startAt");
        return switch (sort) {
            case "startAtAsc" -> Sort.by(Sort.Direction.ASC, "startAt");
            case "priceAsc" -> Sort.by(Sort.Direction.ASC, "price").and(Sort.by("startAt"));
            case "priceDesc" -> Sort.by(Sort.Direction.DESC, "price").and(Sort.by("startAt"));
            default -> Sort.by(Sort.Direction.ASC, "startAt");
        };
    }

    private SessionSearchResponse toResponse(ClassSession cs) {
        var cp = cs.getClassProduct();
        var inst = cp.getInstructor();

        LocalDateTime now = LocalDateTime.now(KST_ZONE);
        boolean full = cs.getCapacity() <= cs.getReservedCount();
        boolean completed = !cs.getStartAt().isAfter(now);
        boolean available = !full && !completed;

        return SessionSearchResponse.builder()
                .sessionId(cs.getId())
                .startAt(cs.getStartAt())
                .slot(cs.getSlot())
                .price(cs.getPrice())
                .capacity(cs.getCapacity())
                .reservedCount(cs.getReservedCount())
                .full(full)
                .completed(completed)
                .available(available)

                .classId(cp.getId())
                .classTitle(cp.getTitle())
                .level(cp.getLevel())
                .runType(cp.getRunType())
                .category(cp.getCategory())

                .instructorId(inst != null ? inst.getId() : null)
                .instructorName(inst != null && inst.getUser() != null ? inst.getUser().getUserName() : null)
                .build();
    }
}
