package com.gradientgeeks.ageis.backendapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "security_questions")
@EntityListeners(AuditingEntityListener.class)
public class SecurityQuestion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @NotBlank
    @Column(name = "question_key", nullable = false)
    private String questionKey; // e.g., "mother_maiden_name", "first_school"
    
    @NotBlank
    @Column(name = "question_text", nullable = false)
    private String questionText; // e.g., "What is your mother's maiden name?"
    
    @NotBlank
    @Column(name = "answer_hash", nullable = false)
    private String answerHash; // Store hashed answer for security
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public SecurityQuestion() {}
    
    public SecurityQuestion(User user, String questionKey, String questionText, String answerHash) {
        this.user = user;
        this.questionKey = questionKey;
        this.questionText = questionText;
        this.answerHash = answerHash;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getQuestionKey() {
        return questionKey;
    }
    
    public void setQuestionKey(String questionKey) {
        this.questionKey = questionKey;
    }
    
    public String getQuestionText() {
        return questionText;
    }
    
    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }
    
    public String getAnswerHash() {
        return answerHash;
    }
    
    public void setAnswerHash(String answerHash) {
        this.answerHash = answerHash;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}