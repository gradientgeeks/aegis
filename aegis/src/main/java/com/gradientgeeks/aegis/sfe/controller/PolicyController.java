package com.gradientgeeks.aegis.sfe.controller;

import com.gradientgeeks.aegis.sfe.dto.PolicyFieldConfigDto;
import com.gradientgeeks.aegis.sfe.dto.PolicyRequest;
import com.gradientgeeks.aegis.sfe.dto.PolicyResponse;
import com.gradientgeeks.aegis.sfe.entity.Policy;
import com.gradientgeeks.aegis.sfe.entity.PolicyViolation;
import com.gradientgeeks.aegis.sfe.service.PolicyFieldConfigService;
import com.gradientgeeks.aegis.sfe.service.PolicyManagementService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST controller for policy management operations.
 * Provides endpoints for creating, updating, and managing organization policies.
 */
@RestController
@RequestMapping("/admin/policies")
@CrossOrigin(origins = "*")
public class PolicyController extends BaseController {
    
    private static final Logger logger = LoggerFactory.getLogger(PolicyController.class);
    
    @Autowired
    private PolicyManagementService policyManagementService;
    
    @Autowired
    private PolicyFieldConfigService policyFieldConfigService;
    
    /**
     * Creates a new policy for an organization
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> createPolicy(@Valid @RequestBody PolicyRequest policyRequest) {
        try {
            String userOrganization = getCurrentUserOrganization();
            logger.info("Creating policy: {} for organization: {}", policyRequest.getPolicyName(), userOrganization);
            
            Policy policy = policyManagementService.createPolicy(policyRequest, userOrganization);
            PolicyResponse response = convertToResponse(policy);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid policy request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating policy", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create policy"));
        }
    }
    
    /**
     * Updates an existing policy
     */
    @PutMapping("/{policyId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> updatePolicy(@PathVariable Long policyId,
                                        @Valid @RequestBody PolicyRequest policyRequest) {
        try {
            String userOrganization = getCurrentUserOrganization();
            logger.info("Updating policy: {} for organization: {}", policyId, userOrganization);
            
            Policy policy = policyManagementService.updatePolicy(policyId, policyRequest, userOrganization);
            PolicyResponse response = convertToResponse(policy);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid policy update request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating policy", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update policy"));
        }
    }
    
    /**
     * Gets all policies for an organization (via clientId)
     */
    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> getPoliciesByClientId(@PathVariable String clientId) {
        try {
            logger.info("Getting policies for clientId: {}", clientId);
            
            List<Policy> policies = policyManagementService.getPoliciesByClientId(clientId);
            List<PolicyResponse> responses = policies.stream()
                    .map(this::convertToResponse)
                    .toList();
            
            return ResponseEntity.ok(responses);
            
        } catch (Exception e) {
            logger.error("Error getting policies for clientId: {}", clientId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get policies"));
        }
    }
    
    /**
     * Gets all policies for current user's organization
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> getPoliciesByOrganization() {
        String userOrganization = null;
        try {
            userOrganization = getCurrentUserOrganization();
            logger.info("Getting policies for organization: {}", userOrganization);
            
            List<Policy> policies = policyManagementService.getPoliciesByOrganization(userOrganization);
            List<PolicyResponse> responses = policies.stream()
                    .map(this::convertToResponse)
                    .toList();
            
            return ResponseEntity.ok(responses);
            
        } catch (Exception e) {
            logger.error("Error getting policies for organization: {}", userOrganization, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get policies"));
        }
    }
    
    /**
     * Gets all available policy fields for rule configuration
     */
    @GetMapping("/fields")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> getPolicyFields() {
        try {
            List<PolicyFieldConfigDto> fields = policyFieldConfigService.getAllPolicyFields();
            return ResponseEntity.ok(fields);
        } catch (Exception e) {
            logger.error("Error getting policy fields", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get policy fields"));
        }
    }
    
    /**
     * Gets all available policy types
     */
    @GetMapping("/types")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> getPolicyTypes() {
        try {
            Policy.PolicyType[] types = Policy.PolicyType.values();
            return ResponseEntity.ok(types);
        } catch (Exception e) {
            logger.error("Error getting policy types", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get policy types"));
        }
    }
    
    /**
     * Gets all available enforcement levels
     */
    @GetMapping("/enforcement-levels")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> getEnforcementLevels() {
        try {
            Policy.EnforcementLevel[] levels = Policy.EnforcementLevel.values();
            return ResponseEntity.ok(levels);
        } catch (Exception e) {
            logger.error("Error getting enforcement levels", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get enforcement levels"));
        }
    }
    
    /**
     * Gets a specific policy by ID
     */
    @GetMapping("/{policyId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> getPolicyById(@PathVariable Long policyId) {
        try {
            String userOrganization = getCurrentUserOrganization();
            Policy policy = policyManagementService.getPolicyById(policyId, userOrganization);
            PolicyResponse response = convertToResponse(policy);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error getting policy: {}", policyId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get policy"));
        }
    }
    
    /**
     * Deletes a policy
     */
    @DeleteMapping("/{policyId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> deletePolicy(@PathVariable Long policyId) {
        try {
            String userOrganization = getCurrentUserOrganization();
            logger.info("Deleting policy: {} for organization: {}", policyId, userOrganization);
            
            policyManagementService.deletePolicy(policyId, userOrganization);
            
            return ResponseEntity.ok(Map.of("message", "Policy deleted successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting policy: {}", policyId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete policy"));
        }
    }
    
    /**
     * Activates or deactivates a policy
     */
    @PutMapping("/{policyId}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> updatePolicyStatus(@PathVariable Long policyId,
                                              @RequestParam boolean active) {
        try {
            String userOrganization = getCurrentUserOrganization();
            logger.info("Updating policy status: {} to {} for organization: {}", policyId, active, userOrganization);
            
            Policy policy = policyManagementService.updatePolicyStatus(policyId, active, userOrganization);
            PolicyResponse response = convertToResponse(policy);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error updating policy status: {}", policyId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update policy status"));
        }
    }
    
    /**
     * Gets policy violations for a device
     */
    @GetMapping("/violations/{deviceId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> getViolationHistory(@PathVariable String deviceId,
                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
                                               Pageable pageable) {
        try {
            String userOrganization = getCurrentUserOrganization();
            logger.info("Getting violation history for device: {} from: {} to: {}", deviceId, from, to);
            
            Page<PolicyViolation> violations = policyManagementService.getViolationHistory(
                    deviceId, from, to, userOrganization, pageable);
            
            return ResponseEntity.ok(violations);
            
        } catch (Exception e) {
            logger.error("Error getting violation history for device: {}", deviceId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get violation history"));
        }
    }
    
    /**
     * Gets violation statistics for organization
     */
    @GetMapping("/violations/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> getViolationStatistics(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        try {
            String userOrganization = getCurrentUserOrganization();
            logger.info("Getting violation statistics for organization: {} from: {} to: {}", userOrganization, from, to);
            
            Map<String, Object> statistics = policyManagementService.getViolationStatistics(from, to, userOrganization);
            
            return ResponseEntity.ok(statistics);
            
        } catch (Exception e) {
            logger.error("Error getting violation statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get violation statistics"));
        }
    }
    
    /**
     * Converts Policy entity to response DTO
     */
    private PolicyResponse convertToResponse(Policy policy) {
        PolicyResponse response = new PolicyResponse();
        response.setId(policy.getId());
        response.setPolicyName(policy.getPolicyName());
        response.setOrganization(policy.getOrganization());
        response.setPolicyType(policy.getPolicyType());
        response.setEnforcementLevel(policy.getEnforcementLevel());
        response.setDescription(policy.getDescription());
        response.setIsActive(policy.getIsActive());
        response.setPriority(policy.getPriority());
        response.setCreatedAt(policy.getCreatedAt());
        response.setUpdatedAt(policy.getUpdatedAt());
        
        // Convert rules if present
        if (policy.getRules() != null) {
            response.setRules(policy.getRules().stream()
                    .map(rule -> {
                        PolicyResponse.PolicyRuleResponse ruleResponse = new PolicyResponse.PolicyRuleResponse();
                        ruleResponse.setId(rule.getId());
                        ruleResponse.setRuleName(rule.getRuleName());
                        ruleResponse.setConditionField(rule.getConditionField());
                        ruleResponse.setOperator(rule.getOperator());
                        ruleResponse.setConditionValue(rule.getConditionValue());
                        ruleResponse.setErrorMessage(rule.getErrorMessage());
                        ruleResponse.setPriority(rule.getPriority());
                        ruleResponse.setIsActive(rule.getIsActive());
                        ruleResponse.setCreatedAt(rule.getCreatedAt());
                        ruleResponse.setUpdatedAt(rule.getUpdatedAt());
                        return ruleResponse;
                    })
                    .toList());
        }
        
        return response;
    }
}