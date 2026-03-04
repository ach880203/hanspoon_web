package com.project.hanspoon.common.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FindPasswordRequest {
    private String email;
    private String userName;
    private String phone;
}
