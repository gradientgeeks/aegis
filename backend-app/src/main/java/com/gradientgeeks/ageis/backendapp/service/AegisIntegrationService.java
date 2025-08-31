package com.gradientgeeks.ageis.backendapp.service;

import com.gradientgeeks.ageis.backendapp.dto.FraudReportRequest;
import com.gradientgeeks.ageis.backendapp.dto.SignatureValidationRequest;
import com.gradientgeeks.ageis.backendapp.dto.SignatureValidationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

/**
 * Service for integrating with the Aegis Security API.
 * Handles signature validation requests to verify the authenticity of incoming requests.
 */
@Service
public class AegisIntegrationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AegisIntegrationService.class);
    
    private final RestTemplate restTemplate;
    
    @Value("${aegis.api.base-url}")
    private String aegisBaseUrl;
    
    @Value("${aegis.api.validate-endpoint}")
    private String validateEndpoint;
    
    @Value("${aegis.api.fraud-report-endpoint:/admin/fraud-report}")
    private String fraudReportEndpoint;
    
    @Value("${aegis.api.client-id}")
    private String clientId;
    
    public AegisIntegrationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * Validates a request signature with the Aegis Security API.
     * 
     * @param deviceId The device ID from the request
     * @param signature The HMAC signature from the request
     * @param method The HTTP method
     * @param uri The request URI
     * @param timestamp The request timestamp
     * @param nonce The request nonce
     * @param bodyHash The hash of the request body (if present)
     * @return SignatureValidationResponse indicating if the signature is valid
     */
    public SignatureValidationResponse validateSignature(String deviceId, String signature,
                                                       String method, String uri, 
                                                       String timestamp, String nonce,
                                                       String bodyHash) {
        return validateSignatureWithMetadata(deviceId, signature, method, uri, timestamp, nonce, bodyHash, null);
    }
    
    /**
     * Validates a request signature with user metadata for policy enforcement.
     * 
     * @param deviceId The device ID from the request
     * @param signature The HMAC signature from the request
     * @param method The HTTP method
     * @param uri The request URI
     * @param timestamp The request timestamp
     * @param nonce The request nonce
     * @param bodyHash The hash of the request body (if present)
     * @param userMetadata User metadata for policy enforcement
     * @return SignatureValidationResponse indicating if the signature is valid
     */
    public SignatureValidationResponse validateSignatureWithMetadata(String deviceId, String signature,
                                                                    String method, String uri, 
                                                                    String timestamp, String nonce,
                                                                    String bodyHash, Map<String, Object> userMetadata) {
        try {
            // Create the string to sign in the same format as the client
            String stringToSign = createStringToSign(method, uri, timestamp, nonce, bodyHash);
            
            logger.debug("Validating signature for device: {} with stringToSign: {}", deviceId, stringToSign);
            
            // Create validation request with client ID and metadata
            SignatureValidationRequest request = new SignatureValidationRequest(deviceId, signature, stringToSign);
            request.setClientId(clientId);
            request.setUserMetadata(userMetadata);
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Client-Id", clientId);
            
            HttpEntity<SignatureValidationRequest> entity = new HttpEntity<>(request, headers);
            
            // Make the API call
            String url = aegisBaseUrl + validateEndpoint;
            ResponseEntity<SignatureValidationResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                SignatureValidationResponse.class
            );
            
            SignatureValidationResponse validationResponse = response.getBody();
            
            if (validationResponse != null && validationResponse.isValid()) {
                logger.info("Signature validation successful for device: {}", deviceId);
            } else {
                logger.warn("Signature validation failed for device: {}", deviceId);
            }
            
            return validationResponse;
            
        } catch (HttpClientErrorException e) {
            logger.error("Client error during signature validation: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return new SignatureValidationResponse(false, "Invalid signature", deviceId);
        } catch (HttpServerErrorException e) {
            logger.error("Server error during signature validation: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return new SignatureValidationResponse(false, "Validation service error", deviceId);
        } catch (Exception e) {
            logger.error("Unexpected error during signature validation", e);
            return new SignatureValidationResponse(false, "Internal error during validation", deviceId);
        }
    }
    
    /**
     * Creates the string to sign in the format expected by the Aegis API.
     * Format: METHOD|URI|TIMESTAMP|NONCE|BODY_HASH
     */
    private String createStringToSign(String method, String uri, String timestamp, 
                                    String nonce, String bodyHash) {
        StringBuilder stringToSign = new StringBuilder();
        stringToSign.append(method.toUpperCase()).append("|");
        stringToSign.append(uri).append("|");
        stringToSign.append(timestamp).append("|");
        stringToSign.append(nonce).append("|");
        
        if (bodyHash != null && !bodyHash.isEmpty()) {
            stringToSign.append(bodyHash);
        } else {
            // Empty string for empty body, matching client SDK behavior
            stringToSign.append("");
        }
        
        return stringToSign.toString();
    }
    
    /**
     * Reports a device for fraud to the Aegis Security API.
     * This allows banks to report suspicious devices that should be blocked.
     * 
     * @param deviceId The device ID to report
     * @param bankTransactionId The bank's internal transaction ID for audit trail
     * @param reasonCode The reason code for the fraud report (e.g., BANK_ML_HIGH_RISK)
     * @param description Additional description of the suspicious activity
     * @return True if the fraud report was processed successfully, false otherwise
     */
    public boolean reportFraud(String deviceId, String bankTransactionId, String reasonCode, String description) {
        try {
            logger.warn("Reporting fraud for device: {} - Transaction: {}, Reason: {}", 
                deviceId, bankTransactionId, reasonCode);
            
            // Create fraud report request
            FraudReportRequest request = new FraudReportRequest(
                deviceId, bankTransactionId, reasonCode, description
            );
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Client-Id", clientId);
            
            HttpEntity<FraudReportRequest> entity = new HttpEntity<>(request, headers);
            
            // Make the API call
            String url = aegisBaseUrl + fraudReportEndpoint;
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Fraud report submitted successfully for device: {}", deviceId);
                return true;
            } else {
                logger.error("Fraud report failed with status: {} for device: {}", 
                    response.getStatusCode(), deviceId);
                return false;
            }
            
        } catch (HttpClientErrorException e) {
            logger.error("Client error during fraud reporting: {} - {} for device: {}", 
                e.getStatusCode(), e.getResponseBodyAsString(), deviceId);
            return false;
        } catch (HttpServerErrorException e) {
            logger.error("Server error during fraud reporting: {} - {} for device: {}", 
                e.getStatusCode(), e.getResponseBodyAsString(), deviceId);
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error during fraud reporting for device: {}", deviceId, e);
            return false;
        }
    }
    
    /**
     * Reports fraud with automatic reason code based on risk score.
     * 
     * @param deviceId The device ID to report
     * @param bankTransactionId The bank's internal transaction ID
     * @param riskScore Risk score from the bank's ML model (0-100)
     * @param description Additional description
     * @return True if the fraud report was processed successfully
     */
    public boolean reportFraudWithRiskScore(String deviceId, String bankTransactionId, 
                                          int riskScore, String description) {
        String reasonCode;
        
        if (riskScore >= 90) {
            reasonCode = "CONFIRMED_FRAUD";
        } else if (riskScore >= 70) {
            reasonCode = "BANK_ML_HIGH_RISK";
        } else if (riskScore >= 50) {
            reasonCode = "SUSPICIOUS_ACTIVITY";
        } else {
            reasonCode = "LOW_RISK_FLAGGED";
        }
        
        String enhancedDescription = String.format("Risk Score: %d/100. %s", riskScore, 
            description != null ? description : "Flagged by bank ML model");
        
        return reportFraud(deviceId, bankTransactionId, reasonCode, enhancedDescription);
    }
    
    /**
     * Computes SHA-256 hash of the given data.
     * Returns hex-encoded hash to match Android SDK implementation.
     */
    public String computeSha256Hash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes());
            
            // Convert to hex string to match Android SDK
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}