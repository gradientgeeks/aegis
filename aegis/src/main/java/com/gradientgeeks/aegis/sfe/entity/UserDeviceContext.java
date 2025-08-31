package com.gradientgeeks.aegis.sfe.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity for tracking anonymized user-device context for policy enforcement.
 * Maintains user patterns and velocity tracking without exposing personal information.
 */
@Entity
@Table(name = "user_device_context", indexes = {
    @Index(name = "idx_anonymized_user_id", columnList = "anonymizedUserId"),
    @Index(name = "idx_device_id", columnList = "deviceId"),
    @Index(name = "idx_organization", columnList = "organization"),
    @Index(name = "idx_user_device_org", columnList = "anonymizedUserId,deviceId,organization", unique = true),
    @Index(name = "idx_last_activity", columnList = "lastActivityAt")
})
public class UserDeviceContext {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 255)
    @Column(name = "anonymized_user_id", nullable = false)
    private String anonymizedUserId;
    
    @NotBlank
    @Size(max = 255)
    @Column(name = "device_id", nullable = false)
    private String deviceId;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "organization", nullable = false)
    private String organization;
    
    @Size(max = 100)
    @Column(name = "client_id")
    private String clientId;
    
    // User session context
    @Size(max = 50)
    @Column(name = "account_tier")
    private String accountTier;
    
    @Column(name = "account_age_months")
    private Integer accountAgeMonths;
    
    @Size(max = 50)
    @Column(name = "kyc_level")
    private String kycLevel;
    
    // Transaction velocity tracking
    @Column(name = "daily_transaction_count")
    private Integer dailyTransactionCount = 0;
    
    @Column(name = "weekly_transaction_count")
    private Integer weeklyTransactionCount = 0;
    
    @Column(name = "monthly_transaction_count")
    private Integer monthlyTransactionCount = 0;
    
    @Size(max = 50)
    @Column(name = "daily_amount_range")
    private String dailyAmountRange;
    
    @Size(max = 50)
    @Column(name = "weekly_amount_range")
    private String weeklyAmountRange;
    
    @Column(name = "last_transaction_at")
    private LocalDateTime lastTransactionAt;
    
    // Transaction amount tracking
    @Column(name = "daily_transaction_amount")
    private Double dailyTransactionAmount = 0.0;
    
    @Column(name = "weekly_transaction_amount")
    private Double weeklyTransactionAmount = 0.0;
    
    @Column(name = "monthly_transaction_amount")
    private Double monthlyTransactionAmount = 0.0;
    
    // Risk indicators
    @Column(name = "is_location_changed")
    private Boolean isLocationChanged = false;
    
    @Column(name = "is_device_changed")
    private Boolean isDeviceChanged = false;
    
    @Column(name = "is_dormant_account")
    private Boolean isDormantAccount = false;
    
    @Column(name = "risk_score")
    private Integer riskScore = 0;
    
    // Activity tracking
    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;
    
    @Column(name = "total_sessions")
    private Long totalSessions = 0L;
    
    @Column(name = "failed_attempts_count")
    private Integer failedAttemptsCount = 0;
    
    @Column(name = "last_failed_attempt_at")
    private LocalDateTime lastFailedAttemptAt;
    
    // Pattern detection
    @Column(name = "usual_transaction_time")
    private String usualTransactionTime;
    
    @Column(name = "unusual_patterns", columnDefinition = "TEXT")
    private String unusualPatterns;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public UserDeviceContext() {}
    
    public UserDeviceContext(String anonymizedUserId, String deviceId, String organization, String clientId) {
        this.anonymizedUserId = anonymizedUserId;
        this.deviceId = deviceId;
        this.organization = organization;
        this.clientId = clientId;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getAnonymizedUserId() {
        return anonymizedUserId;
    }
    
    public void setAnonymizedUserId(String anonymizedUserId) {
        this.anonymizedUserId = anonymizedUserId;
    }
    
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    
    public String getOrganization() {
        return organization;
    }
    
    public void setOrganization(String organization) {
        this.organization = organization;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    public String getAccountTier() {
        return accountTier;
    }
    
    public void setAccountTier(String accountTier) {
        this.accountTier = accountTier;
    }
    
    public Integer getAccountAgeMonths() {
        return accountAgeMonths;
    }
    
    public void setAccountAgeMonths(Integer accountAgeMonths) {
        this.accountAgeMonths = accountAgeMonths;
    }
    
    public String getKycLevel() {
        return kycLevel;
    }
    
    public void setKycLevel(String kycLevel) {
        this.kycLevel = kycLevel;
    }
    
    public Integer getDailyTransactionCount() {
        return dailyTransactionCount;
    }
    
    public void setDailyTransactionCount(Integer dailyTransactionCount) {
        this.dailyTransactionCount = dailyTransactionCount;
    }
    
    public Integer getWeeklyTransactionCount() {
        return weeklyTransactionCount;
    }
    
    public void setWeeklyTransactionCount(Integer weeklyTransactionCount) {
        this.weeklyTransactionCount = weeklyTransactionCount;
    }
    
    public Integer getMonthlyTransactionCount() {
        return monthlyTransactionCount;
    }
    
    public void setMonthlyTransactionCount(Integer monthlyTransactionCount) {
        this.monthlyTransactionCount = monthlyTransactionCount;
    }
    
    public String getDailyAmountRange() {
        return dailyAmountRange;
    }
    
    public void setDailyAmountRange(String dailyAmountRange) {
        this.dailyAmountRange = dailyAmountRange;
    }
    
    public String getWeeklyAmountRange() {
        return weeklyAmountRange;
    }
    
    public void setWeeklyAmountRange(String weeklyAmountRange) {
        this.weeklyAmountRange = weeklyAmountRange;
    }
    
    public LocalDateTime getLastTransactionAt() {
        return lastTransactionAt;
    }
    
    public void setLastTransactionAt(LocalDateTime lastTransactionAt) {
        this.lastTransactionAt = lastTransactionAt;
    }
    
    public Double getDailyTransactionAmount() {
        return dailyTransactionAmount;
    }
    
    public void setDailyTransactionAmount(Double dailyTransactionAmount) {
        this.dailyTransactionAmount = dailyTransactionAmount;
    }
    
    public Double getWeeklyTransactionAmount() {
        return weeklyTransactionAmount;
    }
    
    public void setWeeklyTransactionAmount(Double weeklyTransactionAmount) {
        this.weeklyTransactionAmount = weeklyTransactionAmount;
    }
    
    public Double getMonthlyTransactionAmount() {
        return monthlyTransactionAmount;
    }
    
    public void setMonthlyTransactionAmount(Double monthlyTransactionAmount) {
        this.monthlyTransactionAmount = monthlyTransactionAmount;
    }
    
    public Boolean getIsLocationChanged() {
        return isLocationChanged;
    }
    
    public void setIsLocationChanged(Boolean isLocationChanged) {
        this.isLocationChanged = isLocationChanged;
    }
    
    public Boolean getIsDeviceChanged() {
        return isDeviceChanged;
    }
    
    public void setIsDeviceChanged(Boolean isDeviceChanged) {
        this.isDeviceChanged = isDeviceChanged;
    }
    
    public Boolean getIsDormantAccount() {
        return isDormantAccount;
    }
    
    public void setIsDormantAccount(Boolean isDormantAccount) {
        this.isDormantAccount = isDormantAccount;
    }
    
    public Integer getRiskScore() {
        return riskScore;
    }
    
    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }
    
    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }
    
    public void setLastActivityAt(LocalDateTime lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }
    
    public Long getTotalSessions() {
        return totalSessions;
    }
    
    public void setTotalSessions(Long totalSessions) {
        this.totalSessions = totalSessions;
    }
    
    public Integer getFailedAttemptsCount() {
        return failedAttemptsCount;
    }
    
    public void setFailedAttemptsCount(Integer failedAttemptsCount) {
        this.failedAttemptsCount = failedAttemptsCount;
    }
    
    public LocalDateTime getLastFailedAttemptAt() {
        return lastFailedAttemptAt;
    }
    
    public void setLastFailedAttemptAt(LocalDateTime lastFailedAttemptAt) {
        this.lastFailedAttemptAt = lastFailedAttemptAt;
    }
    
    public String getUsualTransactionTime() {
        return usualTransactionTime;
    }
    
    public void setUsualTransactionTime(String usualTransactionTime) {
        this.usualTransactionTime = usualTransactionTime;
    }
    
    public String getUnusualPatterns() {
        return unusualPatterns;
    }
    
    public void setUnusualPatterns(String unusualPatterns) {
        this.unusualPatterns = unusualPatterns;
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
    
    // Helper methods
    public void incrementDailyTransactionCount() {
        this.dailyTransactionCount = (this.dailyTransactionCount == null) ? 1 : this.dailyTransactionCount + 1;
    }
    
    public void incrementWeeklyTransactionCount() {
        this.weeklyTransactionCount = (this.weeklyTransactionCount == null) ? 1 : this.weeklyTransactionCount + 1;
    }
    
    public void incrementMonthlyTransactionCount() {
        this.monthlyTransactionCount = (this.monthlyTransactionCount == null) ? 1 : this.monthlyTransactionCount + 1;
    }
    
    public void incrementTotalSessions() {
        this.totalSessions = (this.totalSessions == null) ? 1L : this.totalSessions + 1L;
    }
    
    public void incrementFailedAttempts() {
        this.failedAttemptsCount = (this.failedAttemptsCount == null) ? 1 : this.failedAttemptsCount + 1;
        this.lastFailedAttemptAt = LocalDateTime.now();
    }
    
    public void resetFailedAttempts() {
        this.failedAttemptsCount = 0;
        this.lastFailedAttemptAt = null;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDeviceContext that = (UserDeviceContext) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "UserDeviceContext{" +
                "id=" + id +
                ", anonymizedUserId='" + anonymizedUserId + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", organization='" + organization + '\'' +
                ", accountTier='" + accountTier + '\'' +
                ", dailyTransactionCount=" + dailyTransactionCount +
                ", riskScore=" + riskScore +
                '}';
    }
}