package com.project.hanspoon.common.user.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String accessToken;
    private String tokenType;
    private Long userId;
    private String email;
    private String userName;
    private String role;
    private int spoonBalance;
}
