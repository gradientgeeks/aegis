package com.gradientgeeks.aegis.sfe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Display fingerprint data transfer object.
 */
public class DisplayFingerprintDto {
    
    @Positive(message = "Width pixels must be positive")
    private Integer widthPixels;
    
    @Positive(message = "Height pixels must be positive")
    private Integer heightPixels;
    
    @Positive(message = "Density DPI must be positive")
    private Integer densityDpi;
    
    @NotBlank(message = "Display hash is required")
    private String hash;
    
    public DisplayFingerprintDto() {}
    
    public Integer getWidthPixels() {
        return widthPixels;
    }
    
    public void setWidthPixels(Integer widthPixels) {
        this.widthPixels = widthPixels;
    }
    
    public Integer getHeightPixels() {
        return heightPixels;
    }
    
    public void setHeightPixels(Integer heightPixels) {
        this.heightPixels = heightPixels;
    }
    
    public Integer getDensityDpi() {
        return densityDpi;
    }
    
    public void setDensityDpi(Integer densityDpi) {
        this.densityDpi = densityDpi;
    }
    
    public String getHash() {
        return hash;
    }
    
    public void setHash(String hash) {
        this.hash = hash;
    }
}