package com.gradientgeeks.aegis.sfe.service;

import com.gradientgeeks.aegis.sfe.dto.SignatureValidationRequest;
import com.gradientgeeks.aegis.sfe.entity.Policy;
import com.gradientgeeks.aegis.sfe.entity.PolicyRule;
import com.gradientgeeks.aegis.sfe.entity.PolicyViolation;
import com.gradientgeeks.aegis.sfe.repository.PolicyRepository;
import com.gradientgeeks.aegis.sfe.repository.PolicyViolationRepository;
import com.gradientgeeks.aegis.sfe.repository.RegistrationKeyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Service for enforcing policy violations and logging violations.
 * Handles the actions taken when policies are violated.
 */
@Service
public class PolicyEnforcementService {
    
    private static final Logger logger = LoggerFactory.getLogger(PolicyEnforcementService.class);
    
    @Autowired
    private PolicyViolationRepository policyViolationRepository;
    
    @Autowired
    private RegistrationKeyRepository registrationKeyRepository;
    
    @Autowired
    private PolicyRepository policyRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Enforces policy violation and logs the incident
     */
    @Transactional
    public PolicyEnforcementResult enforceViolation(PolicyValidationService.PolicyValidationResult validationResult,
                                                   SignatureValidationRequest request) {
        try {
            if (validationResult.isAllowed()) {
                return PolicyEnforcementResult.allowed("Request allowed");
            }
            
            // Get organization from clientId
            String organization = getOrganizationFromClientId(request.getClientId());
            if (organization == null) {
                logger.error("Cannot enforce policy - no organization found for clientId: {}", request.getClientId());
                return PolicyEnforcementResult.error("Organization not found");
            }
            
            // Log the violation
            PolicyViolation violation = logViolation(validationResult, request, organization);
            
            // Apply enforcement based on level
            Policy.EnforcementLevel enforcementLevel = validationResult.getEnforcementLevel();
            PolicyEnforcementResult result = applyEnforcement(enforcementLevel, validationResult.getMessage(), violation);
            
            logger.info("Policy enforcement applied: {} for device: {} in organization: {}", 
                       enforcementLevel, request.getDeviceId(), organization);
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error enforcing policy violation for device: {}", request.getDeviceId(), e);
            return PolicyEnforcementResult.error("Policy enforcement error: " + e.getMessage());
        }
    }
    
    /**
     * Logs policy violation to database
     */
    private PolicyViolation logViolation(PolicyValidationService.PolicyValidationResult validationResult,
                                       SignatureValidationRequest request, String organization) {
        try {
            PolicyViolation violation = new PolicyViolation();
            violation.setDeviceId(request.getDeviceId());
            violation.setOrganization(organization);
            
            // Ensure policy is attached to persistence context
            Policy violatedPolicy = validationResult.getViolatedPolicy();
            if (violatedPolicy != null && violatedPolicy.getId() != null) {
                // Re-attach the policy to the current persistence context
                violatedPolicy = policyRepository.findById(violatedPolicy.getId()).orElse(null);
            }
            
            if (violatedPolicy == null) {
                logger.error("Cannot log violation - violated policy is null or not found");
                throw new RuntimeException("Violated policy not found");
            }
            
            violation.setPolicy(violatedPolicy);
            violation.setViolatedRule(validationResult.getViolatedRule());
            violation.setActionTaken(validationResult.getEnforcementLevel());
            violation.setIpAddress(request.getIpAddress());
            violation.setUserAgent(request.getUserAgent());
            violation.setClientId(request.getClientId());
            violation.setViolationDetails(validationResult.getMessage());
            
            // Extract anonymized user ID from metadata
            if (request.getUserMetadata() != null) {
                Object userId = request.getUserMetadata().get("anonymizedUserId");
                if (userId != null) {
                    violation.setAnonymizedUserId(String.valueOf(userId));
                }
            }
            
            // Store request details (without sensitive data)
            violation.setRequestDetails(createRequestSummary(request));
            
            // Calculate severity and risk scores
            violation.setSeverityScore(calculateSeverityScore(validationResult));
            violation.setRiskScore(calculateRiskScore(validationResult, request));
            
            return policyViolationRepository.save(violation);
            
        } catch (Exception e) {
            logger.error("Error logging policy violation", e);
            throw new RuntimeException("Failed to log policy violation", e);
        }
    }
    
    /**
     * Applies enforcement action based on level
     */
    private PolicyEnforcementResult applyEnforcement(Policy.EnforcementLevel enforcementLevel, 
                                                    String message, PolicyViolation violation) {
        switch (enforcementLevel) {
            case BLOCK:
                return PolicyEnforcementResult.blocked(message, violation);
                
            case REQUIRE_MFA:
                return PolicyEnforcementResult.requireMfa(message, violation);
                
            case WARN:
                return PolicyEnforcementResult.warning(message, violation);
                
            case NOTIFY:
                return PolicyEnforcementResult.notification(message, violation);
                
            case MONITOR:
                return PolicyEnforcementResult.monitor(message, violation);
                
            default:
                logger.warn("Unknown enforcement level: {}", enforcementLevel);
                return PolicyEnforcementResult.blocked("Unknown enforcement level", violation);
        }
    }
    
    /**
     * Creates a summary of the request without sensitive data
     */
    private String createRequestSummary(SignatureValidationRequest request) {
        try {
            Map<String, Object> summary = Map.of(
                "deviceId", request.getDeviceId(),
                "clientId", request.getClientId(),
                "timestamp", LocalDateTime.now().toString(),
                "hasUserMetadata", request.getUserMetadata() != null,
                "metadataKeys", request.getUserMetadata() != null ? 
                    request.getUserMetadata().keySet() : "none"
            );
            return objectMapper.writeValueAsString(summary);
        } catch (Exception e) {
            logger.error("Error creating request summary", e);
            return "Error creating summary";
        }
    }
    
    /**
     * Calculates severity score based on policy and rule
     */
    private Integer calculateSeverityScore(PolicyValidationService.PolicyValidationResult validationResult) {
        Policy policy = validationResult.getViolatedPolicy();
        if (policy == null) {
            return 50; // Default severity
        }
        
        // Base score on policy type
        int baseScore = switch (policy.getPolicyType()) {
            case DEVICE_SECURITY -> 90;
            case TRANSACTION_LIMIT -> 80;
            case RISK_ASSESSMENT -> 85;
            case VELOCITY_CHECK -> 75;
            case AUTHENTICATION_REQUIREMENT -> 70;
            case DEVICE_BINDING -> 65;
            case TIME_RESTRICTION -> 60;
            case GEOGRAPHIC_RESTRICTION -> 55;
            case API_RATE_LIMIT -> 50;
            case ACCOUNT_SECURITY -> 80;
        };
        
        // Adjust based on enforcement level
        int enforcementMultiplier = switch (policy.getEnforcementLevel()) {
            case BLOCK -> 100;
            case REQUIRE_MFA -> 80;
            case WARN -> 60;
            case NOTIFY -> 40;
            case MONITOR -> 20;
        };
        
        return (baseScore * enforcementMultiplier) / 100;
    }
    
    /**
     * Calculates risk score based on context
     */
    private Integer calculateRiskScore(PolicyValidationService.PolicyValidationResult validationResult, 
                                     SignatureValidationRequest request) {
        int riskScore = 0;
        
        // Base risk from policy violation
        riskScore += calculateSeverityScore(validationResult);
        
        // Additional risk factors from metadata
        if (request.getUserMetadata() != null) {
            Map<String, Object> metadata = request.getUserMetadata();
            
            // Check risk factors
            Object riskFactors = metadata.get("riskFactors");
            if (riskFactors instanceof Map) {
                Map<?, ?> risks = (Map<?, ?>) riskFactors;
                
                if (Boolean.TRUE.equals(risks.get("isLocationChanged"))) {
                    riskScore += 20;
                }
                if (Boolean.TRUE.equals(risks.get("isDeviceChanged"))) {
                    riskScore += 30;
                }
                if (Boolean.TRUE.equals(risks.get("isDormantAccount"))) {
                    riskScore += 25;
                }
            }
            
            // Check transaction context
            Object transactionContext = metadata.get("transactionContext");
            if (transactionContext instanceof Map) {
                Map<?, ?> transaction = (Map<?, ?>) transactionContext;
                
                if ("HIGH".equals(transaction.get("amountRange"))) {
                    riskScore += 15;
                }
                if ("NEW".equals(transaction.get("beneficiaryType"))) {
                    riskScore += 10;
                }
                if ("NIGHT".equals(transaction.get("timeOfDay"))) {
                    riskScore += 10;
                }
            }
        }
        
        return Math.min(riskScore, 100); // Cap at 100
    }
    
    /**
     * Gets organization from clientId
     */
    private String getOrganizationFromClientId(String clientId) {
        return registrationKeyRepository.findByClientId(clientId)
                .map(registrationKey -> registrationKey.getOrganization())
                .orElse(null);
    }
    
    /**
     * Result class for policy enforcement
     */
    public static class PolicyEnforcementResult {
        private final boolean allowed;
        private final boolean requiresMfa;
        private final String action;
        private final String message;
        private final PolicyViolation violation;
        
        private PolicyEnforcementResult(boolean allowed, boolean requiresMfa, String action, 
                                      String message, PolicyViolation violation) {
            this.allowed = allowed;
            this.requiresMfa = requiresMfa;
            this.action = action;
            this.message = message;
            this.violation = violation;
        }
        
        public static PolicyEnforcementResult allowed(String message) {
            return new PolicyEnforcementResult(true, false, "ALLOW", message, null);
        }
        
        public static PolicyEnforcementResult blocked(String message, PolicyViolation violation) {
            return new PolicyEnforcementResult(false, false, "BLOCK", message, violation);
        }
        
        public static PolicyEnforcementResult requireMfa(String message, PolicyViolation violation) {
            return new PolicyEnforcementResult(false, true, "REQUIRE_MFA", message, violation);
        }
        
        public static PolicyEnforcementResult warning(String message, PolicyViolation violation) {
            return new PolicyEnforcementResult(true, false, "WARN", message, violation);
        }
        
        public static PolicyEnforcementResult notification(String message, PolicyViolation violation) {
            return new PolicyEnforcementResult(true, false, "NOTIFY", message, violation);
        }
        
        public static PolicyEnforcementResult monitor(String message, PolicyViolation violation) {
            return new PolicyEnforcementResult(true, false, "MONITOR", message, violation);
        }
        
        public static PolicyEnforcementResult error(String message) {
            return new PolicyEnforcementResult(false, false, "ERROR", message, null);
        }
        
        // Getters
        public boolean isAllowed() { return allowed; }
        public boolean isRequiresMfa() { return requiresMfa; }
        public String getAction() { return action; }
        public String getMessage() { return message; }
        public PolicyViolation getViolation() { return violation; }
    }
}