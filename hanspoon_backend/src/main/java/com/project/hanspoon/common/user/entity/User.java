package com.project.hanspoon.common.user.entity;

import com.project.hanspoon.common.user.constant.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "user_name", nullable = false, length = 30)
    private String userName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "email", nullable = false, unique = true, length = 50)
    private String email;

    @Column(name = "provider", length = 20)
    private String provider;

    @Column(name = "provider_id", length = 100)
    private String providerId;

    @Column(name = "spoon_count")
    @Builder.Default
    private Integer spoonCount = 0;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "role", length = 20)
    @Builder.Default
    private String role = "ROLE_USER";

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public int getSpoonBalance() {
        return this.spoonCount != null ? this.spoonCount : 0;
    }

    public boolean updateProfile(String userName, String phone, String address) {
        if (userName != null && !userName.isBlank())
            this.userName = userName;
        if (phone != null && !phone.isBlank())
            this.phone = phone;
        if (address != null && !address.isBlank())
            this.address = address;
        return true;
    }

    public void softDelete() {
        this.isDeleted = true;
        this.status = UserStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }

    public void suspend() {
        this.status = UserStatus.SUSPENDED;
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
        this.isDeleted = false;
    }

    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void addSpoon(int amount) {
        if (this.spoonCount == null)
            this.spoonCount = 0;
        this.spoonCount += amount;
    }

    public boolean useSpoon(int amount) {
        if (this.spoonCount == null) this.spoonCount = 0;
        if (this.spoonCount < amount)
            return false;
        this.spoonCount -= amount;
        return true;
    }
}
