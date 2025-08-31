package com.gradientgeeks.aegis.sfe.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Entity representing a device fingerprint for fraud detection.
 * Stores stable hardware characteristics that persist across factory resets.
 */
@Entity
@Table(name = "device_fingerprints", indexes = {
    @Index(name = "idx_fingerprint_hash", columnList = "compositeHash"),
    @Index(name = "idx_device_id", columnList = "deviceId"),
    @Index(name = "idx_hardware_hash", columnList = "hardwareHash"),
    @Index(name = "idx_display_hash", columnList = "displayHash"),
    @Index(name = "idx_fraud_status", columnList = "isFraudulent")
})
public class DeviceFingerprint {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 255)
    @Column(name = "device_id", nullable = false)
    private String deviceId;
    
    @NotBlank
    @Size(max = 50)
    @Column(name = "version", nullable = false)
    private String version;
    
    @NotBlank
    @Size(max = 64)
    @Column(name = "composite_hash", nullable = false)
    private String compositeHash;
    
    // Hardware characteristics
    @NotBlank
    @Size(max = 100)
    @Column(name = "manufacturer", nullable = false)
    private String manufacturer;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "model", nullable = false)
    private String model;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "device_name", nullable = false)
    private String deviceName;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "board", nullable = false)
    private String board;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "brand", nullable = false)
    private String brand;
    
    @NotBlank
    @Size(max = 200)
    @Column(name = "cpu_architecture", nullable = false)
    private String cpuArchitecture;
    
    @NotNull
    @Column(name = "api_level", nullable = false)
    private Integer apiLevel;
    
    @NotBlank
    @Size(max = 64)
    @Column(name = "hardware_hash", nullable = false)
    private String hardwareHash;
    
    // Display characteristics
    @NotNull
    @Column(name = "width_pixels", nullable = false)
    private Integer widthPixels;
    
    @NotNull
    @Column(name = "height_pixels", nullable = false)
    private Integer heightPixels;
    
    @NotNull
    @Column(name = "density_dpi", nullable = false)
    private Integer densityDpi;
    
    @NotBlank
    @Size(max = 64)
    @Column(name = "display_hash", nullable = false)
    private String displayHash;
    
    // Sensor information
    @ElementCollection
    @CollectionTable(name = "device_fingerprint_sensors", joinColumns = @JoinColumn(name = "fingerprint_id"))
    @Column(name = "sensor_type")
    private List<Integer> sensorTypes;
    
    @NotNull
    @Column(name = "sensor_count", nullable = false)
    private Integer sensorCount;
    
    @NotBlank
    @Size(max = 64)
    @Column(name = "sensor_hash", nullable = false)
    private String sensorHash;
    
    // Network characteristics
    @Size(max = 100)
    @Column(name = "network_country_iso")
    private String networkCountryIso;
    
    @Size(max = 100)
    @Column(name = "sim_country_iso")
    private String simCountryIso;
    
    @Column(name = "phone_type")
    private Integer phoneType;
    
    @NotBlank
    @Size(max = 64)
    @Column(name = "network_hash", nullable = false)
    private String networkHash;
    
    // Fraud detection
    @Column(name = "is_fraudulent", nullable = false)
    private Boolean isFraudulent = false;
    
    @Column(name = "fraud_reported_at")
    private LocalDateTime fraudReportedAt;
    
    @Size(max = 500)
    @Column(name = "fraud_reason")
    private String fraudReason;
    
    @NotNull
    @Column(name = "fingerprint_timestamp", nullable = false)
    private LocalDateTime fingerprintTimestamp;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // App fingerprint relationship
    @OneToOne(mappedBy = "deviceFingerprint", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private DeviceAppFingerprint appFingerprint;
    
    public DeviceFingerprint() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    
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
    
    public String getDeviceName() {
        return deviceName;
    }
    
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
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
    
    public String getHardwareHash() {
        return hardwareHash;
    }
    
    public void setHardwareHash(String hardwareHash) {
        this.hardwareHash = hardwareHash;
    }
    
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
    
    public String getDisplayHash() {
        return displayHash;
    }
    
    public void setDisplayHash(String displayHash) {
        this.displayHash = displayHash;
    }
    
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
    
    public String getSensorHash() {
        return sensorHash;
    }
    
    public void setSensorHash(String sensorHash) {
        this.sensorHash = sensorHash;
    }
    
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
    
    public String getNetworkHash() {
        return networkHash;
    }
    
    public void setNetworkHash(String networkHash) {
        this.networkHash = networkHash;
    }
    
    public Boolean getIsFraudulent() {
        return isFraudulent;
    }
    
    public void setIsFraudulent(Boolean isFraudulent) {
        this.isFraudulent = isFraudulent;
    }
    
    public LocalDateTime getFraudReportedAt() {
        return fraudReportedAt;
    }
    
    public void setFraudReportedAt(LocalDateTime fraudReportedAt) {
        this.fraudReportedAt = fraudReportedAt;
    }
    
    public String getFraudReason() {
        return fraudReason;
    }
    
    public void setFraudReason(String fraudReason) {
        this.fraudReason = fraudReason;
    }
    
    public LocalDateTime getFingerprintTimestamp() {
        return fingerprintTimestamp;
    }
    
    public void setFingerprintTimestamp(LocalDateTime fingerprintTimestamp) {
        this.fingerprintTimestamp = fingerprintTimestamp;
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
    
    public DeviceAppFingerprint getAppFingerprint() {
        return appFingerprint;
    }
    
    public void setAppFingerprint(DeviceAppFingerprint appFingerprint) {
        this.appFingerprint = appFingerprint;
    }
    
    /**
     * Check if this device fingerprint has associated app data.
     * 
     * @return true if app fingerprint data is available
     */
    public boolean hasAppFingerprint() {
        return appFingerprint != null;
    }
    
    /**
     * Marks this fingerprint as fraudulent.
     * 
     * @param reason The reason for marking as fraudulent
     */
    public void markAsFraudulent(String reason) {
        this.isFraudulent = true;
        this.fraudReportedAt = LocalDateTime.now();
        this.fraudReason = reason;
    }
    
    /**
     * Calculates hardware similarity with another fingerprint.
     * 
     * @param other The other fingerprint to compare with
     * @return Similarity score between 0.0 and 1.0
     */
    public double calculateHardwareSimilarity(DeviceFingerprint other) {
        if (other == null) return 0.0;
        
        int matches = 0;
        int total = 0;
        
        // Core hardware characteristics (weighted)
        if (Objects.equals(manufacturer, other.manufacturer)) matches += 3;
        total += 3;
        
        if (Objects.equals(model, other.model)) matches += 3;
        total += 3;
        
        if (Objects.equals(deviceName, other.deviceName)) matches += 2;
        total += 2;
        
        if (Objects.equals(board, other.board)) matches += 2;
        total += 2;
        
        if (Objects.equals(cpuArchitecture, other.cpuArchitecture)) matches += 2;
        total += 2;
        
        if (Objects.equals(brand, other.brand)) matches += 1;
        total += 1;
        
        return (double) matches / total;
    }
    
    /**
     * Calculates display similarity with another fingerprint.
     * 
     * @param other The other fingerprint to compare with
     * @return 1.0 if display characteristics match exactly, 0.0 otherwise
     */
    public double calculateDisplaySimilarity(DeviceFingerprint other) {
        if (other == null) return 0.0;
        
        return Objects.equals(widthPixels, other.widthPixels) &&
               Objects.equals(heightPixels, other.heightPixels) &&
               Objects.equals(densityDpi, other.densityDpi) ? 1.0 : 0.0;
    }
    
    /**
     * Calculates overall similarity with another fingerprint.
     * 
     * @param other The other fingerprint to compare with
     * @return Weighted similarity score between 0.0 and 1.0
     */
    public double calculateOverallSimilarity(DeviceFingerprint other) {
        if (other == null) return 0.0;
        
        double hardwareSimilarity = calculateHardwareSimilarity(other);
        double displaySimilarity = calculateDisplaySimilarity(other);
        
        // Weighted average: hardware is most important for fraud detection
        return (hardwareSimilarity * 0.7) + (displaySimilarity * 0.3);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceFingerprint that = (DeviceFingerprint) o;
        return Objects.equals(compositeHash, that.compositeHash);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(compositeHash);
    }
    
    @Override
    public String toString() {
        return "DeviceFingerprint{" +
                "id=" + id +
                ", deviceId='" + deviceId + '\'' +
                ", version='" + version + '\'' +
                ", compositeHash='" + compositeHash + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", model='" + model + '\'' +
                ", isFraudulent=" + isFraudulent +
                ", createdAt=" + createdAt +
                '}';
    }
}