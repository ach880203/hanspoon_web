package com.project.hanspoon.common.security.jwt.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Refresh Token DB 저장 엔티티.
 * - 사용자당 1개의 토큰만 유지 (로그인 시 기존 토큰 교체)
 * - Rotation 방식: 리프레시 요청 시 기존 토큰 폐기 후 새로 발급
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false, length = 64)
    private String token;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 토큰 로테이션: 새 토큰 값과 만료 일시로 갱신.
     */
    public void rotate(String newToken, LocalDateTime newExpiry) {
        this.token = newToken;
        this.expiryDate = newExpiry;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }
}
