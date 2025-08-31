package com.gradientgeeks.ageis.backendapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Map;

/**
 * Request DTO for device rebinding with identity verification.
 */
public class DeviceRebindRequest {
    
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Verification method is required")
    private String verificationMethod; // e.g., "AADHAAR_PAN_SECURITY"
    
    @Size(min = 4, max = 4, message = "Aadhaar last 4 digits must be exactly 4 characters")
    private String aadhaarLast4;
    
    @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]", message = "Invalid PAN format")
    private String panNumber;
    
    private Map<String, String> securityAnswers; // Question ID -> Answer
    
    private String otpCode; // If OTP verification is used
    
    // Getters and Setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getVerificationMethod() {
        return verificationMethod;
    }
    
    public void setVerificationMethod(String verificationMethod) {
        this.verificationMethod = verificationMethod;
    }
    
    public String getAadhaarLast4() {
        return aadhaarLast4;
    }
    
    public void setAadhaarLast4(String aadhaarLast4) {
        this.aadhaarLast4 = aadhaarLast4;
    }
    
    public String getPanNumber() {
        return panNumber;
    }
    
    public void setPanNumber(String panNumber) {
        this.panNumber = panNumber;
    }
    
    public Map<String, String> getSecurityAnswers() {
        return securityAnswers;
    }
    
    public void setSecurityAnswers(Map<String, String> securityAnswers) {
        this.securityAnswers = securityAnswers;
    }
    
    public String getOtpCode() {
        return otpCode;
    }
    
    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }
}