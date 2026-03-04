package com.project.hanspoon.oneday.instructor.dto;

public record InstructorAdminRequest(
        Long userId,
        String bio,
        String specialty,
        String career,
        String profileImageData
) {
}

