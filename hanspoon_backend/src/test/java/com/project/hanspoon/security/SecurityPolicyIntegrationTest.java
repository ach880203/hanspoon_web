package com.project.hanspoon.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 보안 정책 회귀 방지 테스트.
 *
 * 검증 포인트:
 * 1) 공개 API는 인증 없이 접근 가능해야 한다.
 * 2) 인증이 필요한 API는 비로그인 요청 시 401이어야 한다.
 * 3) 관리자 API는 일반 사용자 권한으로 접근 시 403이어야 한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityPolicyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthApi_isPublic() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }

    @Test
    void paymentHistory_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/payment/history"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void adminApi_requiresAdminRole() throws Exception {
        mockMvc.perform(get("/api/admin/users/list"))
                .andExpect(status().isForbidden());
    }
}

