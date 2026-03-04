package com.project.hanspoon.oneday.instructor.entity;

import com.project.hanspoon.common.entity.BaseTimeEntity;
import com.project.hanspoon.common.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "instructor")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Instructor extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Legacy DB compatibility: some schemas also require member_id.
    @Column(name = "member_id", nullable = false, unique = true)
    private Long legacyMemberId;

    // Legacy DB compatibility: some schemas keep updatedat as NOT NULL.
    @Column(name = "updatedat", nullable = false)
    private LocalDateTime legacyUpdatedAt;


    @Column(nullable = false, length = 1000)
    private String bio;

    @Column(length = 1000)
    private String specialty;

    @Column(length = 2000)
    private String career;

    @Column(columnDefinition = "LONGTEXT")
    private String profileImageData;

    @Builder
    private Instructor(User user, String bio, String specialty, String career, String profileImageData) {
        this.user = user;
        this.legacyMemberId = (user != null ? user.getUserId() : null);
        this.bio = bio;
        this.specialty = specialty;
        this.career = career;
        this.profileImageData = profileImageData;
    }

    public void updateProfile(String bio, String specialty, String career, String profileImageData) {
        this.bio = bio;
        this.specialty = specialty;
        this.career = career;
        this.profileImageData = profileImageData;
    }

    @PrePersist
    @PreUpdate
    private void syncLegacyMemberId() {
        if (this.user != null) {
            this.legacyMemberId = this.user.getUserId();
        }
        LocalDateTime now = LocalDateTime.now();
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
        this.legacyUpdatedAt = this.updatedAt;
    }
}
