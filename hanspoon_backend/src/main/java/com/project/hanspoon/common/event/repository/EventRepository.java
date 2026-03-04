package com.project.hanspoon.common.event.repository;

import com.project.hanspoon.common.event.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // 검색용
    Page<Event> findByTitleContainingOrContentContaining(String titleKeyword, String contentKeyword, Pageable pageable);

    // 진행중인 이벤트만 조회 (프론트엔드용)
    Page<Event> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDateTime now1, LocalDateTime now2,
            Pageable pageable);
}
