package com.gradientgeeks.aegis.sfe.service;

import com.gradientgeeks.aegis.sfe.dto.PolicyRequest;
import com.gradientgeeks.aegis.sfe.entity.Policy;
import com.gradientgeeks.aegis.sfe.entity.PolicyRule;
import com.gradientgeeks.aegis.sfe.entity.PolicyViolation;
import com.gradientgeeks.aegis.sfe.repository.PolicyRepository;
import com.gradientgeeks.aegis.sfe.repository.PolicyViolationRepository;
import com.gradientgeeks.aegis.sfe.repository.RegistrationKeyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing policies and policy-related operations.
 */
@Service
@Transactional
public class PolicyManagementService {
    
    private static final Logger logger = LoggerFactory.getLogger(PolicyManagementService.class);
    
    @Autowired
    private PolicyRepository policyRepository;
    
    @Autowired
    private PolicyViolationRepository policyViolationRepository;
    
    @Autowired
    private RegistrationKeyRepository registrationKeyRepository;
    
    /**
     * Creates a new policy for an organization
     */
    public Policy createPolicy(PolicyRequest policyRequest, String userOrganization) {
        if (userOrganization == null || userOrganization.trim().isEmpty()) {
            throw new IllegalArgumentException("User organization is required");
        }
        
        // Check if policy name already exists for this organization
        if (policyRepository.existsByPolicyNameAndOrganization(policyRequest.getPolicyName(), userOrganization)) {
            throw new IllegalArgumentException("Policy name already exists for this organization");
        }
        
        Policy policy = new Policy();
        policy.setPolicyName(policyRequest.getPolicyName());
        policy.setOrganization(userOrganization);
        policy.setPolicyType(policyRequest.getPolicyType());
        policy.setEnforcementLevel(policyRequest.getEnforcementLevel());
        policy.setDescription(policyRequest.getDescription());
        policy.setIsActive(policyRequest.getIsActive() != null ? policyRequest.getIsActive() : true);
        policy.setPriority(policyRequest.getPriority() != null ? policyRequest.getPriority() : 100);
        
        // Convert and add rules
        if (policyRequest.getRules() != null) {
            List<PolicyRule> rules = policyRequest.getRules().stream()
                    .map(ruleRequest -> convertToRule(ruleRequest, policy))
                    .collect(Collectors.toList());
            policy.setRules(rules);
        }
        
        Policy savedPolicy = policyRepository.save(policy);
        logger.info("Created policy: {} for organization: {}", savedPolicy.getPolicyName(), userOrganization);
        
        return savedPolicy;
    }
    
    /**
     * Updates an existing policy
     */
    public Policy updatePolicy(Long policyId, PolicyRequest policyRequest, String userOrganization) {
        Policy existingPolicy = getPolicyById(policyId, userOrganization);
        
        // Check if new policy name conflicts with existing ones (excluding current)
        if (!existingPolicy.getPolicyName().equals(policyRequest.getPolicyName()) &&
            policyRepository.existsByPolicyNameAndOrganization(policyRequest.getPolicyName(), userOrganization)) {
            throw new IllegalArgumentException("Policy name already exists for this organization");
        }
        
        existingPolicy.setPolicyName(policyRequest.getPolicyName());
        existingPolicy.setPolicyType(policyRequest.getPolicyType());
        existingPolicy.setEnforcementLevel(policyRequest.getEnforcementLevel());
        existingPolicy.setDescription(policyRequest.getDescription());
        existingPolicy.setIsActive(policyRequest.getIsActive() != null ? policyRequest.getIsActive() : true);
        existingPolicy.setPriority(policyRequest.getPriority() != null ? policyRequest.getPriority() : 100);
        
        // Update rules
        if (policyRequest.getRules() != null) {
            // Clear existing rules
            existingPolicy.getRules().clear();
            
            // Add new rules directly to the existing collection
            policyRequest.getRules().forEach(ruleRequest -> {
                PolicyRule newRule = convertToRule(ruleRequest, existingPolicy);
                existingPolicy.getRules().add(newRule);
            });
        }
        
        Policy savedPolicy = policyRepository.save(existingPolicy);
        logger.info("Updated policy: {} for organization: {}", savedPolicy.getPolicyName(), userOrganization);
        
        return savedPolicy;
    }
    
    /**
     * Gets all policies for an organization by clientId
     */
    public List<Policy> getPoliciesByClientId(String clientId) {
        String organization = getOrganizationFromClientId(clientId);
        if (organization == null) {
            throw new IllegalArgumentException("No organization found for clientId: " + clientId);
        }
        
        return policyRepository.findByOrganizationOrderByCreatedAtDesc(organization);
    }
    
    /**
     * Gets all policies for an organization
     */
    public List<Policy> getPoliciesByOrganization(String organization) {
        if (organization == null || organization.trim().isEmpty()) {
            throw new IllegalArgumentException("Organization is required");
        }
        
        return policyRepository.findByOrganizationOrderByCreatedAtDesc(organization);
    }
    
    /**
     * Gets a specific policy by ID, ensuring it belongs to the user's organization
     */
    public Policy getPolicyById(Long policyId, String userOrganization) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found"));
        
        if (!policy.getOrganization().equals(userOrganization)) {
            throw new IllegalArgumentException("Policy does not belong to your organization");
        }
        
        return policy;
    }
    
    /**
     * Deletes a policy
     */
    public void deletePolicy(Long policyId, String userOrganization) {
        Policy policy = getPolicyById(policyId, userOrganization);
        policyRepository.delete(policy);
        logger.info("Deleted policy: {} for organization: {}", policy.getPolicyName(), userOrganization);
    }
    
    /**
     * Updates policy status (active/inactive)
     */
    public Policy updatePolicyStatus(Long policyId, boolean active, String userOrganization) {
        Policy policy = getPolicyById(policyId, userOrganization);
        policy.setIsActive(active);
        
        Policy savedPolicy = policyRepository.save(policy);
        logger.info("Updated policy status: {} to {} for organization: {}", 
                   savedPolicy.getPolicyName(), active, userOrganization);
        
        return savedPolicy;
    }
    
    /**
     * Gets violation history for a device
     */
    public Page<PolicyViolation> getViolationHistory(String deviceId, LocalDateTime from, LocalDateTime to,
                                                    String userOrganization, Pageable pageable) {
        if (userOrganization == null || userOrganization.trim().isEmpty()) {
            throw new IllegalArgumentException("User organization is required");
        }
        
        return policyViolationRepository.findByOrganizationAndDeviceIdOrderByCreatedAtDesc(
                userOrganization, deviceId, pageable);
    }
    
    /**
     * Gets violation statistics for an organization
     */
    public Map<String, Object> getViolationStatistics(LocalDateTime from, LocalDateTime to, String userOrganization) {
        if (userOrganization == null || userOrganization.trim().isEmpty()) {
            throw new IllegalArgumentException("User organization is required");
        }
        
        // Get total violations count
        long totalViolations = policyViolationRepository.countByOrganizationAndDateRange(
                userOrganization, from, to);
        
        // Get violations by action taken
        List<Object[]> violationsByAction = policyViolationRepository.getViolationStatsByActionTaken(
                userOrganization, from, to);
        
        Map<String, Long> actionStats = violationsByAction.stream()
                .collect(Collectors.toMap(
                        result -> result[0].toString(),
                        result -> (Long) result[1]
                ));
        
        // Get violations by policy type
        List<Object[]> violationsByType = policyViolationRepository.getViolationsByPolicyType(
                userOrganization, from, to);
        
        Map<String, Long> typeStats = violationsByType.stream()
                .collect(Collectors.toMap(
                        result -> result[0].toString(),
                        result -> (Long) result[1]
                ));
        
        // Get daily trends
        List<Object[]> dailyTrends = policyViolationRepository.getViolationTrendsByDay(
                userOrganization, from, to);
        
        Map<String, Long> trendStats = dailyTrends.stream()
                .collect(Collectors.toMap(
                        result -> result[0].toString(),
                        result -> (Long) result[1]
                ));
        
        // Get top violating devices
        List<Object[]> topDevices = policyViolationRepository.getTopViolatingDevices(
                userOrganization, from, to, Pageable.ofSize(10));
        
        Map<String, Long> deviceStats = topDevices.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],
                        result -> (Long) result[1]
                ));
        
        return Map.of(
                "totalViolations", totalViolations,
                "violationsByAction", actionStats,
                "violationsByType", typeStats,
                "dailyTrends", trendStats,
                "topViolatingDevices", deviceStats,
                "fromDate", from,
                "toDate", to,
                "organization", userOrganization
        );
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
     * Converts PolicyRuleRequest to PolicyRule entity
     */
    private PolicyRule convertToRule(PolicyRequest.PolicyRuleRequest ruleRequest, Policy policy) {
        PolicyRule rule = new PolicyRule();
        rule.setPolicy(policy);
        rule.setRuleName(ruleRequest.getRuleName());
        rule.setConditionField(ruleRequest.getConditionField());
        rule.setOperator(ruleRequest.getOperator());
        rule.setConditionValue(ruleRequest.getConditionValue());
        rule.setErrorMessage(ruleRequest.getErrorMessage());
        rule.setPriority(ruleRequest.getPriority() != null ? ruleRequest.getPriority() : 100);
        rule.setIsActive(ruleRequest.getIsActive() != null ? ruleRequest.getIsActive() : true);
        
        return rule;
    }
}