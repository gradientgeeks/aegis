package com.gradientgeeks.aegis.sfe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

/**
 * Device fingerprint data transfer object for API communication.
 * Contains stable hardware characteristics used for fraud detection.
 */
public class DeviceFingerprintDto {
    
    @NotBlank(message = "Fingerprint version is required")
    private String version;
    
    @NotBlank(message = "Composite hash is required")
    private String compositeHash;
    
    @NotNull(message = "Hardware fingerprint is required")
    private HardwareFingerprintDto hardware;
    
    @NotNull(message = "Display fingerprint is required")
    private DisplayFingerprintDto display;
    
    @NotNull(message = "Sensor fingerprint is required")
    private SensorFingerprintDto sensors;
    
    @NotNull(message = "Network fingerprint is required")
    private NetworkFingerprintDto network;
    
    // Optional app fingerprint for enhanced fraud detection (only when similarity threshold met)
    private AppFingerprintDto apps;
    
    @Positive(message = "Timestamp must be positive")
    private Long timestamp;
    
    public DeviceFingerprintDto() {}
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getCompositeHash() {
        return compositeHash;
    }
    
    public void setCompositeHash(String compositeHash) {
        this.compositeHash = compositeHash;
    }
    
    public HardwareFingerprintDto getHardware() {
        return hardware;
    }
    
    public void setHardware(HardwareFingerprintDto hardware) {
        this.hardware = hardware;
    }
    
    public DisplayFingerprintDto getDisplay() {
        return display;
    }
    
    public void setDisplay(DisplayFingerprintDto display) {
        this.display = display;
    }
    
    public SensorFingerprintDto getSensors() {
        return sensors;
    }
    
    public void setSensors(SensorFingerprintDto sensors) {
        this.sensors = sensors;
    }
    
    public NetworkFingerprintDto getNetwork() {
        return network;
    }
    
    public void setNetwork(NetworkFingerprintDto network) {
        this.network = network;
    }
    
    public AppFingerprintDto getApps() {
        return apps;
    }
    
    public void setApps(AppFingerprintDto apps) {
        this.apps = apps;
    }
    
    public Long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "DeviceFingerprintDto{" +
                "version='" + version + '\'' +
                ", compositeHash='" + compositeHash + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}