package com.gradientgeeks.aegis.sfe.dto;

import com.gradientgeeks.aegis.sfe.entity.Policy;
import com.gradientgeeks.aegis.sfe.entity.PolicyRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO for creating and updating policies
 */
public class PolicyRequest {
    
    @NotBlank(message = "Policy name is required")
    @Size(max = 100, message = "Policy name must not exceed 100 characters")
    private String policyName;
    
    @NotNull(message = "Policy type is required")
    private Policy.PolicyType policyType;
    
    @NotNull(message = "Enforcement level is required")
    private Policy.EnforcementLevel enforcementLevel;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    private Boolean isActive = true;
    
    private Integer priority = 100;
    
    @Valid
    private List<PolicyRuleRequest> rules;
    
    // Constructors
    public PolicyRequest() {}
    
    // Getters and Setters
    public String getPolicyName() {
        return policyName;
    }
    
    public void setPolicyName(String policyName) {
        this.policyName = policyName;
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
    
    public List<PolicyRuleRequest> getRules() {
        return rules;
    }
    
    public void setRules(List<PolicyRuleRequest> rules) {
        this.rules = rules;
    }
    
    /**
     * DTO for policy rules within a policy request
     */
    public static class PolicyRuleRequest {
        
        @NotBlank(message = "Rule name is required")
        @Size(max = 100, message = "Rule name must not exceed 100 characters")
        private String ruleName;
        
        @NotBlank(message = "Condition field is required")
        @Size(max = 100, message = "Condition field must not exceed 100 characters")
        private String conditionField;
        
        @NotNull(message = "Operator is required")
        private PolicyRule.RuleOperator operator;
        
        @NotBlank(message = "Condition value is required")
        @Size(max = 500, message = "Condition value must not exceed 500 characters")
        private String conditionValue;
        
        @Size(max = 200, message = "Error message must not exceed 200 characters")
        private String errorMessage;
        
        private Integer priority = 100;
        
        private Boolean isActive = true;
        
        // Constructors
        public PolicyRuleRequest() {}
        
        // Getters and Setters
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
    }
}