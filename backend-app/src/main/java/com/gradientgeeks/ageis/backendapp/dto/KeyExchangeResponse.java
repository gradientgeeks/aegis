package com.gradientgeeks.ageis.backendapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for ECDH key exchange.
 */
public class KeyExchangeResponse {
    
    @JsonProperty("serverPublicKey")
    private String serverPublicKey;
    
    @JsonProperty("sessionId")
    private String sessionId;
    
    @JsonProperty("algorithm")
    private String algorithm;
    
    @JsonProperty("expiresAt")
    private long expiresAt;
    
    // Constructors
    public KeyExchangeResponse() {}
    
    public KeyExchangeResponse(String serverPublicKey, String sessionId, String algorithm, long expiresAt) {
        this.serverPublicKey = serverPublicKey;
        this.sessionId = sessionId;
        this.algorithm = algorithm;
        this.expiresAt = expiresAt;
    }
    
    // Getters and Setters
    public String getServerPublicKey() {
        return serverPublicKey;
    }
    
    public void setServerPublicKey(String serverPublicKey) {
        this.serverPublicKey = serverPublicKey;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getAlgorithm() {
        return algorithm;
    }
    
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
    
    public long getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }
}