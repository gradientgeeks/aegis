package com.gradientgeeks.ageis.backendapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_kyc")
@EntityListeners(AuditingEntityListener.class)
public class UserKYC {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @NotBlank
    @Size(min = 4, max = 4)
    @Column(name = "aadhaar_last4", nullable = false)
    private String aadhaarLast4;
    
    @NotBlank
    @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]")
    @Column(name = "pan_number", nullable = false)
    private String panNumber;
    
    @Column(name = "aadhaar_hash")
    private String aadhaarHash; // Store hash of full Aadhaar for verification
    
    @Column(name = "is_verified")
    private Boolean isVerified = false;
    
    @Column(name = "verification_date")
    private LocalDateTime verificationDate;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public UserKYC() {}
    
    public UserKYC(User user, String aadhaarLast4, String panNumber) {
        this.user = user;
        this.aadhaarLast4 = aadhaarLast4;
        this.panNumber = panNumber;
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
    
    public String getAadhaarLast4() {
        return aadhaarLast4;
    }
    
    public void setAadhaarLast4(String aadhaarLast4) {
        this.aadhaarLast4 = aadhaarLast4;
    }
    
    public String getPanNumber() {
        return panNumber;
    }
    
    public void setPanNumber(String panNumber) {
        this.panNumber = panNumber;
    }
    
    public String getAadhaarHash() {
        return aadhaarHash;
    }
    
    public void setAadhaarHash(String aadhaarHash) {
        this.aadhaarHash = aadhaarHash;
    }
    
    public Boolean getIsVerified() {
        return isVerified;
    }
    
    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }
    
    public LocalDateTime getVerificationDate() {
        return verificationDate;
    }
    
    public void setVerificationDate(LocalDateTime verificationDate) {
        this.verificationDate = verificationDate;
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