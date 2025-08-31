package com.gradientgeeks.ageis.backendapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import java.util.*;

/**
 * Configuration for policy enforcement on specific endpoints.
 * Defines which endpoints require policy validation for sensitive operations.
 */
@Configuration
@ConfigurationProperties(prefix = "security.policy-enforcement")
public class PolicyEnforcementConfig {
    
    /**
     * Map of endpoints that require policy enforcement.
     * Key: HTTP method (GET, POST, PUT, DELETE)
     * Value: Set of URI patterns that require policy enforcement
     */
    private Map<String, Set<String>> protectedEndpoints = new HashMap<>();
    
    /**
     * Global flag to enable/disable policy enforcement
     */
    private boolean enabled = true;
    
    /**
     * List of URI patterns that should always skip policy enforcement
     */
    private Set<String> excludedPatterns = new HashSet<>();
    
    public PolicyEnforcementConfig() {
        // Initialize with default sensitive endpoints
        initializeDefaults();
    }
    
    private void initializeDefaults() {
        // Transfer operations - always require policy
        addProtectedEndpoint("POST", "/api/v1/transactions/transfer");
        addProtectedEndpoint("POST", "/api/v1/transactions/transfer/secure");
        
        // Account modifications
        addProtectedEndpoint("PUT", "/api/v1/accounts/*");
        addProtectedEndpoint("DELETE", "/api/v1/accounts/*");
        
        // User profile changes
        addProtectedEndpoint("PUT", "/api/v1/users/*/profile");
        addProtectedEndpoint("POST", "/api/v1/users/*/change-password");
        
        // Admin operations
        addProtectedEndpoint("POST", "/api/v1/admin/*");
        addProtectedEndpoint("PUT", "/api/v1/admin/*");
        addProtectedEndpoint("DELETE", "/api/v1/admin/*");
        
        // Device rebinding - sensitive operation
        addProtectedEndpoint("POST", "/api/v1/auth/device/rebind");
        
        // Excluded patterns - never require policy
        excludedPatterns.add("/api/v1/auth/login");
        excludedPatterns.add("/api/v1/auth/logout");
        excludedPatterns.add("/api/v1/health");
        excludedPatterns.add("/api/v1/accounts/user/*"); // Simple reads don't need policy
    }
    
    /**
     * Check if an endpoint requires policy enforcement
     */
    public boolean requiresPolicyEnforcement(String method, String uri) {
        if (!enabled) {
            return false;
        }
        
        // Check if explicitly excluded
        for (String pattern : excludedPatterns) {
            if (matchesPattern(uri, pattern)) {
                return false;
            }
        }
        
        // Check if protected
        Set<String> patterns = protectedEndpoints.get(method.toUpperCase());
        if (patterns != null) {
            for (String pattern : patterns) {
                if (matchesPattern(uri, pattern)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Simple pattern matching supporting * wildcard
     */
    private boolean matchesPattern(String uri, String pattern) {
        if (pattern.equals(uri)) {
            return true;
        }
        
        if (pattern.contains("*")) {
            String regex = pattern.replace("*", ".*");
            return uri.matches(regex);
        }
        
        return false;
    }
    
    /**
     * Add a protected endpoint programmatically
     */
    public void addProtectedEndpoint(String method, String pattern) {
        protectedEndpoints.computeIfAbsent(method.toUpperCase(), k -> new HashSet<>())
                         .add(pattern);
    }
    
    /**
     * Remove a protected endpoint programmatically
     */
    public void removeProtectedEndpoint(String method, String pattern) {
        Set<String> patterns = protectedEndpoints.get(method.toUpperCase());
        if (patterns != null) {
            patterns.remove(pattern);
        }
    }
    
    // Getters and setters
    public Map<String, Set<String>> getProtectedEndpoints() {
        return protectedEndpoints;
    }
    
    public void setProtectedEndpoints(Map<String, Set<String>> protectedEndpoints) {
        this.protectedEndpoints = protectedEndpoints;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public Set<String> getExcludedPatterns() {
        return excludedPatterns;
    }
    
    public void setExcludedPatterns(Set<String> excludedPatterns) {
        this.excludedPatterns = excludedPatterns;
    }
}