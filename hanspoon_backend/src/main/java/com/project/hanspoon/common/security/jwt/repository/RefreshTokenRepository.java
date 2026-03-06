package com.project.hanspoon.common.security.jwt.repository;

import com.project.hanspoon.common.security.jwt.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByUserId(Long userId);

    Optional<RefreshToken> findByToken(String token);

    void deleteByUserId(Long userId);

    /** 만료된 토큰 일괄 정리 (스케줄러에서 주기적으로 호출 가능) */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    void deleteAllExpiredBefore(@Param("now") LocalDateTime now);
}
