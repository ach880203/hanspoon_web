package com.project.hanspoon.common.notice.repository;

import com.project.hanspoon.common.notice.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    Page<Notice> findByTitleContaining(String title, Pageable pageable);
    List<Notice> findByIsImportantTrueOrderByCreatedAtDesc();
    Page<Notice> findAllByOrderByIsImportantDescCreatedAtDesc(Pageable pageable);
}
