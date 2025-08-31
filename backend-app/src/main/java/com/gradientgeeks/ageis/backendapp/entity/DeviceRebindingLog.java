package com.gradientgeeks.ageis.backendapp.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "device_rebinding_log")
@EntityListeners(AuditingEntityListener.class)
public class DeviceRebindingLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "old_device_id")
    private String oldDeviceId;
    
    @Column(name = "new_device_id", nullable = false)
    private String newDeviceId;
    
    @Column(name = "verification_method")
    private String verificationMethod;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "success", nullable = false)
    private Boolean success = false;
    
    @Column(name = "failure_reason")
    private String failureReason;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public DeviceRebindingLog() {}
    
    public DeviceRebindingLog(User user, String oldDeviceId, String newDeviceId, 
                             String verificationMethod, Boolean success) {
        this.user = user;
        this.oldDeviceId = oldDeviceId;
        this.newDeviceId = newDeviceId;
        this.verificationMethod = verificationMethod;
        this.success = success;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getOldDeviceId() {
        return oldDeviceId;
    }
    
    public void setOldDeviceId(String oldDeviceId) {
        this.oldDeviceId = oldDeviceId;
    }
    
    public String getNewDeviceId() {
        return newDeviceId;
    }
    
    public void setNewDeviceId(String newDeviceId) {
        this.newDeviceId = newDeviceId;
    }
    
    public String getVerificationMethod() {
        return verificationMethod;
    }
    
    public void setVerificationMethod(String verificationMethod) {
        this.verificationMethod = verificationMethod;
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
    
    public Boolean getSuccess() {
        return success;
    }
    
    public void setSuccess(Boolean success) {
        this.success = success;
    }
    
    public String getFailureReason() {
        return failureReason;
    }
    
    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}