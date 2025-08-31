package com.gradientgeeks.aegis.sfe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;

public class SignatureValidationRequest {
    
    @NotBlank(message = "Device ID is required")
    @Size(max = 255, message = "Device ID must not exceed 255 characters")
    private String deviceId;
    
    @NotBlank(message = "Signature is required")
    @Size(max = 512, message = "Signature must not exceed 512 characters")
    private String signature;
    
    @NotBlank(message = "String to sign is required")
    @Size(max = 2048, message = "String to sign must not exceed 2048 characters")
    private String stringToSign;
    
    // Additional context for validation
    private String ipAddress;
    private String userAgent;
    
    // Client ID for multi-bank support
    private String clientId;
    
    // User metadata for policy enforcement
    private Map<String, Object> userMetadata;
    
    public SignatureValidationRequest() {}
    
    public SignatureValidationRequest(String deviceId, String signature, String stringToSign) {
        this.deviceId = deviceId;
        this.signature = signature;
        this.stringToSign = stringToSign;
    }
    
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    
    public String getSignature() {
        return signature;
    }
    
    public void setSignature(String signature) {
        this.signature = signature;
    }
    
    public String getStringToSign() {
        return stringToSign;
    }
    
    public void setStringToSign(String stringToSign) {
        this.stringToSign = stringToSign;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    public Map<String, Object> getUserMetadata() {
        return userMetadata;
    }
    
    public void setUserMetadata(Map<String, Object> userMetadata) {
        this.userMetadata = userMetadata;
    }
    
    @Override
    public String toString() {
        return "SignatureValidationRequest{" +
                "deviceId='" + deviceId + '\'' +
                ", signature='[REDACTED]'" +
                ", stringToSign='" + stringToSign + '\'' +
                ", clientId='" + clientId + '\'' +
                ", userMetadata=" + (userMetadata != null ? "[METADATA_PROVIDED]" : "[NO_METADATA]") +
                '}';
    }
}