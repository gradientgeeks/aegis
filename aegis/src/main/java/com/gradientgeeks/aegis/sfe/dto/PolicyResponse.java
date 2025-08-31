package com.gradientgeeks.aegis.sfe.dto;

import com.gradientgeeks.aegis.sfe.entity.Policy;
import com.gradientgeeks.aegis.sfe.entity.PolicyRule;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for policy responses
 */
public class PolicyResponse {
    
    private Long id;
    private String policyName;
    private String organization;
    private Policy.PolicyType policyType;
    private Policy.EnforcementLevel enforcementLevel;
    private String description;
    private Boolean isActive;
    private Integer priority;
    private List<PolicyRuleResponse> rules;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public PolicyResponse() {}
    
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
    
    public Policy.PolicyType getPolicyType() {
        return policyType;
    }
    
    public void setPolicyType(Policy.PolicyType policyType) {
        this.policyType = policyType;
    }
    
    public Policy.EnforcementLevel getEnforcementLevel() {
        return enforcementLevel;
    }
    
    public void setEnforcementLevel(Policy.EnforcementLevel enforcementLevel) {
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
    
    public List<PolicyRuleResponse> getRules() {
        return rules;
    }
    
    public void setRules(List<PolicyRuleResponse> rules) {
        this.rules = rules;
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
    
    /**
     * DTO for policy rule responses
     */
    public static class PolicyRuleResponse {
        
        private Long id;
        private String ruleName;
        private String conditionField;
        private PolicyRule.RuleOperator operator;
        private String conditionValue;
        private String errorMessage;
        private Integer priority;
        private Boolean isActive;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        // Constructors
        public PolicyRuleResponse() {}
        
        // Getters and Setters
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
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
        
        public PolicyRule.RuleOperator getOperator() {
            return operator;
        }
        
        public void setOperator(PolicyRule.RuleOperator operator) {
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
    }
}