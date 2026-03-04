package com.project.hanspoon.recipe.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.hanspoon.common.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="recipe_rev")
@Getter
@Setter
public class RecipeRev {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rev_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "recipe_id")
    @JsonIgnore
    private Recipe recipe;

    @Lob
    private String content;

    private int rating;

    // 관리자 답글 본문입니다. (null이면 미답변 상태)
    @Lob
    @Column(name = "answer_content")
    private String answerContent;

    // 답글 작성 사용자 ID(관리자)입니다.
    @Column(name = "answered_by_user_id")
    private Long answeredByUserId;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    // 소프트 삭제 플래그입니다.
    @Column(name = "del_flag", nullable = false, columnDefinition = "tinyint(1) default 0")
    private boolean delFlag;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void updateReview(int rating, String content) {
        this.rating = rating;
        this.content = content;
    }

    public void markDeleted(LocalDateTime now) {
        this.delFlag = true;
        this.deletedAt = now;
    }

    public void answer(String answerContent, Long answeredByUserId, LocalDateTime answeredAt) {
        this.answerContent = answerContent;
        this.answeredByUserId = answeredByUserId;
        this.answeredAt = answeredAt;
    }

    @PrePersist
    private void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = this.createdAt;
        this.delFlag = false;
    }

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
