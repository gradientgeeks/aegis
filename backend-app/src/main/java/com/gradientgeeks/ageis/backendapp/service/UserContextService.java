package com.gradientgeeks.ageis.backendapp.service;

import com.gradientgeeks.ageis.backendapp.entity.Account;
import com.gradientgeeks.ageis.backendapp.entity.AccountType;
import com.gradientgeeks.ageis.backendapp.entity.User;
import com.gradientgeeks.ageis.backendapp.entity.UserKYC;
import com.gradientgeeks.ageis.backendapp.repository.AccountRepository;
import com.gradientgeeks.ageis.backendapp.repository.UserKYCRepository;
import com.gradientgeeks.ageis.backendapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for extracting user context and metadata for policy enforcement.
 * Extracts anonymized user information without exposing sensitive data.
 */
@Service
public class UserContextService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserContextService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private UserKYCRepository userKYCRepository;
    
    @Autowired
    private AnonymizedMappingService anonymizedMappingService;
    
    @Autowired
    private TransactionService transactionService;
    
    /**
     * Extracts user metadata from authenticated user session
     */
    public Map<String, Object> extractUserMetadata(String username, String deviceId, String transactionType, 
                                                  Object transactionAmount, String beneficiaryType) {
        try {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                logger.warn("User not found: {}", username);
                return createBasicMetadata(username, deviceId);
            }
            
            User user = userOpt.get();
            
            // Create anonymized user ID
            String anonymizedUserId = anonymizedMappingService.createAnonymizedUserId(username, deviceId);
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("anonymizedUserId", anonymizedUserId);
            metadata.put("deviceId", deviceId);
            
            // Session context
            Map<String, Object> sessionContext = extractSessionContext(user);
            metadata.put("sessionContext", sessionContext);
            
            // Transaction context (if transaction-related)
            if (transactionType != null) {
                Map<String, Object> transactionContext = extractTransactionContext(
                        transactionType, transactionAmount, beneficiaryType);
                metadata.put("transactionContext", transactionContext);
            }
            
            // Risk factors
            Map<String, Object> riskFactors = extractRiskFactors(user, deviceId);
            metadata.put("riskFactors", riskFactors);
            
            // User limits (transaction limits)
            Map<String, Object> userLimits = extractUserLimits(user);
            metadata.put("userLimits", userLimits);
            
            logger.debug("Extracted user metadata for anonymized user: {}", anonymizedUserId);
            return metadata;
            
        } catch (Exception e) {
            logger.error("Error extracting user metadata for user: {}", username, e);
            return createBasicMetadata(username, deviceId);
        }
    }
    
    /**
     * Extracts session context information
     */
    private Map<String, Object> extractSessionContext(User user) {
        Map<String, Object> sessionContext = new HashMap<>();
        
        // Account tier based on primary account
        String accountTier = determineAccountTier(user);
        sessionContext.put("accountTier", accountTier);
        
        // Account age in months
        long accountAgeMonths = ChronoUnit.MONTHS.between(user.getCreatedAt(), LocalDateTime.now());
        sessionContext.put("accountAge", accountAgeMonths);
        
        // KYC level
        String kycLevel = determineKycLevel(user);
        sessionContext.put("kycLevel", kycLevel);
        
        // Last login timestamp
        if (user.getLastLogin() != null) {
            sessionContext.put("lastLoginTimestamp", user.getLastLogin().toString());
        }
        
        // Device binding info
        sessionContext.put("hasDeviceBinding", user.hasDeviceBinding());
        sessionContext.put("deviceBindingCount", user.getDeviceIds() != null ? user.getDeviceIds().size() : 0);
        
        return sessionContext;
    }
    
    /**
     * Extracts transaction context information
     */
    private Map<String, Object> extractTransactionContext(String transactionType, Object transactionAmount, 
                                                          String beneficiaryType) {
        Map<String, Object> transactionContext = new HashMap<>();
        
        transactionContext.put("transactionType", transactionType);
        
        // Add actual transaction amount for policy validation
        if (transactionAmount != null) {
            transactionContext.put("amount", transactionAmount);
            // Also categorize amount into ranges for privacy
            String amountRange = categorizeAmount(transactionAmount);
            transactionContext.put("amountRange", amountRange);
        }
        
        // Beneficiary type
        if (beneficiaryType != null) {
            transactionContext.put("beneficiaryType", beneficiaryType);
        } else {
            transactionContext.put("beneficiaryType", "UNKNOWN");
        }
        
        // Time of day
        String timeOfDay = getCurrentTimeOfDay();
        transactionContext.put("timeOfDay", timeOfDay);
        
        return transactionContext;
    }
    
    /**
     * Extracts risk factors
     */
    private Map<String, Object> extractRiskFactors(User user, String deviceId) {
        Map<String, Object> riskFactors = new HashMap<>();
        
        // Device change detection
        boolean isDeviceChanged = detectDeviceChange(user, deviceId);
        riskFactors.put("isDeviceChanged", isDeviceChanged);
        
        // Location change (simplified - would need geolocation data in real implementation)
        boolean isLocationChanged = false; // Placeholder - implement based on IP/location data
        riskFactors.put("isLocationChanged", isLocationChanged);
        
        // Dormant account detection (no activity for 90+ days)
        boolean isDormantAccount = user.getLastLogin() != null && 
                ChronoUnit.DAYS.between(user.getLastLogin(), LocalDateTime.now()) > 90;
        riskFactors.put("isDormantAccount", isDormantAccount);
        
        // Device rebinding requirement
        riskFactors.put("requiresDeviceRebinding", user.getRequiresDeviceRebinding());
        
        return riskFactors;
    }
    
    /**
     * Determines account tier based on user's primary account
     * Maps account types to tiers for policy evaluation
     */
    private String determineAccountTier(User user) {
        List<Account> accounts = accountRepository.findByUserId(user.getId().toString());
        
        if (accounts.isEmpty()) {
            return "BASIC"; // Default to BASIC tier
        }
        
        // Get the highest tier account
        AccountType highestType = accounts.stream()
            .map(Account::getAccountType)
            .reduce((type1, type2) -> {
                // Define priority order
                if (type1 == AccountType.CURRENT || type2 == AccountType.CURRENT) return AccountType.CURRENT;
                if (type1 == AccountType.SAVINGS || type2 == AccountType.SAVINGS) return AccountType.SAVINGS;
                return type1;
            })
            .orElse(AccountType.SAVINGS);
        
        // Map account types to tiers
        switch (highestType) {
            case CURRENT:
                return "CORPORATE"; // Current accounts are typically for businesses
            case SAVINGS:
                return "BASIC"; // Regular savings accounts
            default:
                return "BASIC";
        }
    }
    
    /**
     * Determines KYC level for user
     */
    private String determineKycLevel(User user) {
        Optional<UserKYC> kycOpt = userKYCRepository.findByUserId(user.getId());
        
        if (kycOpt.isEmpty()) {
            return "NONE";
        }
        
        UserKYC kyc = kycOpt.get();
        if (kyc.getIsVerified()) {
            // Enhanced KYC if both Aadhaar and PAN are present
            if (kyc.getAadhaarLast4() != null && kyc.getPanNumber() != null) {
                return "FULL";
            } else {
                return "BASIC";
            }
        }
        
        return "PENDING";
    }
    
    /**
     * Categorizes transaction amount into privacy-preserving ranges
     */
    private String categorizeAmount(Object amount) {
        try {
            double amountValue = Double.parseDouble(amount.toString());
            
            if (amountValue < 1000) {
                return "MICRO";
            } else if (amountValue < 10000) {
                return "LOW";
            } else if (amountValue < 100000) {
                return "MEDIUM";
            } else if (amountValue < 1000000) {
                return "HIGH";
            } else {
                return "VERY_HIGH";
            }
        } catch (NumberFormatException e) {
            logger.warn("Invalid transaction amount: {}", amount);
            return "UNKNOWN";
        }
    }
    
    /**
     * Gets current time of day category
     */
    private String getCurrentTimeOfDay() {
        int hour = LocalDateTime.now().getHour();
        
        if (hour >= 6 && hour < 18) {
            return "BUSINESS_HOURS";
        } else if (hour >= 18 && hour < 22) {
            return "AFTER_HOURS";
        } else {
            return "NIGHT";
        }
    }
    
    /**
     * Detects if user is using a different device than usual
     */
    private boolean detectDeviceChange(User user, String deviceId) {
        if (!user.hasDeviceBinding()) {
            return false; // No baseline to compare
        }
        
        // Check if current device is in user's device list
        if (user.getDeviceIds() != null && user.getDeviceIds().contains(deviceId)) {
            return false; // Known device
        }
        
        // Check if it's the bound device
        if (user.isDeviceBound(deviceId)) {
            return false; // Primary bound device
        }
        
        return true; // New/unknown device
    }
    
    /**
     * Extracts user transaction limits
     */
    private Map<String, Object> extractUserLimits(User user) {
        Map<String, Object> userLimits = new HashMap<>();
        
        try {
            // Get transaction statistics for the user
            Map<String, Object> transactionStats = transactionService.getUserTransactionStatistics(user.getId());
            
            // Daily limits
            userLimits.put("dailyTransactionCount", transactionStats.getOrDefault("dailyCount", 0));
            userLimits.put("dailyTransactionAmount", transactionStats.getOrDefault("dailyAmount", 0.0));
            
            // Weekly limits  
            userLimits.put("weeklyTransactionCount", transactionStats.getOrDefault("weeklyCount", 0));
            userLimits.put("weeklyTransactionAmount", transactionStats.getOrDefault("weeklyAmount", 0.0));
            
            // Monthly limits
            userLimits.put("monthlyTransactionCount", transactionStats.getOrDefault("monthlyCount", 0));
            userLimits.put("monthlyTransactionAmount", transactionStats.getOrDefault("monthlyAmount", 0.0));
            
            logger.debug("Extracted user limits - Daily amount: {}, Daily count: {}", 
                userLimits.get("dailyTransactionAmount"), userLimits.get("dailyTransactionCount"));
            
        } catch (Exception e) {
            logger.warn("Could not extract user limits for user: {}", user.getUsername(), e);
            // Set default values if extraction fails
            userLimits.put("dailyTransactionCount", 0);
            userLimits.put("dailyTransactionAmount", 0.0);
            userLimits.put("weeklyTransactionCount", 0);
            userLimits.put("weeklyTransactionAmount", 0.0);
            userLimits.put("monthlyTransactionCount", 0);
            userLimits.put("monthlyTransactionAmount", 0.0);
        }
        
        return userLimits;
    }
    
    /**
     * Creates basic metadata when user details are not available
     */
    private Map<String, Object> createBasicMetadata(String username, String deviceId) {
        Map<String, Object> metadata = new HashMap<>();
        
        String anonymizedUserId = anonymizedMappingService.createAnonymizedUserId(username, deviceId);
        metadata.put("anonymizedUserId", anonymizedUserId);
        metadata.put("deviceId", deviceId);
        
        // Basic session context
        Map<String, Object> sessionContext = new HashMap<>();
        sessionContext.put("accountTier", "UNKNOWN");
        sessionContext.put("accountAge", 0);
        sessionContext.put("kycLevel", "UNKNOWN");
        metadata.put("sessionContext", sessionContext);
        
        // Basic risk factors
        Map<String, Object> riskFactors = new HashMap<>();
        riskFactors.put("isDeviceChanged", false);
        riskFactors.put("isLocationChanged", false);
        riskFactors.put("isDormantAccount", false);
        metadata.put("riskFactors", riskFactors);
        
        return metadata;
    }
}