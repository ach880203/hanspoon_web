package com.project.hanspoon.common.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JWT 토큰 생성 및 검증 유틸리티
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:mySecretKeyForJwtTokenGenerationMustBe256BitsLong}")
    private String secretKeyString;

    /** Access Token: 30분 */
    private static final long ACCESS_TOKEN_VALIDITY_MS = 30 * 60 * 1000L;

    /** Refresh Token: 14일 */
    private static final long REFRESH_TOKEN_VALIDITY_MS = 14L * 24 * 60 * 60 * 1000L;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes());
    }

    // ----------------------------------------------------------------
    // Access Token
    // ----------------------------------------------------------------

    /**
     * Access Token 생성 (30분, auth_time 포함).
     */
    public String createAccessToken(Authentication authentication) {
        String email = authentication.getName();
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date now = new Date();
        long authTime = now.getTime() / 1000L;

        return Jwts.builder()
                .subject(email)
                .claim("roles", roles)
                .claim("auth_time", authTime)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ACCESS_TOKEN_VALIDITY_MS))
                .signWith(secretKey)
                .compact();
    }

    /** 하위 호환 (@deprecated → createAccessToken 사용 권장) */
    @Deprecated
    public String createToken(Authentication authentication) {
        return createAccessToken(authentication);
    }

    // ----------------------------------------------------------------
    // Refresh Token
    // ----------------------------------------------------------------

    /** UUID 기반 Refresh Token 생성 (DB 저장용). */
    public String createRefreshToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public long getRefreshTokenValidityMs() {
        return REFRESH_TOKEN_VALIDITY_MS;
    }

    // ----------------------------------------------------------------
    // Claims 추출
    // ----------------------------------------------------------------

    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public String getRolesFromToken(String token) {
        return parseClaims(token).get("roles", String.class);
    }

    /** auth_time 클레임 추출 (Unix seconds). 결제 보안 검증에 사용. */
    public Long getAuthTimeFromToken(String token) {
        return parseClaims(token).get("auth_time", Long.class);
    }

    // ----------------------------------------------------------------
    // 검증
    // ----------------------------------------------------------------
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("[JWT] 잘못된 서명: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("[JWT] 만료된 토큰: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("[JWT] 지원되지 않는 토큰: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("[JWT] 토큰이 비어있음: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 결제 API 보안: 최근 인증(auth_time)으로부터 maxAgeSeconds 이내인지 검증.
     * 로그인 시 설정된 auth_time은 리프레시 후에도 변경되지 않으므로
     * 결제 전 반드시 재로그인이 필요합니다.
     *
     * @param token         Access Token
     * @param maxAgeSeconds 허용 최대 경과 시간 (결제: 300 = 5분)
     */
    public boolean isWithinRecentAuth(String token, long maxAgeSeconds) {
        try {
            Long authTime = getAuthTimeFromToken(token);
            if (authTime == null)
                return false;
            long nowSeconds = System.currentTimeMillis() / 1000L;
            return (nowSeconds - authTime) <= maxAgeSeconds;
        } catch (Exception e) {
            log.warn("[JWT] auth_time 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    // ----------------------------------------------------------------
    // Internal
    // ----------------------------------------------------------------

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
