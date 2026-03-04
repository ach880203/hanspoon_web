package com.project.hanspoon.oneday.instructor.dto;

import com.project.hanspoon.common.user.entity.User;

public record InstructorCandidateUserResponse(
        Long userId,
        String userName,
        String email,
        String role
) {
    public static InstructorCandidateUserResponse from(User user) {
        if (user == null) return null;
        return new InstructorCandidateUserResponse(
                user.getUserId(),
                user.getUserName(),
                user.getEmail(),
                user.getRole()
        );
    }
}

