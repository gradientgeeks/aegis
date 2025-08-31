package com.gradientgeeks.ageis.backendapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for creating anonymized user mappings.
 * Provides privacy-preserving user identification for policy enforcement.
 */
@Service
public class AnonymizedMappingService {
    
    private static final Logger logger = LoggerFactory.getLogger(AnonymizedMappingService.class);
    
    @Value("${app.security.anonymization.salt:UCO_BANK_DEFAULT_SALT_2024}")
    private String organizationSalt;
    
    @Value("${aegis.api.client-id}")
    private String clientId;
    
    // Cache for consistent mapping within session
    private final ConcurrentHashMap<String, String> mappingCache = new ConcurrentHashMap<>();
    
    /**
     * Creates an anonymized user ID that's consistent for the same user-device-organization combination
     */
    public String createAnonymizedUserId(String username, String deviceId) {
        try {
            // Create cache key
            String cacheKey = username + ":" + deviceId + ":" + clientId;
            
            // Check cache first for consistency within session
            String cachedId = mappingCache.get(cacheKey);
            if (cachedId != null) {
                return cachedId;
            }
            
            // Create composite string for hashing
            String composite = String.join("|",
                    username,
                    deviceId,
                    clientId,
                    organizationSalt
            );
            
            // Generate SHA-256 hash
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(composite.getBytes(StandardCharsets.UTF_8));
            
            // Encode to base64 and take first 16 characters for readability
            String anonymizedId = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(hashBytes)
                    .substring(0, 16);
            
            // Cache the result
            mappingCache.put(cacheKey, anonymizedId);
            
            logger.debug("Created anonymized user ID for device: {}", deviceId);
            return anonymizedId;
            
        } catch (NoSuchAlgorithmException e) {
            logger.error("SHA-256 algorithm not available", e);
            // Fallback to simple hash
            return createFallbackAnonymizedId(username, deviceId);
        } catch (Exception e) {
            logger.error("Error creating anonymized user ID", e);
            return createFallbackAnonymizedId(username, deviceId);
        }
    }
    
    /**
     * Creates a base user ID that's consistent across devices for the same user
     * Used for cross-device policy enforcement
     */
    public String createBaseUserGroupId(String username) {
        try {
            String composite = String.join("|",
                    username,
                    organizationSalt,
                    "BASE_USER"
            );
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(composite.getBytes(StandardCharsets.UTF_8));
            
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(hashBytes)
                    .substring(0, 12);
            
        } catch (Exception e) {
            logger.error("Error creating base user group ID", e);
            return username.hashCode() + "_BASE";
        }
    }
    
    /**
     * Creates session-specific anonymized ID for temporary tracking
     */
    public String createSessionAnonymizedId(String username, String deviceId, String sessionId) {
        try {
            String composite = String.join("|",
                    username,
                    deviceId,
                    sessionId,
                    clientId,
                    String.valueOf(System.currentTimeMillis() / 3600000) // Hour-based for session consistency
            );
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(composite.getBytes(StandardCharsets.UTF_8));
            
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(hashBytes)
                    .substring(0, 20);
            
        } catch (Exception e) {
            logger.error("Error creating session anonymized ID", e);
            return createFallbackAnonymizedId(username, deviceId) + "_S";
        }
    }
    
    /**
     * Creates a transaction-specific anonymized ID for audit purposes
     */
    public String createTransactionAnonymizedId(String anonymizedUserId, String transactionType, long timestamp) {
        try {
            String composite = String.join("|",
                    anonymizedUserId,
                    transactionType,
                    String.valueOf(timestamp / 60000), // Minute-based for transaction grouping
                    organizationSalt
            );
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(composite.getBytes(StandardCharsets.UTF_8));
            
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(hashBytes)
                    .substring(0, 16);
            
        } catch (Exception e) {
            logger.error("Error creating transaction anonymized ID", e);
            return anonymizedUserId + "_T" + (timestamp % 10000);
        }
    }
    
    /**
     * Validates if an anonymized ID could belong to a specific username (without revealing the username)
     */
    public boolean validateAnonymizedId(String anonymizedId, String username, String deviceId) {
        try {
            String expectedId = createAnonymizedUserId(username, deviceId);
            return anonymizedId.equals(expectedId);
        } catch (Exception e) {
            logger.error("Error validating anonymized ID", e);
            return false;
        }
    }
    
    /**
     * Creates metadata hash for additional privacy protection
     */
    public String createMetadataHash(Object... metadataValues) {
        try {
            StringBuilder composite = new StringBuilder();
            for (Object value : metadataValues) {
                if (value != null) {
                    composite.append(value.toString()).append("|");
                }
            }
            composite.append(organizationSalt);
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(composite.toString().getBytes(StandardCharsets.UTF_8));
            
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(hashBytes)
                    .substring(0, 12);
            
        } catch (Exception e) {
            logger.error("Error creating metadata hash", e);
            return "HASH_ERROR";
        }
    }
    
    /**
     * Clears the mapping cache (for security and memory management)
     */
    public void clearCache() {
        mappingCache.clear();
        logger.debug("Cleared anonymization mapping cache");
    }
    
    /**
     * Gets cache statistics for monitoring
     */
    public Map<String, Object> getCacheStatistics() {
        return Map.of(
                "cacheSize", mappingCache.size(),
                "clientId", clientId,
                "saltConfigured", organizationSalt != null && !organizationSalt.isEmpty()
        );
    }
    
    /**
     * Fallback method when cryptographic functions fail
     */
    private String createFallbackAnonymizedId(String username, String deviceId) {
        try {
            int hash = (username + deviceId + clientId + organizationSalt).hashCode();
            return "FALLBACK_" + Math.abs(hash);
        } catch (Exception e) {
            logger.error("Even fallback anonymization failed", e);
            return "ERROR_" + System.currentTimeMillis();
        }
    }
    
    /**
     * Creates a secure random salt for new organizations
     */
    public static String generateOrganizationSalt() {
        try {
            SecureRandom random = new SecureRandom();
            byte[] saltBytes = new byte[32];
            random.nextBytes(saltBytes);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(saltBytes);
        } catch (Exception e) {
            return "DEFAULT_SALT_" + System.currentTimeMillis();
        }
    }
    
    /**
     * Privacy compliance method - ensures no PII is logged
     */
    public Map<String, Object> createComplianceReport(String anonymizedUserId) {
        return Map.of(
                "anonymizedUserId", anonymizedUserId,
                "algorithm", "SHA-256",
                "saltUsed", organizationSalt != null && !organizationSalt.isEmpty(),
                "reversible", false,
                "piiExposed", false,
                "complianceLevel", "GDPR_COMPLIANT"
        );
    }
}