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
import java.util.Locale;

/**
 * OAuth2 로그인 성공 핸들러
 * JWT 토큰 생성 후 프론트엔드로 리다이렉트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

        private final JwtTokenProvider jwtTokenProvider;

        @Value("${FRONTEND_URL:https://hanspoon.store}")
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
                String frontendBaseUrl = resolveFrontendBaseUrl(request);
                String targetUrl = UriComponentsBuilder.fromUriString(frontendBaseUrl + "/oauth2/redirect")
                                .queryParam("token", token)
                                .build().toUriString();

                getRedirectStrategy().sendRedirect(request, response, targetUrl);
        }

        /**
         * 배포 환경에서 FRONTEND_URL이 localhost로 잘못 남아 있어도
         * 실제 요청 호스트 기준으로 안전하게 프론트 리다이렉트를 맞춥니다.
         */
        private String resolveFrontendBaseUrl(HttpServletRequest request) {
                String configuredUrl = normalizeBaseUrl(frontendUrl);
                if (configuredUrl.isEmpty()) {
                        return resolveRequestOrigin(request);
                }

                // 서버 설정이 localhost인데 실제 요청이 운영 도메인이면 요청 Origin 우선
                if (isLocalhostUrl(configuredUrl) && !isLocalhostRequest(request)) {
                        return resolveRequestOrigin(request);
                }
                return configuredUrl;
        }

        private String resolveRequestOrigin(HttpServletRequest request) {
                String forwardedProto = firstHeaderValue(request, "X-Forwarded-Proto");
                String scheme = (forwardedProto == null || forwardedProto.isBlank()) ? request.getScheme() : forwardedProto;

                String forwardedHost = firstHeaderValue(request, "X-Forwarded-Host");
                String host = (forwardedHost == null || forwardedHost.isBlank()) ? request.getHeader("Host") : forwardedHost;

                if (host == null || host.isBlank()) {
                        String serverName = request.getServerName();
                        int serverPort = request.getServerPort();
                        boolean defaultPort = ("http".equalsIgnoreCase(scheme) && serverPort == 80)
                                        || ("https".equalsIgnoreCase(scheme) && serverPort == 443);
                        host = (serverPort > 0 && !defaultPort) ? serverName + ":" + serverPort : serverName;
                }

                return normalizeBaseUrl(scheme + "://" + host);
        }

        private String firstHeaderValue(HttpServletRequest request, String headerName) {
                String value = request.getHeader(headerName);
                if (value == null || value.isBlank()) {
                        return null;
                }
                // X-Forwarded-* 값이 콤마로 여러 개 들어오면 첫 번째 값을 사용
                return value.split(",")[0].trim();
        }

        private boolean isLocalhostRequest(HttpServletRequest request) {
                String forwardedHost = firstHeaderValue(request, "X-Forwarded-Host");
                String host = (forwardedHost == null || forwardedHost.isBlank()) ? request.getHeader("Host") : forwardedHost;
                if (host == null || host.isBlank()) {
                        host = request.getServerName();
                }
                String normalizedHost = host.toLowerCase(Locale.ROOT);
                return normalizedHost.contains("localhost") || normalizedHost.contains("127.0.0.1");
        }

        private boolean isLocalhostUrl(String url) {
                String normalized = url.toLowerCase(Locale.ROOT);
                return normalized.contains("://localhost") || normalized.contains("://127.0.0.1");
        }

        private String normalizeBaseUrl(String value) {
                if (value == null) {
                        return "";
                }
                return value.trim().replaceAll("/+$", "");
        }
}
