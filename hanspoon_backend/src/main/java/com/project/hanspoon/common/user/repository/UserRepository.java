package com.project.hanspoon.common.user.repository;

import com.project.hanspoon.common.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByEmailAndIsDeletedFalse(String email);

    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    Optional<User> findByUserNameAndPhoneAndIsDeletedFalse(String userName, String phone);

    Optional<User> findByEmailAndUserNameAndPhoneAndIsDeletedFalse(String email, String userName, String phone);

    org.springframework.data.domain.Page<User> findByEmailContainingIgnoreCaseOrUserNameContainingIgnoreCase(
            String email, String userName, org.springframework.data.domain.Pageable pageable);

    long countByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
}
