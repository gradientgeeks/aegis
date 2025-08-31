package com.gradientgeeks.aegis.sfe.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class CryptographyService {
    
    private static final Logger logger = LoggerFactory.getLogger(CryptographyService.class);
    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    
    public String generateSecretKey() {
        return new BigInteger(256, SECURE_RANDOM).toString(32);
    }
    
    public String generateRegistrationKey() {
        return new BigInteger(256, SECURE_RANDOM).toString(32);
    }
    
    /**
     * @deprecated Use generatePersistentDeviceId instead for fraud prevention
     */
    @Deprecated
    public String generateDeviceId() {
        return "dev_" + new BigInteger(128, SECURE_RANDOM).toString(32);
    }
    
    /**
     * Generates a persistent device ID based on device fingerprint.
     * This ensures the same device will always get the same device ID,
     * even after app uninstall/reinstall or factory reset.
     * 
     * This follows Android best practices for fraud detection use cases
     * by creating a deterministic identifier from stable device characteristics.
     * 
     * IMPORTANT: The device ID is now generated ONLY from the fingerprint hash,
     * not including the client ID. This allows the same physical device to be
     * used with multiple banking apps (different client IDs) legitimately.
     * 
     * @param fingerprintHash The composite hash of the device fingerprint
     * @param clientId The client identifier (kept for backward compatibility but not used)
     * @return Deterministic device ID based on fingerprint
     */
    public String generatePersistentDeviceId(String fingerprintHash, String clientId) {
        try {
            logger.debug("Generating persistent device ID from fingerprint");
            
            // Generate device ID only from fingerprint hash
            // This allows the same device to be used with multiple bank apps
            
            // Use SHA-256 to create a deterministic hash
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(fingerprintHash.getBytes(StandardCharsets.UTF_8));
            
            // Convert to hex string and truncate to reasonable length
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            // Take first 32 characters for a reasonable device ID length
            String persistentId = "dev_" + hexString.toString().substring(0, 32);
            
            logger.debug("Generated persistent device ID: {}", persistentId);
            return persistentId;
            
        } catch (NoSuchAlgorithmException e) {
            logger.error("Failed to generate persistent device ID", e);
            throw new RuntimeException("Failed to generate persistent device ID", e);
        }
    }
    
    /**
     * Generates a persistent device ID based only on stable hardware characteristics.
     * This method creates a device ID using only hardware fingerprint data that
     * remains constant across app reinstalls and network changes.
     * 
     * @param hardwareHash Hash of stable hardware characteristics (manufacturer, model, device, etc.)
     * @return Deterministic device ID based on stable hardware only
     */
    public String generatePersistentDeviceIdFromHardware(String hardwareHash) {
        try {
            logger.info("Generating persistent device ID from hardware hash: {}", hardwareHash);
            
            // Use SHA-256 to create a deterministic hash from stable hardware characteristics
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(hardwareHash.getBytes(StandardCharsets.UTF_8));
            
            // Convert to hex string and truncate to reasonable length
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            // Take first 32 characters for a reasonable device ID length
            String persistentId = "dev_" + hexString.toString().substring(0, 32);
            
            logger.info("Generated persistent device ID from hardware: {} (full hash: {})", 
                persistentId, hexString.toString());
            return persistentId;
            
        } catch (NoSuchAlgorithmException e) {
            logger.error("Failed to generate persistent device ID from hardware", e);
            throw new RuntimeException("Failed to generate persistent device ID from hardware", e);
        }
    }
    
    public String computeHmacSha256(String secretKey, String data) {
        try {
            logger.debug("Computing HMAC-SHA256 with data length: {} characters", data.length());
            logger.trace("Data to sign: {}", data);
            
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKeySpec);
            byte[] hashBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            String signature = Base64.getEncoder().encodeToString(hashBytes);
            
            logger.debug("Computed HMAC-SHA256 signature: {}", signature);
            return signature;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            logger.error("Failed to compute HMAC-SHA256", e);
            throw new RuntimeException("Failed to compute HMAC-SHA256", e);
        }
    }
    
    public boolean verifyHmacSha256(String secretKey, String data, String expectedSignature) {
        try {
            logger.debug("Verifying HMAC-SHA256 signature");
            String computedSignature = computeHmacSha256(secretKey, data);
            
            boolean isEqual = MessageDigest.isEqual(
                computedSignature.getBytes(StandardCharsets.UTF_8),
                expectedSignature.getBytes(StandardCharsets.UTF_8)
            );
            
            logger.debug("Signature verification result: {}", isEqual);
            if (!isEqual) {
                logger.debug("Computed: {} vs Expected: {}", computedSignature, expectedSignature);
            }
            
            return isEqual;
        } catch (Exception e) {
            logger.error("Error during HMAC-SHA256 verification", e);
            return false;
        }
    }
    
    public String hashString(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to compute SHA-256 hash", e);
        }
    }
    
    public boolean isValidBase64(String input) {
        try {
            Base64.getDecoder().decode(input);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}