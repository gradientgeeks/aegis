package com.gradientgeeks.aegis.sfe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class RegistrationKeyRequest {
    
    @NotBlank(message = "Client ID is required")
    @Size(max = 100, message = "Client ID must not exceed 100 characters")
    private String clientId;
    
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
    
    private LocalDateTime expiresAt;
    
    private String organization;
    
    public RegistrationKeyRequest() {}
    
    public RegistrationKeyRequest(String clientId, String description, LocalDateTime expiresAt) {
        this.clientId = clientId;
        this.description = description;
        this.expiresAt = expiresAt;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public String getOrganization() {
        return organization;
    }
    
    public void setOrganization(String organization) {
        this.organization = organization;
    }
    
    @Override
    public String toString() {
        return "RegistrationKeyRequest{" +
                "clientId='" + clientId + '\'' +
                ", description='" + description + '\'' +
                ", expiresAt=" + expiresAt +
                '}';
    }
}