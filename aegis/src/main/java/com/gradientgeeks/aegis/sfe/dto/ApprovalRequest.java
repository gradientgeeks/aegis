package com.gradientgeeks.aegis.sfe.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for organization approval/rejection request
 */
public class ApprovalRequest {
    
    @NotBlank(message = "Approved by is required")
    private String approvedBy;
    
    private String reason; // Optional reason for rejection
    
    // Constructors
    public ApprovalRequest() {}
    
    public ApprovalRequest(String approvedBy) {
        this.approvedBy = approvedBy;
    }
    
    public ApprovalRequest(String approvedBy, String reason) {
        this.approvedBy = approvedBy;
        this.reason = reason;
    }
    
    // Getters and Setters
    public String getApprovedBy() {
        return approvedBy;
    }
    
    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
}