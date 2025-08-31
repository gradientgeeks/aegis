package com.gradientgeeks.aegis.sfe.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing individual app information for device fingerprinting.
 * Stores package names and install times for fraud detection.
 */
@Entity
@Table(name = "device_app_info", indexes = {
    @Index(name = "idx_app_info_fingerprint_id", columnList = "appFingerprintId"),
    @Index(name = "idx_app_info_package_name", columnList = "packageName")
})
public class DeviceAppInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(name = "app_fingerprint_id", nullable = false)
    private Long appFingerprintId;
    
    @NotBlank
    @Size(max = 255)
    @Column(name = "package_name", nullable = false)
    private String packageName;
    
    @NotNull
    @Positive
    @Column(name = "first_install_time", nullable = false)
    private Long firstInstallTime;
    
    @NotNull
    @Positive
    @Column(name = "last_update_time", nullable = false)
    private Long lastUpdateTime;
    
    @NotNull
    @Column(name = "is_system_app", nullable = false)
    private Boolean isSystemApp;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    public DeviceAppInfo() {}
    
    public DeviceAppInfo(Long appFingerprintId, String packageName, Long firstInstallTime, 
                        Long lastUpdateTime, Boolean isSystemApp) {
        this.appFingerprintId = appFingerprintId;
        this.packageName = packageName;
        this.firstInstallTime = firstInstallTime;
        this.lastUpdateTime = lastUpdateTime;
        this.isSystemApp = isSystemApp;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getAppFingerprintId() {
        return appFingerprintId;
    }
    
    public void setAppFingerprintId(Long appFingerprintId) {
        this.appFingerprintId = appFingerprintId;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    
    public Long getFirstInstallTime() {
        return firstInstallTime;
    }
    
    public void setFirstInstallTime(Long firstInstallTime) {
        this.firstInstallTime = firstInstallTime;
    }
    
    public Long getLastUpdateTime() {
        return lastUpdateTime;
    }
    
    public void setLastUpdateTime(Long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
    
    public Boolean getIsSystemApp() {
        return isSystemApp;
    }
    
    public void setIsSystemApp(Boolean isSystemApp) {
        this.isSystemApp = isSystemApp;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceAppInfo that = (DeviceAppInfo) o;
        return Objects.equals(packageName, that.packageName) &&
               Objects.equals(appFingerprintId, that.appFingerprintId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(appFingerprintId, packageName);
    }
    
    @Override
    public String toString() {
        return "DeviceAppInfo{" +
                "id=" + id +
                ", appFingerprintId=" + appFingerprintId +
                ", packageName='" + packageName + '\'' +
                ", firstInstallTime=" + firstInstallTime +
                ", lastUpdateTime=" + lastUpdateTime +
                ", isSystemApp=" + isSystemApp +
                ", createdAt=" + createdAt +
                '}';
    }
}