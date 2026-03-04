package com.project.hanspoon.recipe.entity;

import com.project.hanspoon.common.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="recipe_ing")
@Getter
@Setter
public class RecipeIng { //문의글

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ing_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    @Column(length = 50)
    private String category;

    @Column(length = 150)
    private String title;

    @Lob
    private String content; //문의내용

    @Column(name = "secret", nullable = false, columnDefinition = "tinyint(1) default 0")
    private boolean secret;

    @Lob
    private String answer; //답글 내용

    @Column(columnDefinition = "boolean default false")
    private boolean isAnswered; //답글 여부

    @Column(name = "answered_by_user_id")
    private Long answeredByUserId;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void updateQuestion(String category, String title, String content, boolean secret) {
        this.category = category;
        this.title = title;
        this.content = content;
        this.secret = secret;
    }

    public void answerQuestion(String answer, Long answeredByUserId, LocalDateTime answeredAt) {
        this.answer = answer;
        this.answeredByUserId = answeredByUserId;
        this.answeredAt = answeredAt;
        this.isAnswered = true;
    }

    @PrePersist
    private void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
