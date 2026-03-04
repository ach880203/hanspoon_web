package com.project.hanspoon.oneday.review.repository;

import com.project.hanspoon.oneday.review.entity.ClassReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClassReviewRepository extends JpaRepository<ClassReview, Long> {
    boolean existsByReservationIdAndDelFlagFalse(Long reservationId);
    Optional<ClassReview> findByReservationId(Long reservationId);

    List<ClassReview> findAllByClassProduct_IdAndDelFlagFalseOrderByCreatedAtDesc(Long classId);
    List<ClassReview> findAllByUserIdAndDelFlagFalseOrderByCreatedAtDesc(Long userId);

    Optional<ClassReview> findByIdAndDelFlagFalse(Long id);
}
