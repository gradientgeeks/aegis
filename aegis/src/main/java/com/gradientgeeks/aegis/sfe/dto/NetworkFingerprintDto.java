package com.gradientgeeks.aegis.sfe.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Network fingerprint data transfer object.
 */
public class NetworkFingerprintDto {
    
    @NotBlank(message = "Network country ISO is required")
    private String networkCountryIso;
    
    @NotBlank(message = "SIM country ISO is required")
    private String simCountryIso;
    
    private Integer phoneType;
    
    @NotBlank(message = "Network hash is required")
    private String hash;
    
    public NetworkFingerprintDto() {}
    
    public String getNetworkCountryIso() {
        return networkCountryIso;
    }
    
    public void setNetworkCountryIso(String networkCountryIso) {
        this.networkCountryIso = networkCountryIso;
    }
    
    public String getSimCountryIso() {
        return simCountryIso;
    }
    
    public void setSimCountryIso(String simCountryIso) {
        this.simCountryIso = simCountryIso;
    }
    
    public Integer getPhoneType() {
        return phoneType;
    }
    
    public void setPhoneType(Integer phoneType) {
        this.phoneType = phoneType;
    }
    
    public String getHash() {
        return hash;
    }
    
    public void setHash(String hash) {
        this.hash = hash;
    }
}