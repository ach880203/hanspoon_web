package com.project.hanspoon.common.user.service;

import com.project.hanspoon.common.security.CustomUserDetails;
import com.project.hanspoon.common.security.CustomUserDetailsService;
import com.project.hanspoon.common.security.jwt.JwtTokenProvider;
import com.project.hanspoon.common.security.jwt.entity.RefreshToken;
import com.project.hanspoon.common.security.jwt.repository.RefreshTokenRepository;
import com.project.hanspoon.common.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 인증 관련 비즈니스 로직.
 * - 로그인: Access + Refresh Token 동시 발급
 * - 리프레시: Rotation 방식으로 새 토큰 세트 발급
 * - 로그아웃: DB의 Refresh Token 삭제
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CustomUserDetailsService userDetailsService;

    public record TokenPair(String accessToken, String refreshToken) {
    }

    // ----------------------------------------------------------------
    // 로그인
    // ----------------------------------------------------------------

    /**
     * 이메일/비밀번호 인증 후 Access + Refresh Token 발급.
     */
    @Transactional
    public TokenPair login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));

        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        String newRefreshToken = issueAndSaveRefreshToken(authentication);

        log.info("[Auth] 로그인 성공: {}", email);
        return new TokenPair(accessToken, newRefreshToken);
    }

    // ----------------------------------------------------------------
    // 리프레시 (Rotation)
    // ----------------------------------------------------------------

    /**
     * 기존 Refresh Token 검증 → 폐기 → 새 Access + Refresh Token 발급.
     */
    @Transactional
    public TokenPair refresh(String oldRefreshToken) {
        RefreshToken stored = refreshTokenRepository.findByToken(oldRefreshToken)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 Refresh Token입니다."));

        if (stored.isExpired()) {
            refreshTokenRepository.delete(stored);
            throw new IllegalStateException("Refresh Token이 만료되었습니다. 다시 로그인해 주세요.");
        }

        // 새 토큰 발급
        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService
                .loadUserByUsername(getUserEmail(stored.getUserId()));

        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        String newAccess = jwtTokenProvider.createAccessToken(auth);

        // auth_time 유지: 기존 access token은 없으므로 새로 설정 (refresh 시점을 인증 시각으로 갱신하지 않음 → 결제
        // 보안을 위해 기존 auth_time 유지)
        // Rotation: 기존 토큰 폐기 & 새 refresh 발급
        String newRefresh = jwtTokenProvider.createRefreshToken();
        LocalDateTime newExpiry = LocalDateTime.now()
                .plusSeconds(jwtTokenProvider.getRefreshTokenValidityMs() / 1000L);
        stored.rotate(newRefresh, newExpiry);

        log.info("[Auth] 토큰 리프레시 완료: userId={}", stored.getUserId());
        return new TokenPair(newAccess, newRefresh);
    }

    // ----------------------------------------------------------------
    // 로그아웃
    // ----------------------------------------------------------------

    /**
     * 해당 사용자의 Refresh Token을 DB에서 삭제.
     */
    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
        log.info("[Auth] 로그아웃 완료: userId={}", userId);
    }

    // ----------------------------------------------------------------
    // Internal
    // ----------------------------------------------------------------

    private String issueAndSaveRefreshToken(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        String token = jwtTokenProvider.createRefreshToken();
        LocalDateTime expiry = LocalDateTime.now()
                .plusSeconds(jwtTokenProvider.getRefreshTokenValidityMs() / 1000L);

        refreshTokenRepository.findByUserId(userId)
                .ifPresentOrElse(
                        existing -> existing.rotate(token, expiry),
                        () -> refreshTokenRepository.save(RefreshToken.builder()
                                .userId(userId)
                                .token(token)
                                .expiryDate(expiry)
                                .build()));
        return token;
    }

    private String getUserEmail(Long userId) {
        return userDetailsService.loadUserById(userId).getUsername();
    }
}
