package com.gradientgeeks.aegis.sfe.repository;

import com.gradientgeeks.aegis.sfe.entity.DeviceAppFingerprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for DeviceAppFingerprint entity operations.
 * Provides methods for app fingerprint comparison and device reinstall detection.
 */
@Repository
public interface DeviceAppFingerprintRepository extends JpaRepository<DeviceAppFingerprint, Long> {
    
    /**
     * Find app fingerprint by device fingerprint ID.
     * 
     * @param fingerprintId The device fingerprint ID
     * @return Optional DeviceAppFingerprint
     */
    Optional<DeviceAppFingerprint> findByFingerprintId(Long fingerprintId);
    
    /**
     * Find app fingerprint by app hash.
     * 
     * @param appHash The app fingerprint hash
     * @return Optional DeviceAppFingerprint
     */
    Optional<DeviceAppFingerprint> findByAppHash(String appHash);
    
    /**
     * Find app fingerprints with similar app counts for comparison.
     * Used to identify potential device reinstalls.
     * 
     * @param totalAppCount Total app count
     * @param userAppCount User app count
     * @param tolerance Count tolerance for similarity
     * @return List of similar app fingerprints
     */
    @Query("SELECT daf FROM DeviceAppFingerprint daf WHERE " +
           "ABS(daf.totalAppCount - :totalAppCount) <= :tolerance AND " +
           "ABS(daf.userAppCount - :userAppCount) <= :tolerance")
    List<DeviceAppFingerprint> findSimilarByAppCounts(
        @Param("totalAppCount") Integer totalAppCount,
        @Param("userAppCount") Integer userAppCount,
        @Param("tolerance") Integer tolerance
    );
    
    /**
     * Find app fingerprints for similarity comparison by device hardware.
     * This helps identify the same device after factory reset or app reinstall.
     * 
     * @param manufacturer Device manufacturer
     * @param model Device model
     * @param board Device board
     * @return List of app fingerprints for devices with similar hardware
     */
    @Query("SELECT daf FROM DeviceAppFingerprint daf " +
           "JOIN DeviceFingerprint df ON daf.fingerprintId = df.id " +
           "WHERE df.manufacturer = :manufacturer " +
           "AND df.model = :model " +
           "AND df.board = :board")
    List<DeviceAppFingerprint> findByHardwareCharacteristics(
        @Param("manufacturer") String manufacturer,
        @Param("model") String model,
        @Param("board") String board
    );
    
    /**
     * Check if app fingerprint exists for a specific device fingerprint.
     * 
     * @param fingerprintId The device fingerprint ID
     * @return true if app fingerprint exists
     */
    boolean existsByFingerprintId(Long fingerprintId);
    
    /**
     * Delete app fingerprint by device fingerprint ID.
     * 
     * @param fingerprintId The device fingerprint ID
     */
    void deleteByFingerprintId(Long fingerprintId);
    
    /**
     * Find all app fingerprints ordered by creation date.
     * 
     * @return List of all app fingerprints
     */
    List<DeviceAppFingerprint> findAllByOrderByCreatedAtDesc();
    
    /**
     * Count total app fingerprints.
     * 
     * @return Total count of app fingerprints
     */
    @Query("SELECT COUNT(daf) FROM DeviceAppFingerprint daf")
    long countTotal();
}