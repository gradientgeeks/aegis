package com.gradientgeeks.ageis.backendapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for reporting fraud to the Aegis Security API.
 * Used by banks to report suspicious devices that should be blocked.
 */
public class FraudReportRequest {
    
    @NotBlank(message = "Device ID cannot be blank")
    @Size(max = 255, message = "Device ID too long")
    @JsonProperty("deviceId")
    private String deviceId;
    
    @NotBlank(message = "Bank transaction ID cannot be blank")
    @Size(max = 255, message = "Bank transaction ID too long")
    @JsonProperty("bankTransactionId")
    private String bankTransactionId;
    
    @NotBlank(message = "Reason code cannot be blank")
    @Size(max = 100, message = "Reason code too long")
    @JsonProperty("reasonCode")
    private String reasonCode;
    
    @Size(max = 1000, message = "Description too long")
    @JsonProperty("description")
    private String description;
    
    public FraudReportRequest() {}
    
    public FraudReportRequest(String deviceId, String bankTransactionId, 
                            String reasonCode, String description) {
        this.deviceId = deviceId;
        this.bankTransactionId = bankTransactionId;
        this.reasonCode = reasonCode;
        this.description = description;
    }
    
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    
    public String getBankTransactionId() {
        return bankTransactionId;
    }
    
    public void setBankTransactionId(String bankTransactionId) {
        this.bankTransactionId = bankTransactionId;
    }
    
    public String getReasonCode() {
        return reasonCode;
    }
    
    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        return "FraudReportRequest{" +
                "deviceId='" + deviceId + '\'' +
                ", bankTransactionId='" + bankTransactionId + '\'' +
                ", reasonCode='" + reasonCode + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}