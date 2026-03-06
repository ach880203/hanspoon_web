package com.project.hanspoon.common.user.controller;

import com.project.hanspoon.common.response.ApiResponse;
import com.project.hanspoon.common.security.CustomUserDetails;
import com.project.hanspoon.common.security.jwt.JwtTokenProvider;
import com.project.hanspoon.common.user.dto.LoginRequest;
import com.project.hanspoon.common.user.dto.LoginResponse;
import com.project.hanspoon.common.user.dto.UserRegisterDto;
import com.project.hanspoon.common.user.dto.FindIdRequest;
import com.project.hanspoon.common.user.dto.FindPasswordRequest;
import com.project.hanspoon.common.user.entity.User;
import com.project.hanspoon.common.user.service.AuthService;
import com.project.hanspoon.common.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_COOKIE_NAME = "refresh_token";
    private static final int REFRESH_COOKIE_MAX_AGE_SEC = 14 * 24 * 60 * 60;

    private final UserService userService;
    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Login endpoint.
     *
     * Important:
     * - Token authority claims are generated from the authenticated principal.
     * - Response role is normalized to a single token like ROLE_ADMIN.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @RequestBody LoginRequest request,
            HttpServletResponse httpResponse) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Access Token (30분)
            String accessToken = jwtTokenProvider.createAccessToken(authentication);
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            userService.updateLastLogin(user.getUserId());

            // Refresh Token → HttpOnly 쿠키
            AuthService.TokenPair tokens = authService.login(request.getEmail(), request.getPassword());
            setRefreshCookie(httpResponse, tokens.refreshToken());

            LoginResponse response = LoginResponse.builder()
                    .accessToken(tokens.accessToken())
                    .tokenType("Bearer")
                    .userId(user.getUserId())
                    .email(user.getEmail())
                    .userName(user.getUserName())
                    .spoonBalance(user.getSpoonBalance())
                    .role(resolveRole(user, userDetails))
                    .build();

            return ResponseEntity.ok(ApiResponse.ok("로그인에 성공했습니다.", response));
        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("이메일 또는 비밀번호가 올바르지 않습니다."));
        } catch (LockedException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("계정이 잠겨 있습니다."));
        } catch (DisabledException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("비활성화된 계정입니다."));
        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("로그인에 실패했습니다: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("서버 오류가 발생했습니다."));
        }
    }

    /**
     * Refresh Token으로 Access Token 재발급 (Rotation 방식).
     * Refresh Token은 쿠키에서 읽고, 새 Refresh Token은 쿠키로 반환합니다.
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        String oldRefreshToken = extractRefreshCookie(request);
        if (oldRefreshToken == null) {
            return ResponseEntity.status(401).body(
                    ApiResponse.error("Refresh Token이 없습니다. 다시 로그인해 주세요."));
        }
        try {
            AuthService.TokenPair tokens = authService.refresh(oldRefreshToken);
            setRefreshCookie(response, tokens.refreshToken());
            return ResponseEntity.ok(ApiResponse.ok("토큰이 재발급되었습니다.",
                    Map.of("accessToken", tokens.accessToken())));
        } catch (IllegalArgumentException | IllegalStateException e) {
            clearRefreshCookie(response);
            return ResponseEntity.status(401).body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 로그아웃: DB에서 Refresh Token 삭제 + 쿠키 만료.
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletResponse response) {
        if (userDetails != null) {
            authService.logout(userDetails.getUserId());
        }
        clearRefreshCookie(response);
        return ResponseEntity.ok(ApiResponse.ok("로그아웃이 완료되었습니다."));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody UserRegisterDto dto) {
        try {
            userService.register(dto);
            return ResponseEntity.ok(ApiResponse.ok("회원가입에 성공했습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmail(@RequestParam String email) {
        boolean available = !userService.isEmailExists(email);
        String message = available ? "사용 가능한 이메일입니다." : "이미 사용 중인 이메일입니다.";
        return ResponseEntity.ok(ApiResponse.ok(message, available));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<LoginResponse>> getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("인증이 필요합니다."));
        }

        User user = userDetails.getUser();
        LoginResponse response = LoginResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .userName(user.getUserName())
                .spoonBalance(user.getSpoonBalance()) // ✅ 추가
                .role(resolveRole(user, userDetails))
                .build();

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/oauth2/success")
    public ResponseEntity<ApiResponse<LoginResponse>> oauth2Success(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("소셜 로그인 인증에 실패했습니다."));
        }

        User user = userDetails.getUser();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());
        String token = jwtTokenProvider.createAccessToken(authentication);

        LoginResponse response = LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .userId(user.getUserId())
                .email(user.getEmail())
                .userName(user.getUserName())
                .spoonBalance(user.getSpoonBalance()) // ✅ 추가
                .role(resolveRole(user, userDetails))
                .build();

        return ResponseEntity.ok(ApiResponse.ok("소셜 로그인에 성공했습니다.", response));
    }

    @GetMapping("/oauth2/failure")
    public ResponseEntity<ApiResponse<Void>> oauth2Failure() {
        return ResponseEntity.badRequest().body(ApiResponse.error("소셜 로그인에 실패했습니다."));
    }

    @PostMapping("/find-email")
    public ResponseEntity<ApiResponse<String>> findEmail(@RequestBody FindIdRequest request) {
        try {
            String email = userService.findEmail(request.getUserName(), request.getPhone());
            return ResponseEntity.ok(ApiResponse.ok("이메일 찾기에 성공했습니다.", email));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody FindPasswordRequest request) {
        try {
            String tempPassword = userService.resetPassword(
                    request.getEmail(), request.getUserName(), request.getPhone());
            return ResponseEntity.ok(ApiResponse.ok("비밀번호 재설정에 성공했습니다.", tempPassword));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Normalize role for client-side authorization checks.
     *
     * Priority:
     * 1) user.role from DB
     * 2) first granted authority
     * 3) ROLE_USER fallback
     */
    private String resolveRole(User user, CustomUserDetails userDetails) {
        if (user != null && user.getRole() != null && !user.getRole().isBlank()) {
            return user.getRole().trim();
        }

        if (userDetails != null && userDetails.getAuthorities() != null) {
            return userDetails.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .filter(a -> a != null && !a.isBlank())
                    .findFirst()
                    .orElse("ROLE_USER");
        }

        return "ROLE_USER";
    }

    // ----------------------------------------------------------------
    // Refresh Token 쿠키 헬퍼
    // ----------------------------------------------------------------

    private void setRefreshCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/api/auth"); // refresh/logout 경로에만 전송
        cookie.setMaxAge(REFRESH_COOKIE_MAX_AGE_SEC);
        // HTTPS 환경에서는 아래 주석 해제
        // cookie.setSecure(true);
        response.addCookie(cookie);
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String extractRefreshCookie(HttpServletRequest request) {
        if (request.getCookies() == null)
            return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> REFRESH_COOKIE_NAME.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
