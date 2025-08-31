package com.gradientgeeks.aegis.sfe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Hardware fingerprint data transfer object.
 */
public class HardwareFingerprintDto {
    
    @NotBlank(message = "Manufacturer is required")
    private String manufacturer;
    
    @NotBlank(message = "Model is required")
    private String model;
    
    @NotBlank(message = "Device is required")
    private String device;
    
    @NotBlank(message = "Board is required")
    private String board;
    
    @NotBlank(message = "Brand is required")
    private String brand;
    
    @NotBlank(message = "CPU architecture is required")
    private String cpuArchitecture;
    
    @Positive(message = "API level must be positive")
    private Integer apiLevel;
    
    @NotBlank(message = "Build fingerprint is required")
    private String buildFingerprint;
    
    @NotBlank(message = "Hardware hash is required")
    private String hash;
    
    public HardwareFingerprintDto() {}
    
    public String getManufacturer() {
        return manufacturer;
    }
    
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public String getDevice() {
        return device;
    }
    
    public void setDevice(String device) {
        this.device = device;
    }
    
    public String getBoard() {
        return board;
    }
    
    public void setBoard(String board) {
        this.board = board;
    }
    
    public String getBrand() {
        return brand;
    }
    
    public void setBrand(String brand) {
        this.brand = brand;
    }
    
    public String getCpuArchitecture() {
        return cpuArchitecture;
    }
    
    public void setCpuArchitecture(String cpuArchitecture) {
        this.cpuArchitecture = cpuArchitecture;
    }
    
    public Integer getApiLevel() {
        return apiLevel;
    }
    
    public void setApiLevel(Integer apiLevel) {
        this.apiLevel = apiLevel;
    }
    
    public String getBuildFingerprint() {
        return buildFingerprint;
    }
    
    public void setBuildFingerprint(String buildFingerprint) {
        this.buildFingerprint = buildFingerprint;
    }
    
    public String getHash() {
        return hash;
    }
    
    public void setHash(String hash) {
        this.hash = hash;
    }
}