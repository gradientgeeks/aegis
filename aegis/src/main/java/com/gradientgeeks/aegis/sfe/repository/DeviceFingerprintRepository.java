package com.gradientgeeks.aegis.sfe.repository;

import com.gradientgeeks.aegis.sfe.entity.DeviceFingerprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

/**
 * Repository for DeviceFingerprint entity operations.
 * Provides methods for fraud detection and fingerprint comparison.
 */
@Repository
public interface DeviceFingerprintRepository extends JpaRepository<DeviceFingerprint, Long> {
    
    /**
     * Find fingerprint by device ID.
     * 
     * @param deviceId The device identifier
     * @return Optional DeviceFingerprint
     */
    Optional<DeviceFingerprint> findByDeviceId(String deviceId);
    
    /**
     * Find fingerprint by composite hash.
     * 
     * @param compositeHash The composite fingerprint hash
     * @return Optional DeviceFingerprint
     */
    Optional<DeviceFingerprint> findByCompositeHash(String compositeHash);
    
    /**
     * Find fingerprint by hardware hash.
     * 
     * @param hardwareHash The hardware fingerprint hash
     * @return Optional DeviceFingerprint
     */
    Optional<DeviceFingerprint> findByHardwareHash(String hardwareHash);
    
    /**
     * Find all fraudulent fingerprints.
     * 
     * @return List of fraudulent fingerprints
     */
    @Query("SELECT df FROM DeviceFingerprint df WHERE df.isFraudulent = true")
    List<DeviceFingerprint> findAllFraudulent();
    
    /**
     * Find fingerprints with similar hardware characteristics.
     * Used for probabilistic fraud detection.
     * 
     * @param manufacturer Device manufacturer
     * @param model Device model
     * @param board Device board
     * @param cpuArchitecture CPU architecture
     * @return List of similar fingerprints
     */
    @Query("SELECT df FROM DeviceFingerprint df WHERE " +
           "df.manufacturer = :manufacturer AND " +
           "df.model = :model AND " +
           "df.board = :board AND " +
           "df.cpuArchitecture = :cpuArchitecture")
    List<DeviceFingerprint> findSimilarHardwareFingerprints(
        @Param("manufacturer") String manufacturer,
        @Param("model") String model,
        @Param("board") String board,
        @Param("cpuArchitecture") String cpuArchitecture
    );
    
    /**
     * Find fingerprints with exact hardware and display match.
     * 
     * @param hardwareHash Hash of hardware characteristics
     * @param displayHash Hash of display characteristics
     * @return List of matching fingerprints
     */
    @Query("SELECT df FROM DeviceFingerprint df WHERE " +
           "df.hardwareHash = :hardwareHash AND " +
           "df.displayHash = :displayHash")
    List<DeviceFingerprint> findByHardwareAndDisplayHash(
        @Param("hardwareHash") String hardwareHash,
        @Param("displayHash") String displayHash
    );
    
    /**
     * Find fraudulent fingerprints with similar hardware characteristics.
     * 
     * @param manufacturer Device manufacturer
     * @param model Device model
     * @param board Device board
     * @return List of fraudulent fingerprints with similar hardware
     */
    @Query("SELECT df FROM DeviceFingerprint df WHERE " +
           "df.isFraudulent = true AND " +
           "df.manufacturer = :manufacturer AND " +
           "df.model = :model AND " +
           "df.board = :board")
    List<DeviceFingerprint> findFraudulentSimilarHardware(
        @Param("manufacturer") String manufacturer,
        @Param("model") String model,
        @Param("board") String board
    );
    
    /**
     * Mark a fingerprint as fraudulent.
     * 
     * @param deviceId The device identifier
     * @param reason Reason for marking as fraudulent
     */
    @Modifying
    @Query("UPDATE DeviceFingerprint df SET " +
           "df.isFraudulent = true, " +
           "df.fraudReportedAt = :reportedAt, " +
           "df.fraudReason = :reason " +
           "WHERE df.deviceId = :deviceId")
    void markAsFraudulent(
        @Param("deviceId") String deviceId,
        @Param("reason") String reason,
        @Param("reportedAt") LocalDateTime reportedAt
    );
    
    /**
     * Check if a composite hash exists in fraudulent fingerprints.
     * 
     * @param compositeHash The composite fingerprint hash
     * @return true if hash exists in fraudulent records
     */
    @Query("SELECT COUNT(df) > 0 FROM DeviceFingerprint df WHERE " +
           "df.compositeHash = :compositeHash AND " +
           "df.isFraudulent = true")
    boolean existsFraudulentByCompositeHash(@Param("compositeHash") String compositeHash);
    
    /**
     * Count fraudulent fingerprints with similar hardware.
     * 
     * @param manufacturer Device manufacturer
     * @param model Device model
     * @return Count of fraudulent fingerprints with similar hardware
     */
    @Query("SELECT COUNT(df) FROM DeviceFingerprint df WHERE " +
           "df.isFraudulent = true AND " +
           "df.manufacturer = :manufacturer AND " +
           "df.model = :model")
    long countFraudulentByManufacturerAndModel(
        @Param("manufacturer") String manufacturer,
        @Param("model") String model
    );
    
    /**
     * Find recent fingerprints (within specified days) for pattern analysis.
     * 
     * @param since Date threshold
     * @return List of recent fingerprints
     */
    @Query("SELECT df FROM DeviceFingerprint df WHERE df.createdAt >= :since")
    List<DeviceFingerprint> findRecentFingerprints(@Param("since") LocalDateTime since);
    
    /**
     * Check if device ID already exists.
     * 
     * @param deviceId The device identifier
     * @return true if device fingerprint exists
     */
    boolean existsByDeviceId(String deviceId);
    
    /**
     * Count fraudulent devices
     */
    long countByIsFraudulent(boolean isFraudulent);
    
    /**
     * Count fraudulent devices reported after a specific date
     */
    long countByIsFraudulentAndFraudReportedAtAfter(boolean isFraudulent, LocalDateTime after);
    
    /**
     * Find recent fraudulent devices ordered by report date
     */
    @Query("SELECT df FROM DeviceFingerprint df WHERE df.isFraudulent = :isFraudulent ORDER BY df.fraudReportedAt DESC")
    List<DeviceFingerprint> findByIsFraudulentOrderByFraudReportedAtDesc(@Param("isFraudulent") boolean isFraudulent, Pageable pageable);
    
    /**
     * Find device fingerprints that have associated app fingerprints.
     * Used for comprehensive similarity comparison including app data.
     * 
     * @param manufacturer Device manufacturer
     * @param model Device model
     * @param board Device board
     * @return List of device fingerprints with app data
     */
    @Query("SELECT df FROM DeviceFingerprint df WHERE " +
           "df.manufacturer = :manufacturer AND " +
           "df.model = :model AND " +
           "df.board = :board AND " +
           "EXISTS (SELECT daf FROM DeviceAppFingerprint daf WHERE daf.fingerprintId = df.id)")
    List<DeviceFingerprint> findWithAppFingerprintsByHardware(
        @Param("manufacturer") String manufacturer,
        @Param("model") String model,
        @Param("board") String board
    );
    
    /**
     * Find device fingerprints with app data for detailed comparison.
     * 
     * @param hardwareHash Hash of hardware characteristics
     * @return List of device fingerprints that have app fingerprints
     */
    @Query("SELECT df FROM DeviceFingerprint df WHERE " +
           "df.hardwareHash = :hardwareHash AND " +
           "EXISTS (SELECT daf FROM DeviceAppFingerprint daf WHERE daf.fingerprintId = df.id)")
    List<DeviceFingerprint> findWithAppFingerprintsByHardwareHash(@Param("hardwareHash") String hardwareHash);
}