package com.project.hanspoon.common.user.repository;

import com.project.hanspoon.common.user.entity.PointHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    Page<PointHistory> findByUserUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
