package com.project.hanspoon.common.security;

import com.project.hanspoon.common.security.jwt.JwtTokenProvider;
import com.project.hanspoon.common.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationSuccessHandlerTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private OAuth2AuthenticationSuccessHandler successHandler;

    @BeforeEach
    void setUp() {
        successHandler = new OAuth2AuthenticationSuccessHandler(jwtTokenProvider);
        given(jwtTokenProvider.createToken(any(Authentication.class))).willReturn("test-jwt-token");
    }

    @Test
    @DisplayName("FRONTEND_URL이 정상 설정되어 있으면 해당 주소로 리다이렉트한다")
    void shouldRedirectToConfiguredFrontendUrl() throws Exception {
        ReflectionTestUtils.setField(successHandler, "frontendUrl", "https://front.hanspoon.com/");

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        successHandler.onAuthenticationSuccess(request, response, createAuthentication());

        assertThat(response.getRedirectedUrl())
                .isEqualTo("https://front.hanspoon.com/oauth2/redirect?token=test-jwt-token");
    }

    @Test
    @DisplayName("FRONTEND_URL이 localhost이면 운영 요청 헤더 기준으로 리다이렉트한다")
    void shouldUseForwardedHostWhenConfiguredUrlIsLocalhost() throws Exception {
        ReflectionTestUtils.setField(successHandler, "frontendUrl", "http://localhost:5173");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-Proto", "https");
        request.addHeader("X-Forwarded-Host", "www.hanspoon.com");
        request.setServerName("localhost");

        MockHttpServletResponse response = new MockHttpServletResponse();

        successHandler.onAuthenticationSuccess(request, response, createAuthentication());

        // 운영 도메인 요청에서 localhost로 튀는 문제를 막기 위한 핵심 검증입니다.
        assertThat(response.getRedirectedUrl())
                .isEqualTo("https://www.hanspoon.com/oauth2/redirect?token=test-jwt-token");
    }

    @Test
    @DisplayName("FRONTEND_URL이 운영 도메인이면 해당 주소로 리다이렉트한다")
    void shouldRedirectToHanspoonStore() throws Exception {
        ReflectionTestUtils.setField(successHandler, "frontendUrl", "https://hanspoon.store");

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        successHandler.onAuthenticationSuccess(request, response, createAuthentication());

        assertThat(response.getRedirectedUrl())
                .isEqualTo("https://hanspoon.store/oauth2/redirect?token=test-jwt-token");
    }

    private Authentication createAuthentication() {
        User user = User.builder()
                .email("tester@hanspoon.com")
                .password("encoded-password")
                .userName("테스터")
                .provider("google")
                .build();
        CustomUserDetails principal = new CustomUserDetails(user);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }
}
