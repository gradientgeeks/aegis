package com.gradientgeeks.ageis.backendapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for ECDH key exchange.
 */
public class KeyExchangeRequest {
    
    @NotBlank(message = "Client public key is required")
    @JsonProperty("clientPublicKey")
    private String clientPublicKey;
    
    @NotBlank(message = "Session ID is required")
    @JsonProperty("sessionId")
    private String sessionId;
    
    @NotBlank(message = "Algorithm is required")
    @JsonProperty("algorithm")
    private String algorithm = "ECDH-P256";
    
    // Constructors
    public KeyExchangeRequest() {}
    
    public KeyExchangeRequest(String clientPublicKey, String sessionId, String algorithm) {
        this.clientPublicKey = clientPublicKey;
        this.sessionId = sessionId;
        this.algorithm = algorithm;
    }
    
    // Getters and Setters
    public String getClientPublicKey() {
        return clientPublicKey;
    }
    
    public void setClientPublicKey(String clientPublicKey) {
        this.clientPublicKey = clientPublicKey;
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
}