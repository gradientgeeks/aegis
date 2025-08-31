package com.gradientgeeks.ageis.backendapp.security;

import com.gradientgeeks.ageis.backendapp.config.PolicyEnforcementConfig;
import com.gradientgeeks.ageis.backendapp.dto.SignatureValidationResponse;
import com.gradientgeeks.ageis.backendapp.exception.UnauthorizedException;
import com.gradientgeeks.ageis.backendapp.service.AegisIntegrationService;
import com.gradientgeeks.ageis.backendapp.service.UserContextService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

/**
 * Security interceptor that validates request signatures using the Aegis Security API.
 */
@Component
public class AegisSecurityInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(AegisSecurityInterceptor.class);
    
    private final AegisIntegrationService aegisIntegrationService;
    
    @Autowired
    private UserContextService userContextService;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private PolicyEnforcementConfig policyEnforcementConfig;
    
    @Value("${security.request.signature.header}")
    private String signatureHeader;
    
    @Value("${security.request.device-id.header}")
    private String deviceIdHeader;
    
    @Value("${security.request.timestamp.header}")
    private String timestampHeader;
    
    @Value("${security.request.nonce.header}")
    private String nonceHeader;
    
    @Value("${security.request.timestamp.tolerance}")
    private long timestampTolerance;
    
    public AegisSecurityInterceptor(AegisIntegrationService aegisIntegrationService) {
        this.aegisIntegrationService = aegisIntegrationService;
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        logger.debug("Validating request: {} {}", request.getMethod(), request.getRequestURI());
        
        // Extract security headers
        String signature = request.getHeader(signatureHeader);
        String deviceId = request.getHeader(deviceIdHeader);
        String timestamp = request.getHeader(timestampHeader);
        String nonce = request.getHeader(nonceHeader);
        
        // Validate required headers
        if (signature == null || deviceId == null || timestamp == null || nonce == null) {
            logger.warn("Missing required security headers");
            throw new UnauthorizedException("Missing required security headers");
        }
        
        // Validate timestamp to prevent replay attacks
        validateTimestamp(timestamp);
        
        // Get request body hash if present
        String bodyHash = null;
        if (request.getContentLength() > 0) {
            bodyHash = computeBodyHash(request);
        }
        
        // Extract user metadata for policy enforcement
        Map<String, Object> userMetadata = null;
        String username = null;
        
        // First try to get username from SecurityContext (set by JWT filter)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !(authentication instanceof AnonymousAuthenticationToken)) {
            username = authentication.getName();
            logger.debug("Got username from SecurityContext: {}", username);
        }
        
        // If not found, try to get username from JWT token in Authorization header
        if (username == null) {
            String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);
                try {
                    username = jwtTokenProvider.getUsernameFromToken(token);
                    logger.debug("Extracted username from JWT token: {}", username);
                } catch (Exception e) {
                    logger.debug("Could not extract username from JWT token: {}", e.getMessage());
                }
            }
        }
        
        // Fall back to session if JWT extraction failed
        if (username == null) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                username = (String) session.getAttribute("username");
                logger.debug("Got username from session: {}", username);
            }
        }
        
        // Check if this endpoint requires policy enforcement
        boolean requiresPolicy = policyEnforcementConfig.requiresPolicyEnforcement(
                request.getMethod(), 
                request.getRequestURI()
        );
        
        logger.debug("Policy enforcement required for {} {}: {}", 
                    request.getMethod(), request.getRequestURI(), requiresPolicy);
        
        // Only extract metadata if policy enforcement is required AND we have a username
        if (requiresPolicy && username != null) {
            // Determine transaction context based on URI
            String transactionType = determineTransactionType(request.getRequestURI());
            String beneficiaryType = extractBeneficiaryType(request);
            
            // Extract transaction amount if this is a transfer
            Object transactionAmount = null;
            if ("TRANSFER".equals(transactionType) && request.getContentLength() > 0) {
                // For secure transfer endpoints, we can't extract amount from encrypted payload
                // The amount will need to be passed via a header or query parameter
                String amountHeader = request.getHeader("X-Transaction-Amount");
                if (amountHeader != null) {
                    try {
                        transactionAmount = Double.parseDouble(amountHeader);
                        logger.debug("Got transaction amount from header: {}", transactionAmount);
                    } catch (NumberFormatException e) {
                        logger.debug("Invalid transaction amount header: {}", amountHeader);
                    }
                } else {
                    // Try to extract from non-encrypted payload
                    transactionAmount = extractTransactionAmount(request);
                }
            }
            
            // Extract user metadata for policy enforcement
            userMetadata = userContextService.extractUserMetadata(
                    username, 
                    deviceId, 
                    transactionType,
                    transactionAmount,
                    beneficiaryType
            );
            
            logger.debug("Extracted user metadata for policy enforcement: {}", 
                       userMetadata != null ? userMetadata.keySet() : "none");
            if (transactionAmount != null) {
                logger.debug("Transaction amount: {}", transactionAmount);
            }
        } else if (requiresPolicy && username == null) {
            logger.warn("Policy enforcement required but no username available for endpoint: {} {}", 
                       request.getMethod(), request.getRequestURI());
        }
        
        // Validate signature - use appropriate method based on policy requirement
        SignatureValidationResponse validationResponse;
        if (requiresPolicy && userMetadata != null) {
            // Validate with policy enforcement
            validationResponse = aegisIntegrationService.validateSignatureWithMetadata(
                    deviceId,
                    signature,
                    request.getMethod(),
                    request.getRequestURI(),
                    timestamp,
                    nonce,
                    bodyHash,
                    userMetadata
            );
        } else {
            // Simple signature validation without policy
            validationResponse = aegisIntegrationService.validateSignature(
                    deviceId,
                    signature,
                    request.getMethod(),
                    request.getRequestURI(),
                    timestamp,
                    nonce,
                    bodyHash
            );
        }
        
        if (!validationResponse.isValid()) {
            logger.warn("Invalid signature for device: {} - {}", deviceId, validationResponse.getMessage());
            throw new UnauthorizedException("Invalid request signature");
        }
        
        // Store device ID in request attributes for use in controllers
        request.setAttribute("deviceId", deviceId);
        request.setAttribute("signatureTimestamp", parseTimestamp(timestamp));
        request.setAttribute("nonce", nonce);
        request.setAttribute("signature", signature);
        
        logger.debug("Request validated successfully for device: {}", deviceId);
        return true;
    }
    
    /**
     * Validates the timestamp to prevent replay attacks.
     */
    private void validateTimestamp(String timestampStr) {
        try {
            long timestamp = Long.parseLong(timestampStr);
            long currentTime = System.currentTimeMillis();
            long difference = Math.abs(currentTime - timestamp);
            
            if (difference > timestampTolerance) {
                throw new UnauthorizedException("Request timestamp is too old or too far in the future");
            }
        } catch (NumberFormatException e) {
            throw new UnauthorizedException("Invalid timestamp format");
        }
    }
    
    /**
     * Parses timestamp string to LocalDateTime.
     */
    private LocalDateTime parseTimestamp(String timestampStr) {
        try {
            long timestamp = Long.parseLong(timestampStr);
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
    
    /**
     * Computes SHA-256 hash of request body.
     */
    private String computeBodyHash(HttpServletRequest request) throws Exception {
        // For POST/PUT requests, we need to read the body
        if ("POST".equalsIgnoreCase(request.getMethod()) || "PUT".equalsIgnoreCase(request.getMethod())) {
            // Read the body - our RepeatableRequestBodyFilter ensures this can be read multiple times
            String body = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
            if (!body.isEmpty()) {
                return aegisIntegrationService.computeSha256Hash(body);
            }
        }
        return null;
    }
    
    /**
     * Determines transaction type based on request URI
     */
    private String determineTransactionType(String uri) {
        if (uri == null) {
            return null;
        }
        
        // Map URIs to transaction types
        if (uri.contains("/transfer")) {
            return "TRANSFER";
        } else if (uri.contains("/payment")) {
            return "PAYMENT";
        } else if (uri.contains("/withdrawal")) {
            return "WITHDRAWAL";
        } else if (uri.contains("/deposit")) {
            return "DEPOSIT";
        } else if (uri.contains("/balance")) {
            return "BALANCE_CHECK";
        } else if (uri.contains("/account")) {
            return "ACCOUNT_ACCESS";
        } else if (uri.contains("/login")) {
            return "LOGIN";
        } else if (uri.contains("/auth")) {
            return "AUTHENTICATION";
        }
        
        return "GENERAL";
    }
    
    /**
     * Extracts beneficiary type from request if applicable
     */
    private String extractBeneficiaryType(HttpServletRequest request) {
        // Check request parameters or headers for beneficiary information
        String beneficiaryId = request.getParameter("beneficiaryId");
        if (beneficiaryId != null) {
            // In a real implementation, you would check if this is a new or existing beneficiary
            // For now, we'll use a simple heuristic
            return beneficiaryId.startsWith("NEW_") ? "NEW" : "EXISTING";
        }
        
        // Check custom header if present
        String beneficiaryType = request.getHeader("X-Beneficiary-Type");
        if (beneficiaryType != null) {
            return beneficiaryType;
        }
        
        return "UNKNOWN";
    }
    
    /**
     * Extracts transaction amount from request body if available
     */
    private Object extractTransactionAmount(HttpServletRequest request) {
        try {
            // Read the body - our RepeatableRequestBodyFilter ensures this can be read multiple times
            String body = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
            if (!body.isEmpty()) {
                // Parse JSON to extract amount
                // This is a simple implementation - in production use proper JSON parsing
                if (body.contains("\"amount\"")) {
                    int amountIndex = body.indexOf("\"amount\"");
                    int colonIndex = body.indexOf(":", amountIndex);
                    int commaIndex = body.indexOf(",", colonIndex);
                    int endIndex = commaIndex > 0 ? commaIndex : body.indexOf("}", colonIndex);
                    
                    if (colonIndex > 0 && endIndex > colonIndex) {
                        String amountStr = body.substring(colonIndex + 1, endIndex).trim();
                        // Remove quotes if present
                        amountStr = amountStr.replace("\"", "");
                        
                        try {
                            return Double.parseDouble(amountStr);
                        } catch (NumberFormatException e) {
                            logger.debug("Could not parse amount: {}", amountStr);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Could not extract transaction amount from request", e);
        }
        return null;
    }
}