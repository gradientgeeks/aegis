package com.gradientgeeks.aegis.sfe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * App information data transfer object for API communication.
 * Contains details about an installed application.
 */
public class AppInfoDto {
    
    @NotBlank(message = "Package name is required")
    private String packageName;
    
    @Positive(message = "First install time must be positive")
    private Long firstInstallTime;
    
    @Positive(message = "Last update time must be positive")
    private Long lastUpdateTime;
    
    @NotNull(message = "System app flag is required")
    private Boolean isSystemApp;
    
    public AppInfoDto() {}
    
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
    
    @Override
    public String toString() {
        return "AppInfoDto{" +
                "packageName='" + packageName + '\'' +
                ", firstInstallTime=" + firstInstallTime +
                ", lastUpdateTime=" + lastUpdateTime +
                ", isSystemApp=" + isSystemApp +
                '}';
    }
}