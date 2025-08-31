package com.gradientgeeks.aegis.sfe.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing a policy violation incident.
 * Used for audit trail and compliance reporting.
 */
@Entity
@Table(name = "policy_violations", indexes = {
    @Index(name = "idx_device_id", columnList = "deviceId"),
    @Index(name = "idx_anonymized_user_id", columnList = "anonymizedUserId"),
    @Index(name = "idx_organization", columnList = "organization"),
    @Index(name = "idx_policy_id", columnList = "policy_id"),
    @Index(name = "idx_created_at", columnList = "createdAt"),
    @Index(name = "idx_action_taken", columnList = "actionTaken")
})
public class PolicyViolation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 255)
    @Column(name = "device_id", nullable = false)
    private String deviceId;
    
    @Size(max = 255)
    @Column(name = "anonymized_user_id")
    private String anonymizedUserId;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "organization", nullable = false)
    private String organization;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_rule_id")
    private PolicyRule violatedRule;
    
    @NotNull
    @Column(name = "action_taken", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private Policy.EnforcementLevel actionTaken;
    
    @Column(name = "request_details", columnDefinition = "TEXT")
    private String requestDetails;
    
    @Column(name = "violation_details", columnDefinition = "TEXT")
    private String violationDetails;
    
    @Size(max = 100)
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Size(max = 500)
    @Column(name = "user_agent")
    private String userAgent;
    
    @Size(max = 100)
    @Column(name = "client_id")
    private String clientId;
    
    @Column(name = "severity_score")
    private Integer severityScore;
    
    @Column(name = "risk_score")
    private Integer riskScore;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public PolicyViolation() {}
    
    public PolicyViolation(String deviceId, String organization, Policy policy, Policy.EnforcementLevel actionTaken) {
        this.deviceId = deviceId;
        this.organization = organization;
        this.policy = policy;
        this.actionTaken = actionTaken;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    
    public String getAnonymizedUserId() {
        return anonymizedUserId;
    }
    
    public void setAnonymizedUserId(String anonymizedUserId) {
        this.anonymizedUserId = anonymizedUserId;
    }
    
    public String getOrganization() {
        return organization;
    }
    
    public void setOrganization(String organization) {
        this.organization = organization;
    }
    
    public Policy getPolicy() {
        return policy;
    }
    
    public void setPolicy(Policy policy) {
        this.policy = policy;
    }
    
    public PolicyRule getViolatedRule() {
        return violatedRule;
    }
    
    public void setViolatedRule(PolicyRule violatedRule) {
        this.violatedRule = violatedRule;
    }
    
    public Policy.EnforcementLevel getActionTaken() {
        return actionTaken;
    }
    
    public void setActionTaken(Policy.EnforcementLevel actionTaken) {
        this.actionTaken = actionTaken;
    }
    
    public String getRequestDetails() {
        return requestDetails;
    }
    
    public void setRequestDetails(String requestDetails) {
        this.requestDetails = requestDetails;
    }
    
    public String getViolationDetails() {
        return violationDetails;
    }
    
    public void setViolationDetails(String violationDetails) {
        this.violationDetails = violationDetails;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    public Integer getSeverityScore() {
        return severityScore;
    }
    
    public void setSeverityScore(Integer severityScore) {
        this.severityScore = severityScore;
    }
    
    public Integer getRiskScore() {
        return riskScore;
    }
    
    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PolicyViolation that = (PolicyViolation) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "PolicyViolation{" +
                "id=" + id +
                ", deviceId='" + deviceId + '\'' +
                ", anonymizedUserId='" + anonymizedUserId + '\'' +
                ", organization='" + organization + '\'' +
                ", actionTaken=" + actionTaken +
                ", severityScore=" + severityScore +
                ", createdAt=" + createdAt +
                '}';
    }
}