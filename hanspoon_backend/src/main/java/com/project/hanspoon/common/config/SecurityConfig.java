package com.project.hanspoon.common.config;

import com.project.hanspoon.common.security.CustomOAuth2UserService;
import com.project.hanspoon.common.security.CustomUserDetailsService;
import com.project.hanspoon.common.security.OAuth2AuthenticationSuccessHandler;
import com.project.hanspoon.common.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 설정 (JWT + OAuth2).
 *
 * 보안 정책 원칙:
 * 1) 인증/헬스체크/공개 조회 API만 permitAll
 * 2) 관리자 API는 ROLE_ADMIN 강제
 * 3) 나머지 /api/** 는 인증 필요
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final CustomUserDetailsService userDetailsService;
        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
        private final CustomOAuth2UserService customOAuth2UserService;

        @Bean
        public AuthenticationManager authenticationManager(
                        AuthenticationConfiguration authenticationConfiguration) throws Exception {
                return authenticationConfiguration.getAuthenticationManager();
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .cors(Customizer.withDefaults())
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                // 인증/인가 없이 접근 가능한 엔드포인트
                                                .requestMatchers("/api/auth/**").permitAll()
                                                .requestMatchers("/api/health", "/api/info").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/notice/**", "/api/faq/**",
                                                                "/api/events", "/api/events/**",
                                                                "/api/banners", "/api/banners/**")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET,
                                                                "/api/products",
                                                                "/api/products/*",
                                                                "/api/products/*/images")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET,
                                                                "/api/oneday/home",
                                                                "/api/oneday/classes",
                                                                "/api/oneday/classes/*",
                                                                "/api/oneday/classes/*/sessions",
                                                                "/api/oneday/sessions/search")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET,
                                                                "/api/recipe/list",
                                                                "/api/recipe/detail/*",
                                                                "/api/recipe/reviews/recipes/*",
                                                                "/api/recipe/inquiries/recipes/*")
                                                .permitAll()

                                                // 관리자 전용 API
                                                .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/api/products")
                                                .hasAuthority("ROLE_ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/products/*")
                                                .hasAuthority("ROLE_ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/api/products/*/images")
                                                .hasAuthority("ROLE_ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/products/*/images/*")
                                                .hasAuthority("ROLE_ADMIN")

                                                // 정적 리소스 공개
                                                .requestMatchers("/images/**", "/css/**", "/js/**").permitAll()

                                                // 위에서 허용하지 않은 API는 인증 필수
                                                .requestMatchers("/api/**").authenticated()
                                                .anyRequest().permitAll())
                                .oauth2Login(oauth2 -> oauth2
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .userService(customOAuth2UserService))
                                                .successHandler(oAuth2AuthenticationSuccessHandler)
                                                .failureUrl("/api/auth/oauth2/failure"))
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        response.setContentType("application/json;charset=UTF-8");
                                                        response.setStatus(401);
                                                        response.getWriter().write(
                                                                        "{\"success\":false,\"message\":\"로그인이 필요합니다.\"}");
                                                })
                                                .accessDeniedHandler((request, response, accessDeniedException) -> {
                                                        response.setContentType("application/json;charset=UTF-8");
                                                        response.setStatus(403);
                                                        response.getWriter().write(
                                                                        "{\"success\":false,\"message\":\"접근 권한이 없습니다.\"}");
                                                }))
                                .userDetailsService(userDetailsService);

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                // CORS 진단용 모든 허용 (credentials=true 일 때는 allowedOriginPatterns 사용)
                configuration.setAllowedOriginPatterns(List.of("*"));
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(List.of("*"));
                configuration.setAllowCredentials(true);
                configuration.setExposedHeaders(List.of("Authorization"));

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/api/**", configuration);
                return source;
        }
}
