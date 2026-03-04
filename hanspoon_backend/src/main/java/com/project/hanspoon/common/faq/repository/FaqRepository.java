package com.project.hanspoon.common.faq.repository;

import com.project.hanspoon.common.faq.entity.Faq;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaqRepository extends JpaRepository<Faq, Long> {
    List<Faq> findByCategory(String category);
    Page<Faq> findByQuestionContaining(String question, Pageable pageable);
}
