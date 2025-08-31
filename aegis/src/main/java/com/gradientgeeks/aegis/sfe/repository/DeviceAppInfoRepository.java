package com.gradientgeeks.aegis.sfe.repository;

import com.gradientgeeks.aegis.sfe.entity.DeviceAppInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * Repository for DeviceAppInfo entity operations.
 * Provides methods for detailed app package comparison.
 */
@Repository
public interface DeviceAppInfoRepository extends JpaRepository<DeviceAppInfo, Long> {
    
    /**
     * Find all app info for a specific app fingerprint.
     * 
     * @param appFingerprintId The app fingerprint ID
     * @return List of app info
     */
    List<DeviceAppInfo> findByAppFingerprintId(Long appFingerprintId);
    
    /**
     * Find user apps for a specific app fingerprint.
     * 
     * @param appFingerprintId The app fingerprint ID
     * @return List of user app info
     */
    @Query("SELECT dai FROM DeviceAppInfo dai WHERE dai.appFingerprintId = :appFingerprintId AND dai.isSystemApp = false")
    List<DeviceAppInfo> findUserAppsByFingerprintId(@Param("appFingerprintId") Long appFingerprintId);
    
    /**
     * Find system apps for a specific app fingerprint.
     * 
     * @param appFingerprintId The app fingerprint ID
     * @return List of system app info
     */
    @Query("SELECT dai FROM DeviceAppInfo dai WHERE dai.appFingerprintId = :appFingerprintId AND dai.isSystemApp = true")
    List<DeviceAppInfo> findSystemAppsByFingerprintId(@Param("appFingerprintId") Long appFingerprintId);
    
    /**
     * Get package names for user apps in a specific app fingerprint.
     * 
     * @param appFingerprintId The app fingerprint ID
     * @return Set of user app package names
     */
    @Query("SELECT dai.packageName FROM DeviceAppInfo dai WHERE dai.appFingerprintId = :appFingerprintId AND dai.isSystemApp = false")
    Set<String> findUserAppPackageNamesByFingerprintId(@Param("appFingerprintId") Long appFingerprintId);
    
    /**
     * Get package names for system apps in a specific app fingerprint.
     * 
     * @param appFingerprintId The app fingerprint ID
     * @return Set of system app package names
     */
    @Query("SELECT dai.packageName FROM DeviceAppInfo dai WHERE dai.appFingerprintId = :appFingerprintId AND dai.isSystemApp = true")
    Set<String> findSystemAppPackageNamesByFingerprintId(@Param("appFingerprintId") Long appFingerprintId);
    
    /**
     * Find apps by package name across all fingerprints.
     * Used to identify common packages across devices.
     * 
     * @param packageName The package name
     * @return List of app info with this package name
     */
    List<DeviceAppInfo> findByPackageName(String packageName);
    
    /**
     * Count apps by type for a specific app fingerprint.
     * 
     * @param appFingerprintId The app fingerprint ID
     * @param isSystemApp Whether to count system apps (true) or user apps (false)
     * @return Count of apps
     */
    @Query("SELECT COUNT(dai) FROM DeviceAppInfo dai WHERE dai.appFingerprintId = :appFingerprintId AND dai.isSystemApp = :isSystemApp")
    long countByAppFingerprintIdAndIsSystemApp(@Param("appFingerprintId") Long appFingerprintId, @Param("isSystemApp") Boolean isSystemApp);
    
    /**
     * Delete all app info for a specific app fingerprint.
     * 
     * @param appFingerprintId The app fingerprint ID
     */
    void deleteByAppFingerprintId(Long appFingerprintId);
    
    /**
     * Check if a specific package exists in an app fingerprint.
     * 
     * @param appFingerprintId The app fingerprint ID
     * @param packageName The package name
     * @return true if package exists
     */
    boolean existsByAppFingerprintIdAndPackageName(Long appFingerprintId, String packageName);
    
    /**
     * Find common packages between two app fingerprints.
     * 
     * @param appFingerprintId1 First app fingerprint ID
     * @param appFingerprintId2 Second app fingerprint ID
     * @return List of common package names
     */
    @Query("SELECT dai1.packageName FROM DeviceAppInfo dai1 " +
           "WHERE dai1.appFingerprintId = :appFingerprintId1 " +
           "AND EXISTS (SELECT dai2 FROM DeviceAppInfo dai2 " +
           "WHERE dai2.appFingerprintId = :appFingerprintId2 " +
           "AND dai1.packageName = dai2.packageName)")
    List<String> findCommonPackageNames(@Param("appFingerprintId1") Long appFingerprintId1, 
                                       @Param("appFingerprintId2") Long appFingerprintId2);
}