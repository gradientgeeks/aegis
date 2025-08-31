package com.gradientgeeks.ageis.backendapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank
    @Column(nullable = false)
    private String passwordHash;

    @NotBlank
    @Email
    @Size(max = 100)
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String fullName;

    @Size(max = 20)
    private String phoneNumber;

    @Column(nullable = false)
    private Boolean isActive = true;
    
    // Device binding fields
    @Size(max = 255)
    @Column(name = "bound_device_id")
    private String boundDeviceId;
    
    @Column(name = "device_binding_timestamp")
    private LocalDateTime deviceBindingTimestamp;
    
    @Column(name = "requires_device_rebinding")
    private Boolean requiresDeviceRebinding = false;
    
    // Multiple device support
    @ElementCollection
    @CollectionTable(name = "user_devices", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "device_id")
    private List<String> deviceIds = new ArrayList<>();
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public User() {
    }

    public User(String username, String passwordHash, String email, String fullName) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.fullName = fullName;
        this.isActive = true;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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
    
    public String getBoundDeviceId() {
        return boundDeviceId;
    }
    
    public void setBoundDeviceId(String boundDeviceId) {
        this.boundDeviceId = boundDeviceId;
    }
    
    public LocalDateTime getDeviceBindingTimestamp() {
        return deviceBindingTimestamp;
    }
    
    public void setDeviceBindingTimestamp(LocalDateTime deviceBindingTimestamp) {
        this.deviceBindingTimestamp = deviceBindingTimestamp;
    }
    
    public Boolean getRequiresDeviceRebinding() {
        return requiresDeviceRebinding;
    }
    
    public void setRequiresDeviceRebinding(Boolean requiresDeviceRebinding) {
        this.requiresDeviceRebinding = requiresDeviceRebinding;
    }
    
    public List<String> getDeviceIds() {
        return deviceIds;
    }
    
    public void setDeviceIds(List<String> deviceIds) {
        this.deviceIds = deviceIds;
    }
    
    public LocalDateTime getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    // Helper method to get name (alias for fullName)
    public String getName() {
        return fullName;
    }
    
    /**
     * Checks if the user is bound to a specific device.
     * @return true if user has a bound device, false otherwise
     */
    public boolean hasDeviceBinding() {
        return boundDeviceId != null && !boundDeviceId.trim().isEmpty();
    }
    
    /**
     * Checks if the device ID matches the user's bound device.
     * @param deviceId The device ID to check
     * @return true if device matches bound device, false otherwise
     */
    public boolean isDeviceBound(String deviceId) {
        return hasDeviceBinding() && boundDeviceId.equals(deviceId);
    }
    
    /**
     * Binds the user to a specific device.
     * @param deviceId The device ID to bind to
     */
    public void bindToDevice(String deviceId) {
        this.boundDeviceId = deviceId;
        this.deviceBindingTimestamp = LocalDateTime.now();
        this.requiresDeviceRebinding = false;
    }
    
    /**
     * Marks the user as requiring device rebinding.
     * This is used when the user needs to verify their identity on a new device.
     */
    public void requireDeviceRebinding() {
        this.requiresDeviceRebinding = true;
    }
}