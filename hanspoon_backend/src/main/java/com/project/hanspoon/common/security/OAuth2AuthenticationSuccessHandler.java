package com.project.hanspoon.common.security;

import com.project.hanspoon.common.security.jwt.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * OAuth2 로그인 성공 핸들러
 * JWT 토큰 생성 후 프론트엔드로 리다이렉트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

        private final JwtTokenProvider jwtTokenProvider;

        @Value("${FRONTEND_URL:http://localhost:5173}")
        private String frontendUrl;

        @Override
        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                        Authentication authentication) throws IOException, ServletException {

                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

                // JWT 토큰 생성
                String token = jwtTokenProvider.createToken(authentication);

                log.info("OAuth2 로그인 성공: email={}, provider={}",
                                userDetails.getUser().getEmail(),
                                userDetails.getUser().getProvider());

                // 프론트엔드로 토큰 전달
                String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth2/redirect")
                                .queryParam("token", token)
                                .build().toUriString();

                getRedirectStrategy().sendRedirect(request, response, targetUrl);
        }
}
