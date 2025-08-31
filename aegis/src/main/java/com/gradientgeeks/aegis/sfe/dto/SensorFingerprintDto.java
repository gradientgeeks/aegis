package com.gradientgeeks.aegis.sfe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

/**
 * Sensor fingerprint data transfer object.
 */
public class SensorFingerprintDto {
    
    @NotNull(message = "Sensor types list is required")
    private List<Integer> sensorTypes;
    
    @Positive(message = "Sensor count must be positive")
    private Integer sensorCount;
    
    @NotBlank(message = "Sensor hash is required")
    private String hash;
    
    public SensorFingerprintDto() {}
    
    public List<Integer> getSensorTypes() {
        return sensorTypes;
    }
    
    public void setSensorTypes(List<Integer> sensorTypes) {
        this.sensorTypes = sensorTypes;
    }
    
    public Integer getSensorCount() {
        return sensorCount;
    }
    
    public void setSensorCount(Integer sensorCount) {
        this.sensorCount = sensorCount;
    }
    
    public String getHash() {
        return hash;
    }
    
    public void setHash(String hash) {
        this.hash = hash;
    }
}