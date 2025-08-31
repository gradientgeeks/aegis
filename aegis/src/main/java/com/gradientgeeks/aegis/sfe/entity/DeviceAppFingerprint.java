package com.gradientgeeks.aegis.sfe.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Entity representing app fingerprint data for enhanced device identification.
 * Used to detect device reinstalls by comparing installed application patterns.
 */
@Entity
@Table(name = "device_app_fingerprints", indexes = {
    @Index(name = "idx_app_fingerprint_id", columnList = "fingerprintId"),
    @Index(name = "idx_app_fingerprint_hash", columnList = "appHash")
})
public class DeviceAppFingerprint {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(name = "fingerprint_id", nullable = false)
    private Long fingerprintId;
    
    @OneToOne
    @JoinColumn(name = "fingerprint_id", insertable = false, updatable = false)
    private DeviceFingerprint deviceFingerprint;
    
    @NotNull
    @PositiveOrZero
    @Column(name = "total_app_count", nullable = false)
    private Integer totalAppCount;
    
    @NotNull
    @PositiveOrZero
    @Column(name = "user_app_count", nullable = false)
    private Integer userAppCount;
    
    @NotNull
    @PositiveOrZero
    @Column(name = "system_app_count", nullable = false)
    private Integer systemAppCount;
    
    @NotNull
    @Size(max = 64)
    @Column(name = "app_hash", nullable = false)
    private String appHash;
    
    @OneToMany(mappedBy = "appFingerprintId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DeviceAppInfo> appInfoList;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    public DeviceAppFingerprint() {}
    
    public DeviceAppFingerprint(Long fingerprintId, Integer totalAppCount, Integer userAppCount, 
                               Integer systemAppCount, String appHash) {
        this.fingerprintId = fingerprintId;
        this.totalAppCount = totalAppCount;
        this.userAppCount = userAppCount;
        this.systemAppCount = systemAppCount;
        this.appHash = appHash;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getFingerprintId() {
        return fingerprintId;
    }
    
    public void setFingerprintId(Long fingerprintId) {
        this.fingerprintId = fingerprintId;
    }
    
    public Integer getTotalAppCount() {
        return totalAppCount;
    }
    
    public void setTotalAppCount(Integer totalAppCount) {
        this.totalAppCount = totalAppCount;
    }
    
    public Integer getUserAppCount() {
        return userAppCount;
    }
    
    public void setUserAppCount(Integer userAppCount) {
        this.userAppCount = userAppCount;
    }
    
    public Integer getSystemAppCount() {
        return systemAppCount;
    }
    
    public void setSystemAppCount(Integer systemAppCount) {
        this.systemAppCount = systemAppCount;
    }
    
    public String getAppHash() {
        return appHash;
    }
    
    public void setAppHash(String appHash) {
        this.appHash = appHash;
    }
    
    public List<DeviceAppInfo> getAppInfoList() {
        return appInfoList;
    }
    
    public void setAppInfoList(List<DeviceAppInfo> appInfoList) {
        this.appInfoList = appInfoList;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public DeviceFingerprint getDeviceFingerprint() {
        return deviceFingerprint;
    }
    
    public void setDeviceFingerprint(DeviceFingerprint deviceFingerprint) {
        this.deviceFingerprint = deviceFingerprint;
    }
    
    /**
     * Calculates similarity with another app fingerprint based on app counts and package overlap.
     */
    public double calculateSimilarity(DeviceAppFingerprint other) {
        if (other == null) return 0.0;
        
        // App count similarity (30% weight)
        double totalCountSimilarity = 1.0 - ((double) Math.abs(totalAppCount - other.totalAppCount) / 
                                            (double) Math.max(totalAppCount, other.totalAppCount));
        
        double userCountSimilarity = 1.0 - ((double) Math.abs(userAppCount - other.userAppCount) / 
                                           (double) Math.max(userAppCount, other.userAppCount));
        
        // Basic similarity based on counts - detailed package comparison done separately
        return (totalCountSimilarity * 0.4 + userCountSimilarity * 0.6);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceAppFingerprint that = (DeviceAppFingerprint) o;
        return Objects.equals(appHash, that.appHash);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(appHash);
    }
    
    @Override
    public String toString() {
        return "DeviceAppFingerprint{" +
                "id=" + id +
                ", fingerprintId=" + fingerprintId +
                ", totalAppCount=" + totalAppCount +
                ", userAppCount=" + userAppCount +
                ", systemAppCount=" + systemAppCount +
                ", appHash='" + appHash + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}