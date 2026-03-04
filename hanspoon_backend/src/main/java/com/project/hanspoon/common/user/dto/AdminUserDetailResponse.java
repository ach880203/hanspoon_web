package com.project.hanspoon.common.user.dto;

import com.project.hanspoon.common.user.constant.UserStatus;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserDetailResponse {
    private Long userId;
    private String userName;
    private String email;
    private String phone;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private UserHistoryDto history;
}
