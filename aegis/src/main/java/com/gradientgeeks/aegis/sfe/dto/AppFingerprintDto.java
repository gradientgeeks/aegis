package com.gradientgeeks.aegis.sfe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

/**
 * App fingerprint data transfer object for API communication.
 * Contains installed application data for enhanced fraud detection.
 */
public class AppFingerprintDto {
    
    @NotNull(message = "User apps list is required")
    private List<AppInfoDto> userApps;
    
    @NotNull(message = "System apps list is required")
    private List<AppInfoDto> systemApps;
    
    @PositiveOrZero(message = "Total app count must be non-negative")
    private Integer totalAppCount;
    
    @PositiveOrZero(message = "User app count must be non-negative")
    private Integer userAppCount;
    
    @PositiveOrZero(message = "System app count must be non-negative")
    private Integer systemAppCount;
    
    @NotBlank(message = "App fingerprint hash is required")
    private String hash;
    
    public AppFingerprintDto() {}
    
    public List<AppInfoDto> getUserApps() {
        return userApps;
    }
    
    public void setUserApps(List<AppInfoDto> userApps) {
        this.userApps = userApps;
    }
    
    public List<AppInfoDto> getSystemApps() {
        return systemApps;
    }
    
    public void setSystemApps(List<AppInfoDto> systemApps) {
        this.systemApps = systemApps;
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
    
    public String getHash() {
        return hash;
    }
    
    public void setHash(String hash) {
        this.hash = hash;
    }
    
    @Override
    public String toString() {
        return "AppFingerprintDto{" +
                "totalAppCount=" + totalAppCount +
                ", userAppCount=" + userAppCount +
                ", systemAppCount=" + systemAppCount +
                ", hash='" + hash + '\'' +
                '}';
    }
}