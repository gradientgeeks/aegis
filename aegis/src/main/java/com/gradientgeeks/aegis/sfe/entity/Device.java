package com.gradientgeeks.aegis.sfe.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "devices", indexes = {
    @Index(name = "idx_device_id", columnList = "deviceId"),
    @Index(name = "idx_client_id", columnList = "clientId"),
    @Index(name = "idx_device_status", columnList = "status"),
    @Index(name = "idx_device_client_composite", columnList = "deviceId,clientId", unique = true)
})
@IdClass(DeviceId.class)
public class Device {
    
    public enum DeviceStatus {
        ACTIVE,              // Device is active and can make transactions
        TEMPORARILY_BLOCKED, // Device is temporarily blocked (can be unblocked)
        PERMANENTLY_BLOCKED  // Device is permanently blocked (requires admin review)
    }
    
    @Id
    @NotBlank
    @Size(max = 255)
    @Column(name = "device_id", nullable = false)
    private String deviceId;
    
    @Id
    @NotBlank
    @Size(max = 100)
    @Column(name = "client_id", nullable = false)
    private String clientId;
    
    @NotBlank
    @Size(max = 512)
    @Column(name = "secret_key", nullable = false)
    private String secretKey;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeviceStatus status = DeviceStatus.ACTIVE;
    
    @Column(name = "last_seen")
    private LocalDateTime lastSeen;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public Device() {}
    
    public Device(String deviceId, String clientId, String secretKey) {
        this.deviceId = deviceId;
        this.clientId = clientId;
        this.secretKey = secretKey;
        this.status = DeviceStatus.ACTIVE;
    }
    
    
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    public String getSecretKey() {
        return secretKey;
    }
    
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getLastSeen() {
        return lastSeen;
    }
    
    public void setLastSeen(LocalDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public DeviceStatus getStatus() {
        return status;
    }
    
    public void setStatus(DeviceStatus status) {
        this.status = status;
    }
    
    /**
     * Checks if the device is currently blocked (temporarily or permanently).
     * @return true if device is blocked, false otherwise
     */
    public boolean isBlocked() {
        return status == DeviceStatus.TEMPORARILY_BLOCKED || status == DeviceStatus.PERMANENTLY_BLOCKED;
    }
    
    /**
     * Checks if the device can make transactions.
     * @return true if device is active and not blocked, false otherwise
     */
    public boolean canMakeTransactions() {
        return isActive && status == DeviceStatus.ACTIVE;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Device device = (Device) o;
        return Objects.equals(deviceId, device.deviceId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(deviceId);
    }
    
    @Override
    public String toString() {
        return "Device{" +
                "deviceId='" + deviceId + '\'' +
                ", clientId='" + clientId + '\'' +
                ", isActive=" + isActive +
                ", status=" + status +
                ", lastSeen=" + lastSeen +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}