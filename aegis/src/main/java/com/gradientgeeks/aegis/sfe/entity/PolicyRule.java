package com.gradientgeeks.aegis.sfe.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing a specific rule within a policy.
 * Rules define conditions and actions for policy enforcement.
 */
@Entity
@Table(name = "policy_rules", indexes = {
    @Index(name = "idx_policy_id", columnList = "policy_id"),
    @Index(name = "idx_rule_priority", columnList = "priority"),
    @Index(name = "idx_is_active", columnList = "isActive")
})
public class PolicyRule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "rule_name", nullable = false)
    private String ruleName;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "condition_field", nullable = false)
    private String conditionField;
    
    @NotNull
    @Column(name = "operator", nullable = false)
    @Enumerated(EnumType.STRING)
    private RuleOperator operator;
    
    @NotBlank
    @Size(max = 500)
    @Column(name = "condition_value", nullable = false)
    private String conditionValue;
    
    @Size(max = 200)
    @Column(name = "error_message")
    private String errorMessage;
    
    @Column(name = "priority", nullable = false)
    private Integer priority = 100;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public PolicyRule() {}
    
    public PolicyRule(String ruleName, String conditionField, RuleOperator operator, String conditionValue) {
        this.ruleName = ruleName;
        this.conditionField = conditionField;
        this.operator = operator;
        this.conditionValue = conditionValue;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Policy getPolicy() {
        return policy;
    }
    
    public void setPolicy(Policy policy) {
        this.policy = policy;
    }
    
    public String getRuleName() {
        return ruleName;
    }
    
    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }
    
    public String getConditionField() {
        return conditionField;
    }
    
    public void setConditionField(String conditionField) {
        this.conditionField = conditionField;
    }
    
    public RuleOperator getOperator() {
        return operator;
    }
    
    public void setOperator(RuleOperator operator) {
        this.operator = operator;
    }
    
    public String getConditionValue() {
        return conditionValue;
    }
    
    public void setConditionValue(String conditionValue) {
        this.conditionValue = conditionValue;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public Integer getPriority() {
        return priority;
    }
    
    public void setPriority(Integer priority) {
        this.priority = priority;
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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PolicyRule that = (PolicyRule) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "PolicyRule{" +
                "id=" + id +
                ", ruleName='" + ruleName + '\'' +
                ", conditionField='" + conditionField + '\'' +
                ", operator=" + operator +
                ", conditionValue='" + conditionValue + '\'' +
                ", priority=" + priority +
                ", isActive=" + isActive +
                '}';
    }
    
    /**
     * Operators for rule conditions
     */
    public enum RuleOperator {
        EQUALS,
        NOT_EQUALS,
        GREATER_THAN,
        LESS_THAN,
        GREATER_THAN_OR_EQUALS,
        LESS_THAN_OR_EQUALS,
        CONTAINS,
        NOT_CONTAINS,
        STARTS_WITH,
        ENDS_WITH,
        IN,
        NOT_IN,
        REGEX_MATCH,
        BETWEEN,
        IS_NULL,
        IS_NOT_NULL
    }
}