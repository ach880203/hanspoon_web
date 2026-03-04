package com.project.hanspoon.oneday.home.service;

import com.project.hanspoon.oneday.clazz.domain.RunType;
import com.project.hanspoon.oneday.clazz.dto.ClassListItemResponse;
import com.project.hanspoon.oneday.clazz.repository.ClassProductRepository;
import com.project.hanspoon.oneday.home.dto.ClassHomeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClassHomeService {

    private final ClassProductRepository classProductRepository;

    public ClassHomeResponse home() {
        // 홈 미리보기 정책: 이벤트/상시 각각 최신 4개만 노출합니다.
        // 최신 기준은 BaseTimeEntity.createdAt 내림차순입니다.
        var page = PageRequest.of(0, 4);

        var events = classProductRepository.findAllByRunTypeOrderByCreatedAtDesc(RunType.EVENT, page)
                .stream().map(ClassListItemResponse::from).toList();

        var always = classProductRepository.findAllByRunTypeOrderByCreatedAtDesc(RunType.ALWAYS, page)
                .stream().map(ClassListItemResponse::from).toList();

        return new ClassHomeResponse(events, always);
    }
}
