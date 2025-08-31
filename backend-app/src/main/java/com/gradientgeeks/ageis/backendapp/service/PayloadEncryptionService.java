package com.gradientgeeks.ageis.backendapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Service for encrypting and decrypting request/response payloads using AES-256-GCM.
 */
@Service
public class PayloadEncryptionService {
    
    private static final Logger logger = LoggerFactory.getLogger(PayloadEncryptionService.class);
    private static final String AES_GCM_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int ENCRYPTED_PAYLOAD_VERSION = 1;
    
    @Autowired
    private SessionKeyService sessionKeyService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Decrypts an encrypted payload using the session key.
     */
    public String decryptPayload(EncryptedPayload encryptedPayload, String sessionId) throws Exception {
        logger.debug("Decrypting payload for session: {}", sessionId);
        
        // Validate version
        if (encryptedPayload.getVersion() != ENCRYPTED_PAYLOAD_VERSION) {
            throw new IllegalArgumentException("Unsupported encryption version: " + encryptedPayload.getVersion());
        }
        
        // Get session key
        SecretKey sessionKey = sessionKeyService.getSessionKey(sessionId);
        if (sessionKey == null) {
            throw new IllegalStateException("Session key not found for session: " + sessionId);
        }
        
        // Decode IV and ciphertext
        byte[] iv = Base64.getDecoder().decode(encryptedPayload.getIv());
        byte[] ciphertext = Base64.getDecoder().decode(encryptedPayload.getCiphertext());
        
        // Initialize cipher
        Cipher cipher = Cipher.getInstance(AES_GCM_ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, sessionKey, gcmSpec);
        
        // Add associated data if provided
        if (encryptedPayload.getAad() != null) {
            cipher.updateAAD(encryptedPayload.getAad().getBytes(StandardCharsets.UTF_8));
        }
        
        // Decrypt the payload
        byte[] decryptedBytes = cipher.doFinal(ciphertext);
        String decryptedPayload = new String(decryptedBytes, StandardCharsets.UTF_8);
        
        logger.debug("Successfully decrypted payload ({} bytes)", decryptedBytes.length);
        return decryptedPayload;
    }
    
    /**
     * Encrypts a response payload using the session key.
     */
    public EncryptedPayload encryptPayload(String payload, String sessionId, String aad) throws Exception {
        logger.debug("Encrypting payload for session: {}", sessionId);
        
        // Get session key
        SecretKey sessionKey = sessionKeyService.getSessionKey(sessionId);
        if (sessionKey == null) {
            throw new IllegalStateException("Session key not found for session: " + sessionId);
        }
        
        // Generate random IV
        byte[] iv = new byte[12]; // 96 bits for GCM
        new java.security.SecureRandom().nextBytes(iv);
        
        // Initialize cipher
        Cipher cipher = Cipher.getInstance(AES_GCM_ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, sessionKey, gcmSpec);
        
        // Add associated data if provided
        if (aad != null) {
            cipher.updateAAD(aad.getBytes(StandardCharsets.UTF_8));
        }
        
        // Encrypt the payload
        byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = cipher.doFinal(payloadBytes);
        
        // Create encrypted payload object
        EncryptedPayload encryptedPayload = new EncryptedPayload();
        encryptedPayload.setVersion(ENCRYPTED_PAYLOAD_VERSION);
        encryptedPayload.setIv(Base64.getEncoder().encodeToString(iv));
        encryptedPayload.setCiphertext(Base64.getEncoder().encodeToString(ciphertext));
        encryptedPayload.setAlgorithm("AES-256-GCM");
        encryptedPayload.setAad(aad);
        
        logger.debug("Successfully encrypted payload ({} bytes)", payloadBytes.length);
        return encryptedPayload;
    }
    
    /**
     * Decrypts a secure request and returns the original request object.
     */
    public <T> T decryptSecureRequest(SecureRequest secureRequest, String sessionId, Class<T> requestClass) throws Exception {
        // Validate metadata
        RequestMetadata metadata = secureRequest.getMetadata();
        if (!sessionId.equals(metadata.getSessionId())) {
            throw new IllegalArgumentException("Session ID mismatch");
        }
        
        // Decrypt payload
        String decryptedJson = decryptPayload(secureRequest.getEncryptedPayload(), sessionId);
        
        // Parse JSON to object
        return objectMapper.readValue(decryptedJson, requestClass);
    }
    
    /**
     * Creates an encrypted response.
     */
    public SecureResponse createSecureResponse(Object responseObject, String sessionId, int statusCode) throws Exception {
        // Convert response to JSON
        String responseJson = objectMapper.writeValueAsString(responseObject);
        
        // Create response metadata
        ResponseMetadata metadata = new ResponseMetadata();
        metadata.setStatusCode(statusCode);
        metadata.setTimestamp(System.currentTimeMillis());
        metadata.setSessionId(sessionId);
        
        // Create AAD from metadata
        String aad = statusCode + "|" + metadata.getTimestamp() + "|" + sessionId;
        
        // Encrypt payload
        EncryptedPayload encryptedPayload = encryptPayload(responseJson, sessionId, aad);
        
        // Create secure response
        SecureResponse secureResponse = new SecureResponse();
        secureResponse.setEncryptedPayload(encryptedPayload);
        secureResponse.setMetadata(metadata);
        
        return secureResponse;
    }
    
    // Data classes
    public static class EncryptedPayload {
        private int version;
        private String iv;
        private String ciphertext;
        private String algorithm;
        private String aad;
        
        // Getters and setters
        public int getVersion() { return version; }
        public void setVersion(int version) { this.version = version; }
        
        public String getIv() { return iv; }
        public void setIv(String iv) { this.iv = iv; }
        
        public String getCiphertext() { return ciphertext; }
        public void setCiphertext(String ciphertext) { this.ciphertext = ciphertext; }
        
        public String getAlgorithm() { return algorithm; }
        public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
        
        public String getAad() { return aad; }
        public void setAad(String aad) { this.aad = aad; }
    }
    
    public static class RequestMetadata {
        private String method;
        private String uri;
        private long timestamp;
        private String sessionId;
        
        // Getters and setters
        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }
        
        public String getUri() { return uri; }
        public void setUri(String uri) { this.uri = uri; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    }
    
    public static class ResponseMetadata {
        private int statusCode;
        private long timestamp;
        private String sessionId;
        
        // Getters and setters
        public int getStatusCode() { return statusCode; }
        public void setStatusCode(int statusCode) { this.statusCode = statusCode; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    }
    
    public static class SecureRequest {
        private EncryptedPayload encryptedPayload;
        private RequestMetadata metadata;
        
        // Getters and setters
        public EncryptedPayload getEncryptedPayload() { return encryptedPayload; }
        public void setEncryptedPayload(EncryptedPayload encryptedPayload) { this.encryptedPayload = encryptedPayload; }
        
        public RequestMetadata getMetadata() { return metadata; }
        public void setMetadata(RequestMetadata metadata) { this.metadata = metadata; }
    }
    
    public static class SecureResponse {
        private EncryptedPayload encryptedPayload;
        private ResponseMetadata metadata;
        
        // Getters and setters
        public EncryptedPayload getEncryptedPayload() { return encryptedPayload; }
        public void setEncryptedPayload(EncryptedPayload encryptedPayload) { this.encryptedPayload = encryptedPayload; }
        
        public ResponseMetadata getMetadata() { return metadata; }
        public void setMetadata(ResponseMetadata metadata) { this.metadata = metadata; }
    }
}