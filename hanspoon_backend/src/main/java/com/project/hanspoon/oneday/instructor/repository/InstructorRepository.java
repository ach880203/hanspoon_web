package com.project.hanspoon.oneday.instructor.repository;

import com.project.hanspoon.oneday.instructor.entity.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InstructorRepository extends JpaRepository<Instructor, Long> {
    Optional<Instructor> findByUser_UserId(Long userId);
    boolean existsByUser_UserId(Long userId);
}
