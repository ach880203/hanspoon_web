package com.project.hanspoon.oneday.clazz.service;

import com.project.hanspoon.common.exception.BusinessException;
import com.project.hanspoon.oneday.clazz.domain.*;
import com.project.hanspoon.oneday.clazz.dto.*;
import com.project.hanspoon.oneday.clazz.entity.ClassProduct;
import com.project.hanspoon.oneday.clazz.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClassQueryService {

    private final ClassProductRepository classProductRepository;
    private final ClassSessionRepository classSessionRepository;

    public Page<ClassListItemResponse> searchClasses(
            Level level,
            RunType runType,
            RecipeCategory category,
            Long instructorId,
            String instructorName,
            String keyword,
            Pageable pageable) {
        var spec = ClassProductSpec.search(level, runType, category, instructorId, instructorName, keyword);

        return classProductRepository.findAll(spec, pageable)
                .map(ClassListItemResponse::from);
    }

    public ClassDetailResponse getClassDetail(Long classId) {
        ClassProduct product = classProductRepository.findById(classId)
                .orElseThrow(() -> new BusinessException("클래스를 찾을 수 없습니다. id=" + classId));

        // instructor는 fetch join을 list에서 했고, detail은 트랜잭션 안이라 lazy 접근 가능
        return ClassDetailResponse.from(product);
    }

    public List<SessionResponse> getSessions(Long classId, LocalDate date, SessionSlot slot) {
        // date 필터: 그 날짜의 00:00 ~ 다음날 00:00
        LocalDateTime from = null;
        LocalDateTime to = null;

        if (date != null) {
            from = date.atStartOfDay();
            to = date.plusDays(1).atStartOfDay();
        }

        return classSessionRepository.findSessions(classId, from, to, slot)
                .stream()
                .map(SessionResponse::from)
                .toList();
    }
}
