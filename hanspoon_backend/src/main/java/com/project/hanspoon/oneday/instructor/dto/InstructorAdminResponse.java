package com.project.hanspoon.oneday.instructor.dto;

import com.project.hanspoon.oneday.instructor.entity.Instructor;

public record InstructorAdminResponse(
        Long id,
        Long userId,
        String userName,
        String email,
        String bio,
        String specialty,
        String career,
        String profileImageData
) {
    public static InstructorAdminResponse from(Instructor instructor) {
        if (instructor == null) return null;
        var user = instructor.getUser();
        return new InstructorAdminResponse(
                instructor.getId(),
                user != null ? user.getUserId() : null,
                user != null ? user.getUserName() : null,
                user != null ? user.getEmail() : null,
                instructor.getBio(),
                instructor.getSpecialty(),
                instructor.getCareer(),
                instructor.getProfileImageData()
        );
    }
}

