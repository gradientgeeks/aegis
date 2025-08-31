package com.gradientgeeks.aegis.sfe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class DeviceRegistrationRequest {
    
    @NotBlank(message = "Client ID is required")
    @Size(max = 100, message = "Client ID must not exceed 100 characters")
    private String clientId;
    
    @NotBlank(message = "Registration key is required")
    @Size(max = 512, message = "Registration key must not exceed 512 characters")
    private String registrationKey;
    
    private String integrityToken;
    
    @NotNull(message = "Device fingerprint is required")
    private DeviceFingerprintDto deviceFingerprint;
    
    public DeviceRegistrationRequest() {}
    
    public DeviceRegistrationRequest(String clientId, String registrationKey, String integrityToken, DeviceFingerprintDto deviceFingerprint) {
        this.clientId = clientId;
        this.registrationKey = registrationKey;
        this.integrityToken = integrityToken;
        this.deviceFingerprint = deviceFingerprint;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    public String getRegistrationKey() {
        return registrationKey;
    }
    
    public void setRegistrationKey(String registrationKey) {
        this.registrationKey = registrationKey;
    }
    
    public String getIntegrityToken() {
        return integrityToken;
    }
    
    public void setIntegrityToken(String integrityToken) {
        this.integrityToken = integrityToken;
    }
    
    public DeviceFingerprintDto getDeviceFingerprint() {
        return deviceFingerprint;
    }
    
    public void setDeviceFingerprint(DeviceFingerprintDto deviceFingerprint) {
        this.deviceFingerprint = deviceFingerprint;
    }
    
    @Override
    public String toString() {
        return "DeviceRegistrationRequest{" +
                "clientId='" + clientId + '\'' +
                ", registrationKey='[REDACTED]'" +
                ", integrityToken='" + (integrityToken != null ? "[PRESENT]" : "null") + '\'' +
                ", deviceFingerprint=" + (deviceFingerprint != null ? "[PRESENT]" : "null") +
                '}';
    }
}