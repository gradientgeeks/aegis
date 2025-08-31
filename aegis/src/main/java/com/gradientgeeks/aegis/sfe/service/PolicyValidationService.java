package com.gradientgeeks.aegis.sfe.service;

import com.gradientgeeks.aegis.sfe.entity.Policy;
import com.gradientgeeks.aegis.sfe.entity.PolicyRule;
import com.gradientgeeks.aegis.sfe.entity.UserDeviceContext;
import com.gradientgeeks.aegis.sfe.repository.PolicyRepository;
import com.gradientgeeks.aegis.sfe.repository.RegistrationKeyRepository;
import com.gradientgeeks.aegis.sfe.repository.UserDeviceContextRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service for validating user metadata against organization policies.
 * Evaluates policy rules and determines enforcement actions.
 */
@Service
public class PolicyValidationService {
    
    private static final Logger logger = LoggerFactory.getLogger(PolicyValidationService.class);
    
    @Autowired
    private PolicyRepository policyRepository;
    
    @Autowired
    private RegistrationKeyRepository registrationKeyRepository;
    
    @Autowired
    private UserDeviceContextRepository userDeviceContextRepository;
    
    /**
     * Validates user metadata against organization policies
     */
    public PolicyValidationResult validatePolicies(String clientId, String deviceId, Map<String, Object> userMetadata) {
        try {
            // Get organization from clientId
            String organization = getOrganizationFromClientId(clientId);
            if (organization == null) {
                logger.warn("No organization found for clientId: {}", clientId);
                return PolicyValidationResult.allowed("No organization found for client");
            }
            
            // Get active policies for organization
            List<Policy> policies = policyRepository.findByOrganizationAndIsActiveTrueOrderByPriorityAsc(organization);
            if (policies.isEmpty()) {
                logger.debug("No active policies found for organization: {}", organization);
                return PolicyValidationResult.allowed("No policies configured");
            }
            
            // Get or create user device context
            UserDeviceContext context = getOrCreateUserDeviceContext(deviceId, organization, clientId, userMetadata);
            
            // Validate each policy
            for (Policy policy : policies) {
                PolicyValidationResult result = validatePolicy(policy, userMetadata, context);
                if (!result.isAllowed()) {
                    logger.info("Policy violation detected: {} for device: {}", 
                               policy.getPolicyName(), deviceId);
                    return result;
                }
            }
            
            // Update user device context with successful validation
            updateUserDeviceContext(context, userMetadata);
            
            return PolicyValidationResult.allowed("All policies satisfied");
            
        } catch (Exception e) {
            logger.error("Error validating policies for device: {}", deviceId, e);
            return PolicyValidationResult.error("Policy validation error: " + e.getMessage());
        }
    }
    
    /**
     * Validates a single policy against user metadata
     */
    private PolicyValidationResult validatePolicy(Policy policy, Map<String, Object> userMetadata, UserDeviceContext context) {
        List<PolicyRule> rules = policy.getRules().stream()
                .filter(rule -> rule.getIsActive())
                .sorted(Comparator.comparing(PolicyRule::getPriority))
                .collect(Collectors.toList());
        
        for (PolicyRule rule : rules) {
            if (!evaluateRule(rule, userMetadata, context)) {
                return PolicyValidationResult.violation(
                    policy, 
                    rule, 
                    rule.getErrorMessage() != null ? rule.getErrorMessage() : 
                        "Policy violation: " + rule.getRuleName()
                );
            }
        }
        
        return PolicyValidationResult.allowed("Policy satisfied: " + policy.getPolicyName());
    }
    
    /**
     * Evaluates a single rule against user metadata and context
     */
    private boolean evaluateRule(PolicyRule rule, Map<String, Object> userMetadata, UserDeviceContext context) {
        String field = rule.getConditionField();
        String expectedValue = rule.getConditionValue();
        PolicyRule.RuleOperator operator = rule.getOperator();
        
        // Get the actual value from metadata or context
        Object actualValue = getFieldValue(field, userMetadata, context);
        
        if (actualValue == null && !isNullCheckOperator(operator)) {
            logger.debug("Field {} not found in metadata or context", field);
            return true; // Rule doesn't apply if field is missing
        }
        
        return evaluateCondition(actualValue, operator, expectedValue);
    }
    
    /**
     * Gets field value from user metadata or device context
     */
    private Object getFieldValue(String field, Map<String, Object> userMetadata, UserDeviceContext context) {
        // First check user metadata
        Object value = getNestedValue(userMetadata, field);
        if (value != null) {
            return value;
        }
        
        // Then check device context and computed values
        switch (field.toLowerCase()) {
            case "accounttier":
            case "account_tier":
                return context.getAccountTier();
            case "accountage":
            case "account_age_months":
                return context.getAccountAgeMonths();
            case "kyc_level":
            case "kyc":
                return context.getKycLevel();
            case "daily_transaction_count":
            case "dailytransactioncount":
                return context.getDailyTransactionCount();
            case "weekly_transaction_count":
            case "weeklytransactioncount":
                return context.getWeeklyTransactionCount();
            case "monthly_transaction_count":
            case "monthlytransactioncount":
                return context.getMonthlyTransactionCount();
            case "risk_score":
            case "riskscore":
                return context.getRiskScore();
            case "failed_attempts_count":
            case "failedattemptscount":
                return context.getFailedAttemptsCount();
            case "is_device_changed":
            case "devicechanged":
                return context.getIsDeviceChanged();
            case "is_location_changed":
            case "locationchanged":
                return context.getIsLocationChanged();
            case "is_dormant_account":
            case "dormantaccount":
                return context.getIsDormantAccount();
            case "time_of_day":
            case "timeofday":
                return getCurrentTimeOfDay();
            
            // Transaction limit fields - dynamically computed
            case "userlimits.dailytransactionamount":
                return getDailyTransactionAmount(context);
            case "userlimits.weeklytransactionamount":
                return getWeeklyTransactionAmount(context);
            case "userlimits.monthlytransactionamount":
                return getMonthlyTransactionAmount(context);
            
            // Account-based limits
            case "accountlimits.maxdailyamount":
                return getMaxDailyAmountForAccountTier(context.getAccountTier());
            case "accountlimits.maxsingletransactionamount":
                return getMaxSingleTransactionForAccountTier(context.getAccountTier());
            case "accountlimits.maxmonthlyamount":
                return getMaxMonthlyAmountForAccountTier(context.getAccountTier());
                
            default:
                return null;
        }
    }
    
    /**
     * Gets nested value from metadata map using dot notation
     */
    private Object getNestedValue(Map<String, Object> map, String field) {
        if (map == null || field == null) {
            return null;
        }
        
        String[] parts = field.split("\\.");
        Object current = map;
        
        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
            } else {
                return null;
            }
        }
        
        return current;
    }
    
    /**
     * Evaluates condition based on operator
     */
    private boolean evaluateCondition(Object actualValue, PolicyRule.RuleOperator operator, String expectedValue) {
        try {
            switch (operator) {
                case EQUALS:
                    return Objects.equals(String.valueOf(actualValue), expectedValue);
                    
                case NOT_EQUALS:
                    return !Objects.equals(String.valueOf(actualValue), expectedValue);
                    
                case GREATER_THAN:
                    return compareNumbers(actualValue, expectedValue) > 0;
                    
                case LESS_THAN:
                    return compareNumbers(actualValue, expectedValue) < 0;
                    
                case GREATER_THAN_OR_EQUALS:
                    return compareNumbers(actualValue, expectedValue) >= 0;
                    
                case LESS_THAN_OR_EQUALS:
                    return compareNumbers(actualValue, expectedValue) <= 0;
                    
                case CONTAINS:
                    return String.valueOf(actualValue).contains(expectedValue);
                    
                case NOT_CONTAINS:
                    return !String.valueOf(actualValue).contains(expectedValue);
                    
                case STARTS_WITH:
                    return String.valueOf(actualValue).startsWith(expectedValue);
                    
                case ENDS_WITH:
                    return String.valueOf(actualValue).endsWith(expectedValue);
                    
                case IN:
                    return Arrays.asList(expectedValue.split(","))
                            .contains(String.valueOf(actualValue).trim());
                    
                case NOT_IN:
                    return !Arrays.asList(expectedValue.split(","))
                            .contains(String.valueOf(actualValue).trim());
                    
                case REGEX_MATCH:
                    return Pattern.matches(expectedValue, String.valueOf(actualValue));
                    
                case BETWEEN:
                    return evaluateBetween(actualValue, expectedValue);
                    
                case IS_NULL:
                    return actualValue == null;
                    
                case IS_NOT_NULL:
                    return actualValue != null;
                    
                default:
                    logger.warn("Unknown operator: {}", operator);
                    return false;
            }
        } catch (Exception e) {
            logger.error("Error evaluating condition: {} {} {}", actualValue, operator, expectedValue, e);
            return false;
        }
    }
    
    /**
     * Compares two values as numbers
     */
    private int compareNumbers(Object actualValue, String expectedValue) {
        try {
            double actual = Double.parseDouble(String.valueOf(actualValue));
            double expected = Double.parseDouble(expectedValue);
            return Double.compare(actual, expected);
        } catch (NumberFormatException e) {
            // Fall back to string comparison
            return String.valueOf(actualValue).compareTo(expectedValue);
        }
    }
    
    /**
     * Evaluates BETWEEN operator (expected format: "min,max")
     */
    private boolean evaluateBetween(Object actualValue, String expectedValue) {
        String[] parts = expectedValue.split(",");
        if (parts.length != 2) {
            return false;
        }
        
        try {
            double actual = Double.parseDouble(String.valueOf(actualValue));
            double min = Double.parseDouble(parts[0].trim());
            double max = Double.parseDouble(parts[1].trim());
            return actual >= min && actual <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Checks if operator is for null checking
     */
    private boolean isNullCheckOperator(PolicyRule.RuleOperator operator) {
        return operator == PolicyRule.RuleOperator.IS_NULL || 
               operator == PolicyRule.RuleOperator.IS_NOT_NULL;
    }
    
    /**
     * Gets current time of day category
     */
    private String getCurrentTimeOfDay() {
        LocalTime now = LocalTime.now();
        
        if (now.isAfter(LocalTime.of(6, 0)) && now.isBefore(LocalTime.of(18, 0))) {
            return "BUSINESS_HOURS";
        } else if (now.isAfter(LocalTime.of(18, 0)) && now.isBefore(LocalTime.of(22, 0))) {
            return "AFTER_HOURS";
        } else {
            return "NIGHT";
        }
    }
    
    /**
     * Gets organization from clientId using RegistrationKey
     */
    private String getOrganizationFromClientId(String clientId) {
        return registrationKeyRepository.findByClientId(clientId)
                .map(registrationKey -> registrationKey.getOrganization())
                .orElse(null);
    }
    
    /**
     * Gets or creates user device context
     */
    private UserDeviceContext getOrCreateUserDeviceContext(String deviceId, String organization, 
                                                           String clientId, Map<String, Object> userMetadata) {
        String anonymizedUserId = extractAnonymizedUserId(userMetadata);
        
        Optional<UserDeviceContext> existing = userDeviceContextRepository
                .findByAnonymizedUserIdAndDeviceIdAndOrganization(anonymizedUserId, deviceId, organization);
        
        if (existing.isPresent()) {
            return existing.get();
        }
        
        // Create new context
        UserDeviceContext context = new UserDeviceContext(anonymizedUserId, deviceId, organization, clientId);
        updateContextFromMetadata(context, userMetadata);
        return userDeviceContextRepository.save(context);
    }
    
    /**
     * Updates user device context with metadata
     */
    private void updateUserDeviceContext(UserDeviceContext context, Map<String, Object> userMetadata) {
        updateContextFromMetadata(context, userMetadata);
        context.setLastActivityAt(LocalDateTime.now());
        context.incrementTotalSessions();
        userDeviceContextRepository.save(context);
    }
    
    /**
     * Updates context fields from user metadata
     */
    private void updateContextFromMetadata(UserDeviceContext context, Map<String, Object> userMetadata) {
        Object sessionContext = userMetadata.get("sessionContext");
        if (sessionContext instanceof Map) {
            Map<?, ?> session = (Map<?, ?>) sessionContext;
            updateFromMap(context, session, "accountTier", value -> context.setAccountTier(String.valueOf(value)));
            updateFromMap(context, session, "accountAge", value -> {
                if (value instanceof Number) {
                    context.setAccountAgeMonths(((Number) value).intValue());
                }
            });
            updateFromMap(context, session, "kycLevel", value -> context.setKycLevel(String.valueOf(value)));
        }
        
        Object transactionContext = userMetadata.get("transactionContext");
        if (transactionContext instanceof Map) {
            Map<?, ?> transaction = (Map<?, ?>) transactionContext;
            // Update transaction counts would happen here based on transaction type
        }
        
        Object riskFactors = userMetadata.get("riskFactors");
        if (riskFactors instanceof Map) {
            Map<?, ?> risk = (Map<?, ?>) riskFactors;
            updateFromMap(context, risk, "isLocationChanged", value -> {
                if (value instanceof Boolean) {
                    context.setIsLocationChanged((Boolean) value);
                }
            });
            updateFromMap(context, risk, "isDeviceChanged", value -> {
                if (value instanceof Boolean) {
                    context.setIsDeviceChanged((Boolean) value);
                }
            });
            updateFromMap(context, risk, "isDormantAccount", value -> {
                if (value instanceof Boolean) {
                    context.setIsDormantAccount((Boolean) value);
                }
            });
        }
    }
    
    /**
     * Helper method to update context from map
     */
    private void updateFromMap(UserDeviceContext context, Map<?, ?> map, String key, java.util.function.Consumer<Object> setter) {
        Object value = map.get(key);
        if (value != null) {
            setter.accept(value);
        }
    }
    
    /**
     * Extracts anonymized user ID from metadata
     */
    private String extractAnonymizedUserId(Map<String, Object> userMetadata) {
        Object userId = userMetadata.get("anonymizedUserId");
        return userId != null ? String.valueOf(userId) : "unknown";
    }
    
    /**
     * Gets daily transaction amount for user
     */
    private Double getDailyTransactionAmount(UserDeviceContext context) {
        // This would typically query transaction records for today
        // For now, return a placeholder value or calculate from context
        return context.getDailyTransactionAmount() != null ? context.getDailyTransactionAmount() : 0.0;
    }
    
    /**
     * Gets weekly transaction amount for user
     */
    private Double getWeeklyTransactionAmount(UserDeviceContext context) {
        // This would typically query transaction records for this week
        return context.getWeeklyTransactionAmount() != null ? context.getWeeklyTransactionAmount() : 0.0;
    }
    
    /**
     * Gets monthly transaction amount for user
     */
    private Double getMonthlyTransactionAmount(UserDeviceContext context) {
        // This would typically query transaction records for this month
        return context.getMonthlyTransactionAmount() != null ? context.getMonthlyTransactionAmount() : 0.0;
    }
    
    /**
     * Gets maximum daily amount limit based on account tier
     */
    private Double getMaxDailyAmountForAccountTier(String accountTier) {
        if (accountTier == null) return 50000.0; // Default limit
        
        switch (accountTier.toUpperCase()) {
            case "SAVINGS":
                return 50000.0;   // ₹50,000
            case "CURRENT":
                return 100000.0;  // ₹1,00,000
            case "FIXED_DEPOSIT":
            case "RECURRING_DEPOSIT":
                return 25000.0;   // ₹25,000
            case "CORPORATE":
                return 1000000.0; // ₹10,00,000
            case "PREMIUM_SAVINGS":
            case "PREMIUM_CHECKING":
                return 200000.0;  // ₹2,00,000
            default:
                return 50000.0;
        }
    }
    
    /**
     * Gets maximum single transaction amount based on account tier
     */
    private Double getMaxSingleTransactionForAccountTier(String accountTier) {
        if (accountTier == null) return 25000.0; // Default limit
        
        switch (accountTier.toUpperCase()) {
            case "SAVINGS":
                return 25000.0;   // ₹25,000
            case "CURRENT":
                return 50000.0;   // ₹50,000
            case "FIXED_DEPOSIT":
            case "RECURRING_DEPOSIT":
                return 10000.0;   // ₹10,000
            case "CORPORATE":
                return 500000.0;  // ₹5,00,000
            case "PREMIUM_SAVINGS":
            case "PREMIUM_CHECKING":
                return 100000.0;  // ₹1,00,000
            default:
                return 25000.0;
        }
    }
    
    /**
     * Gets maximum monthly amount limit based on account tier
     */
    private Double getMaxMonthlyAmountForAccountTier(String accountTier) {
        if (accountTier == null) return 1000000.0; // Default limit
        
        switch (accountTier.toUpperCase()) {
            case "SAVINGS":
                return 1000000.0;   // ₹10,00,000
            case "CURRENT":
                return 2000000.0;   // ₹20,00,000
            case "FIXED_DEPOSIT":
            case "RECURRING_DEPOSIT":
                return 500000.0;    // ₹5,00,000
            case "CORPORATE":
                return 25000000.0;  // ₹2,50,00,000
            case "PREMIUM_SAVINGS":
            case "PREMIUM_CHECKING":
                return 5000000.0;   // ₹50,00,000
            default:
                return 1000000.0;
        }
    }
    
    /**
     * Result class for policy validation
     */
    public static class PolicyValidationResult {
        private final boolean allowed;
        private final Policy.EnforcementLevel enforcementLevel;
        private final String message;
        private final Policy violatedPolicy;
        private final PolicyRule violatedRule;
        
        private PolicyValidationResult(boolean allowed, Policy.EnforcementLevel enforcementLevel, 
                                     String message, Policy violatedPolicy, PolicyRule violatedRule) {
            this.allowed = allowed;
            this.enforcementLevel = enforcementLevel;
            this.message = message;
            this.violatedPolicy = violatedPolicy;
            this.violatedRule = violatedRule;
        }
        
        public static PolicyValidationResult allowed(String message) {
            return new PolicyValidationResult(true, null, message, null, null);
        }
        
        public static PolicyValidationResult violation(Policy policy, PolicyRule rule, String message) {
            return new PolicyValidationResult(false, policy.getEnforcementLevel(), message, policy, rule);
        }
        
        public static PolicyValidationResult error(String message) {
            return new PolicyValidationResult(false, Policy.EnforcementLevel.BLOCK, message, null, null);
        }
        
        // Getters
        public boolean isAllowed() { return allowed; }
        public Policy.EnforcementLevel getEnforcementLevel() { return enforcementLevel; }
        public String getMessage() { return message; }
        public Policy getViolatedPolicy() { return violatedPolicy; }
        public PolicyRule getViolatedRule() { return violatedRule; }
    }
}