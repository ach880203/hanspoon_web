package com.project.hanspoon.oneday.inquiry.repository;

import com.project.hanspoon.oneday.inquiry.entity.ClassInquiry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassInquiryRepository extends JpaRepository<ClassInquiry, Long> {
    List<ClassInquiry> findAllByOrderByCreatedAtDesc();

    List<ClassInquiry> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    long countByAnsweredFalse();
}
