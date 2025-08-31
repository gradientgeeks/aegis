package com.gradientgeeks.aegis.sfe.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entity representing an organization-specific security policy.
 * Policies define rules that are enforced during request validation.
 */
@Entity
@Table(name = "policies", indexes = {
    @Index(name = "idx_organization", columnList = "organization"),
    @Index(name = "idx_policy_type_organization", columnList = "policyType,organization"),
    @Index(name = "idx_is_active", columnList = "isActive")
})
public class Policy {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "policy_name", nullable = false)
    private String policyName;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "organization", nullable = false)
    private String organization;
    
    @NotNull
    @Column(name = "policy_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private PolicyType policyType;
    
    @NotNull
    @Column(name = "enforcement_level", nullable = false)
    @Enumerated(EnumType.STRING)
    private EnforcementLevel enforcementLevel;
    
    @Size(max = 500)
    @Column(name = "description")
    private String description;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "priority", nullable = false)
    private Integer priority = 100;
    
    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<PolicyRule> rules = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public Policy() {}
    
    public Policy(String policyName, String organization, PolicyType policyType, EnforcementLevel enforcementLevel) {
        this.policyName = policyName;
        this.organization = organization;
        this.policyType = policyType;
        this.enforcementLevel = enforcementLevel;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getPolicyName() {
        return policyName;
    }
    
    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }
    
    public String getOrganization() {
        return organization;
    }
    
    public void setOrganization(String organization) {
        this.organization = organization;
    }
    
    public PolicyType getPolicyType() {
        return policyType;
    }
    
    public void setPolicyType(PolicyType policyType) {
        this.policyType = policyType;
    }
    
    public EnforcementLevel getEnforcementLevel() {
        return enforcementLevel;
    }
    
    public void setEnforcementLevel(EnforcementLevel enforcementLevel) {
        this.enforcementLevel = enforcementLevel;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Integer getPriority() {
        return priority;
    }
    
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
    
    public List<PolicyRule> getRules() {
        return rules;
    }
    
    public void setRules(List<PolicyRule> rules) {
        if (this.rules == null) {
            this.rules = new ArrayList<>();
        }
        this.rules.clear();
        if (rules != null) {
            rules.forEach(rule -> {
                rule.setPolicy(this);
                this.rules.add(rule);
            });
        }
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
    public void addRule(PolicyRule rule) {
        rules.add(rule);
        rule.setPolicy(this);
    }
    
    public void removeRule(PolicyRule rule) {
        rules.remove(rule);
        rule.setPolicy(null);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Policy policy = (Policy) o;
        return Objects.equals(id, policy.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Policy{" +
                "id=" + id +
                ", policyName='" + policyName + '\'' +
                ", organization='" + organization + '\'' +
                ", policyType=" + policyType +
                ", enforcementLevel=" + enforcementLevel +
                ", isActive=" + isActive +
                ", priority=" + priority +
                '}';
    }
    
    /**
     * Policy types for different security areas
     */
    public enum PolicyType {
        DEVICE_SECURITY,
        TRANSACTION_LIMIT,
        GEOGRAPHIC_RESTRICTION,
        TIME_RESTRICTION,
        DEVICE_BINDING,
        RISK_ASSESSMENT,
        API_RATE_LIMIT,
        AUTHENTICATION_REQUIREMENT,
        VELOCITY_CHECK,
        ACCOUNT_SECURITY
    }
    
    /**
     * Enforcement levels for policy violations
     */
    public enum EnforcementLevel {
        BLOCK,          // Block the request completely
        REQUIRE_MFA,    // Require multi-factor authentication
        WARN,           // Allow but log warning
        NOTIFY,         // Allow but send notification
        MONITOR         // Allow but increase monitoring
    }
}