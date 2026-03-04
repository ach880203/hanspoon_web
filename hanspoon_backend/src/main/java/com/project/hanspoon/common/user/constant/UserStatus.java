package com.project.hanspoon.common.user.constant;

public enum UserStatus {
    ACTIVE("정상"),
    SUSPENDED("정지"),
    DELETED("탈퇴");

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
