package com.project.hanspoon.common.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDto {
    private String userName;
    private String phone;
    private String address;
    
    // 비밀번호 변경 시 사용 (null이면 변경 안함)
    private String currentPassword;
    private String newPassword;
    private String newPasswordConfirm;
}
