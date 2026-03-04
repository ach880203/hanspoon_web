package com.project.hanspoon.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Method Security 설정 (@PreAuthorize 활성화)
 * dev 프로필이 아닐 때만 활성화하여 개발 모드에서 권한 체크를 건너뛸 수 있게 함
 */
@Configuration
@Profile("!dev")
@EnableMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfig {
}
