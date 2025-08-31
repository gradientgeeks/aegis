package com.gradientgeeks.aegis.sfe.service;

import com.gradientgeeks.aegis.sfe.dto.DeviceFingerprintDto;
import com.gradientgeeks.aegis.sfe.dto.HardwareFingerprintDto;
import com.gradientgeeks.aegis.sfe.dto.DisplayFingerprintDto;
import com.gradientgeeks.aegis.sfe.dto.SensorFingerprintDto;
import com.gradientgeeks.aegis.sfe.dto.NetworkFingerprintDto;
import com.gradientgeeks.aegis.sfe.dto.AppFingerprintDto;
import com.gradientgeeks.aegis.sfe.dto.AppInfoDto;
import com.gradientgeeks.aegis.sfe.entity.DeviceFingerprint;
import com.gradientgeeks.aegis.sfe.entity.DeviceAppFingerprint;
import com.gradientgeeks.aegis.sfe.entity.DeviceAppInfo;
import com.gradientgeeks.aegis.sfe.repository.DeviceFingerprintRepository;
import com.gradientgeeks.aegis.sfe.repository.DeviceAppFingerprintRepository;
import com.gradientgeeks.aegis.sfe.repository.DeviceAppInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Service for device fraud detection using fingerprinting.
 * Implements probabilistic matching to detect devices that may have been
 * factory reset or app reinstalled after being involved in fraud.
 */
@Service
@Transactional
public class DeviceFraudDetectionService {
    
    private static final Logger logger = LoggerFactory.getLogger(DeviceFraudDetectionService.class);
    
    // Similarity thresholds for fraud detection
    private static final double HIGH_SIMILARITY_THRESHOLD = 0.9;
    private static final double MEDIUM_SIMILARITY_THRESHOLD = 0.7;
    private static final double LOW_SIMILARITY_THRESHOLD = 0.5;
    
    private final DeviceFingerprintRepository fingerprintRepository;
    private final DeviceAppFingerprintRepository appFingerprintRepository;
    private final DeviceAppInfoRepository appInfoRepository;
    
    @Autowired
    public DeviceFraudDetectionService(DeviceFingerprintRepository fingerprintRepository,
                                     DeviceAppFingerprintRepository appFingerprintRepository,
                                     DeviceAppInfoRepository appInfoRepository) {
        this.fingerprintRepository = fingerprintRepository;
        this.appFingerprintRepository = appFingerprintRepository;
        this.appInfoRepository = appInfoRepository;
    }
    
    /**
     * Analyzes device fingerprint for potential fraud indicators.
     * 
     * @param deviceId The device identifier
     * @param fingerprintDto The device fingerprint data
     * @return FraudDetectionResult with risk assessment
     */
    public FraudDetectionResult analyzeFingerprint(String deviceId, DeviceFingerprintDto fingerprintDto) {
        logger.info("Analyzing fingerprint for device: {}", deviceId);
        
        try {
            // Step 1: Check for exact composite hash match
            Optional<DeviceFingerprint> exactMatch = fingerprintRepository
                .findByCompositeHash(fingerprintDto.getCompositeHash());
            
            if (exactMatch.isPresent()) {
                DeviceFingerprint existingFingerprint = exactMatch.get();
                
                if (existingFingerprint.getIsFraudulent()) {
                    logger.warn("Exact fraudulent fingerprint match found for device: {}", deviceId);
                    return FraudDetectionResult.blocked(
                        "Exact match with known fraudulent device fingerprint",
                        1.0,
                        existingFingerprint.getDeviceId()
                    );
                } else if (existingFingerprint.getDeviceId().equals(deviceId)) {
                    // Same device re-registering - this is normal (app reinstall)
                    logger.info("Same device re-registering: {}", deviceId);
                    return FraudDetectionResult.allowed("Same device re-registration", 0.0);
                } else if (existingFingerprint.getDeviceId().startsWith(deviceId + "_") || 
                          deviceId.startsWith(existingFingerprint.getDeviceId() + "_")) {
                    // Same physical device but different bank app - this is allowed
                    logger.info("Same physical device registering with different bank app - Base Device: {}, Related: {}", 
                        existingFingerprint.getDeviceId(), deviceId);
                    return FraudDetectionResult.allowed("Same device with different bank app", 0.0);
                } else {
                    // Different deviceId but same fingerprint
                    // Check if this is a transition from old device ID format to new format
                    // Old format included client ID in hash, new format doesn't
                    logger.info("Same fingerprint with different device ID - Existing: {}, New: {}", 
                        existingFingerprint.getDeviceId(), deviceId);
                    
                    // Allow the registration but log for monitoring
                    // This handles the transition period where old device IDs are still in the database
                    return FraudDetectionResult.allowed(
                        "Same fingerprint detected - allowing for multi-bank support transition", 
                        0.0
                    );
                }
            }
            
            // Step 2: Check for similar hardware characteristics among fraudulent fingerprints
            List<DeviceFingerprint> similarFraudulent = fingerprintRepository
                .findFraudulentSimilarHardware(
                    fingerprintDto.getHardware().getManufacturer(),
                    fingerprintDto.getHardware().getModel(),
                    fingerprintDto.getHardware().getBoard()
                );
            
            if (!similarFraudulent.isEmpty()) {
                logger.warn("Found {} similar fraudulent fingerprints for device: {}", 
                    similarFraudulent.size(), deviceId);
                
                double maxSimilarity = calculateMaxSimilarity(fingerprintDto, similarFraudulent);
                
                if (maxSimilarity >= HIGH_SIMILARITY_THRESHOLD) {
                    return FraudDetectionResult.blocked(
                        "High similarity with known fraudulent device",
                        maxSimilarity,
                        similarFraudulent.get(0).getDeviceId()
                    );
                } else if (maxSimilarity >= MEDIUM_SIMILARITY_THRESHOLD) {
                    return FraudDetectionResult.flagged(
                        "Medium similarity with known fraudulent device",
                        maxSimilarity,
                        similarFraudulent.get(0).getDeviceId()
                    );
                }
            }
            
            // Step 3: Check for suspicious patterns (multiple devices with same hardware)
            List<DeviceFingerprint> similarHardware = fingerprintRepository
                .findSimilarHardwareFingerprints(
                    fingerprintDto.getHardware().getManufacturer(),
                    fingerprintDto.getHardware().getModel(),
                    fingerprintDto.getHardware().getBoard(),
                    fingerprintDto.getHardware().getCpuArchitecture()
                );
            
            // If too many devices with identical hardware characteristics, flag for review
            if (similarHardware.size() > 10) {
                logger.info("High number of similar hardware fingerprints found: {}", similarHardware.size());
                return FraudDetectionResult.flagged(
                    "Suspicious: High number of devices with identical hardware characteristics",
                    0.6,
                    null
                );
            }
            
            // Step 4: No significant fraud indicators found
            logger.info("No fraud indicators found for device: {}", deviceId);
            return FraudDetectionResult.allowed("No fraud indicators detected", 0.0);
            
        } catch (Exception e) {
            logger.error("Error during fraud detection analysis for device: {}", deviceId, e);
            // In case of error, allow registration but log for investigation
            return FraudDetectionResult.allowed("Analysis error - allowed with monitoring", 0.0);
        }
    }
    
    /**
     * Saves device fingerprint to database for future fraud detection.
     * 
     * @param deviceId The device identifier
     * @param fingerprintDto The device fingerprint data
     * @return The saved DeviceFingerprint entity
     */
    @Transactional
    public DeviceFingerprint saveFingerprint(String deviceId, DeviceFingerprintDto fingerprintDto) {
        logger.info("Saving fingerprint for device: {}", deviceId);
        
        // Check if fingerprint already exists for this device
        Optional<DeviceFingerprint> existingFingerprint = fingerprintRepository.findByDeviceId(deviceId);
        if (existingFingerprint.isPresent()) {
            logger.info("Fingerprint already exists for device: {}, updating timestamp", deviceId);
            DeviceFingerprint existing = existingFingerprint.get();
            existing.setFingerprintTimestamp(
                LocalDateTime.ofEpochSecond(
                    fingerprintDto.getTimestamp() / 1000, 
                    (int) ((fingerprintDto.getTimestamp() % 1000) * 1_000_000), 
                    ZoneOffset.UTC
                )
            );
            return fingerprintRepository.save(existing);
        }
        
        DeviceFingerprint fingerprint = new DeviceFingerprint();
        fingerprint.setDeviceId(deviceId);
        fingerprint.setVersion(fingerprintDto.getVersion());
        fingerprint.setCompositeHash(fingerprintDto.getCompositeHash());
        
        // Hardware characteristics
        fingerprint.setManufacturer(fingerprintDto.getHardware().getManufacturer());
        fingerprint.setModel(fingerprintDto.getHardware().getModel());
        fingerprint.setDeviceName(fingerprintDto.getHardware().getDevice());
        fingerprint.setBoard(fingerprintDto.getHardware().getBoard());
        fingerprint.setBrand(fingerprintDto.getHardware().getBrand());
        fingerprint.setCpuArchitecture(fingerprintDto.getHardware().getCpuArchitecture());
        fingerprint.setApiLevel(fingerprintDto.getHardware().getApiLevel());
        fingerprint.setHardwareHash(fingerprintDto.getHardware().getHash());
        
        // Display characteristics
        fingerprint.setWidthPixels(fingerprintDto.getDisplay().getWidthPixels());
        fingerprint.setHeightPixels(fingerprintDto.getDisplay().getHeightPixels());
        fingerprint.setDensityDpi(fingerprintDto.getDisplay().getDensityDpi());
        fingerprint.setDisplayHash(fingerprintDto.getDisplay().getHash());
        
        // Sensor characteristics
        fingerprint.setSensorTypes(fingerprintDto.getSensors().getSensorTypes());
        fingerprint.setSensorCount(fingerprintDto.getSensors().getSensorCount());
        fingerprint.setSensorHash(fingerprintDto.getSensors().getHash());
        
        // Network characteristics
        fingerprint.setNetworkCountryIso(fingerprintDto.getNetwork().getNetworkCountryIso());
        fingerprint.setSimCountryIso(fingerprintDto.getNetwork().getSimCountryIso());
        fingerprint.setPhoneType(fingerprintDto.getNetwork().getPhoneType());
        fingerprint.setNetworkHash(fingerprintDto.getNetwork().getHash());
        
        // Timestamp
        fingerprint.setFingerprintTimestamp(
            LocalDateTime.ofEpochSecond(
                fingerprintDto.getTimestamp() / 1000, 
                (int) ((fingerprintDto.getTimestamp() % 1000) * 1_000_000), 
                ZoneOffset.UTC
            )
        );
        
        DeviceFingerprint savedFingerprint = fingerprintRepository.save(fingerprint);
        
        // Save app fingerprint data if available
        if (fingerprintDto.getApps() != null) {
            try {
                saveAppFingerprint(savedFingerprint.getId(), fingerprintDto.getApps());
                logger.info("App fingerprint data saved for device: {}", deviceId);
            } catch (Exception e) {
                logger.error("Failed to save app fingerprint data for device: {}", deviceId, e);
                // Don't fail the entire operation if app data save fails
            }
        }
        
        return savedFingerprint;
    }
    
    /**
     * Marks a device as fraudulent and updates all associated fingerprints.
     * 
     * @param deviceId The device identifier
     * @param reason Reason for marking as fraudulent
     * @return true if successfully marked, false otherwise
     */
    @Transactional
    public boolean markDeviceAsFraudulent(String deviceId, String reason) {
        logger.warn("Marking device as fraudulent: {} - Reason: {}", deviceId, reason);
        
        try {
            fingerprintRepository.markAsFraudulent(deviceId, reason, LocalDateTime.now());
            
            // Also mark similar fingerprints for review
            Optional<DeviceFingerprint> deviceFingerprint = fingerprintRepository.findByDeviceId(deviceId);
            if (deviceFingerprint.isPresent()) {
                DeviceFingerprint fp = deviceFingerprint.get();
                List<DeviceFingerprint> similar = fingerprintRepository
                    .findSimilarHardwareFingerprints(
                        fp.getManufacturer(), fp.getModel(), 
                        fp.getBoard(), fp.getCpuArchitecture()
                    );
                
                logger.info("Found {} similar fingerprints that may need review", similar.size());
            }
            
            return true;
        } catch (Exception e) {
            logger.error("Error marking device as fraudulent: {}", deviceId, e);
            return false;
        }
    }
    
    /**
     * Calculates maximum similarity score with a list of fingerprints.
     */
    private double calculateMaxSimilarity(DeviceFingerprintDto incoming, List<DeviceFingerprint> existing) {
        double maxSimilarity = 0.0;
        
        for (DeviceFingerprint fingerprint : existing) {
            double similarity = calculateSimilarity(incoming, fingerprint);
            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
            }
        }
        
        return maxSimilarity;
    }
    
    /**
     * Calculates similarity between incoming fingerprint and existing fingerprint.
     */
    private double calculateSimilarity(DeviceFingerprintDto incoming, DeviceFingerprint existing) {
        // Hardware similarity (most important)
        double hardwareSimilarity = calculateHardwareSimilarity(incoming, existing);
        
        // Display similarity
        double displaySimilarity = calculateDisplaySimilarity(incoming, existing);
        
        // App similarity (for device reinstall detection)
        double appSimilarity = calculateAppSimilarity(incoming, existing);
        
        // Weighted average: hardware is most critical, app data helps with reinstall detection
        if (appSimilarity >= 0.0) {
            // When app data is available, include it in similarity calculation
            return (hardwareSimilarity * 0.5) + (displaySimilarity * 0.25) + (appSimilarity * 0.25);
        } else {
            // Original calculation when no app data
            return (hardwareSimilarity * 0.7) + (displaySimilarity * 0.3);
        }
    }
    
    /**
     * Calculates hardware similarity score.
     */
    private double calculateHardwareSimilarity(DeviceFingerprintDto incoming, DeviceFingerprint existing) {
        int matches = 0;
        int total = 0;
        
        // Core hardware characteristics (weighted)
        if (incoming.getHardware().getManufacturer().equals(existing.getManufacturer())) matches += 3;
        total += 3;
        
        if (incoming.getHardware().getModel().equals(existing.getModel())) matches += 3;
        total += 3;
        
        if (incoming.getHardware().getDevice().equals(existing.getDeviceName())) matches += 2;
        total += 2;
        
        if (incoming.getHardware().getBoard().equals(existing.getBoard())) matches += 2;
        total += 2;
        
        if (incoming.getHardware().getCpuArchitecture().equals(existing.getCpuArchitecture())) matches += 2;
        total += 2;
        
        if (incoming.getHardware().getBrand().equals(existing.getBrand())) matches += 1;
        total += 1;
        
        return (double) matches / total;
    }
    
    /**
     * Calculates display similarity score.
     */
    private double calculateDisplaySimilarity(DeviceFingerprintDto incoming, DeviceFingerprint existing) {
        return incoming.getDisplay().getWidthPixels().equals(existing.getWidthPixels()) &&
               incoming.getDisplay().getHeightPixels().equals(existing.getHeightPixels()) &&
               incoming.getDisplay().getDensityDpi().equals(existing.getDensityDpi()) ? 1.0 : 0.0;
    }
    
    /**
     * Calculates app similarity score for device reinstall detection.
     * Returns -1.0 if app data is not available, otherwise returns similarity score (0.0 to 1.0).
     */
    private double calculateAppSimilarity(DeviceFingerprintDto incoming, DeviceFingerprint existing) {
        AppFingerprintDto incomingApps = incoming.getApps();
        if (incomingApps == null) {
            return -1.0; // No app data available
        }
        
        // Find existing app fingerprint for the device
        Optional<DeviceAppFingerprint> existingAppFingerprint = appFingerprintRepository.findByFingerprintId(existing.getId());
        if (!existingAppFingerprint.isPresent()) {
            logger.debug("No existing app fingerprint found for device fingerprint ID: {}", existing.getId());
            return -1.0; // No existing app data to compare
        }
        
        DeviceAppFingerprint existingApps = existingAppFingerprint.get();
        logger.debug("Comparing app fingerprints - Incoming: Total={}, User={}, System={} | Existing: Total={}, User={}, System={}", 
            incomingApps.getTotalAppCount(), incomingApps.getUserAppCount(), incomingApps.getSystemAppCount(),
            existingApps.getTotalAppCount(), existingApps.getUserAppCount(), existingApps.getSystemAppCount());
        
        // Calculate basic count-based similarity
        double countSimilarity = calculateAppCountSimilarity(incomingApps, existingApps);
        
        // Calculate package name overlap similarity
        double packageSimilarity = calculatePackageNameSimilarity(incomingApps, existingApps.getId());
        
        // Weighted combination: package similarity is more important than counts
        double finalSimilarity = (countSimilarity * 0.3) + (packageSimilarity * 0.7);
        
        logger.debug("App similarity calculation - Count: {}, Package: {}, Final: {}", 
            countSimilarity, packageSimilarity, finalSimilarity);
        
        return finalSimilarity;
    }
    
    /**
     * Calculates similarity based on app counts.
     */
    private double calculateAppCountSimilarity(AppFingerprintDto incoming, DeviceAppFingerprint existing) {
        // Total app count similarity
        double totalCountSimilarity = 1.0 - ((double) Math.abs(incoming.getTotalAppCount() - existing.getTotalAppCount()) / 
                                            (double) Math.max(incoming.getTotalAppCount(), existing.getTotalAppCount()));
        
        // User app count similarity
        double userCountSimilarity = 1.0 - ((double) Math.abs(incoming.getUserAppCount() - existing.getUserAppCount()) / 
                                           (double) Math.max(incoming.getUserAppCount(), existing.getUserAppCount()));
        
        // System app count similarity
        double systemCountSimilarity = 1.0 - ((double) Math.abs(incoming.getSystemAppCount() - existing.getSystemAppCount()) / 
                                             (double) Math.max(incoming.getSystemAppCount(), existing.getSystemAppCount()));
        
        // Weighted average: user apps are more distinctive than system apps
        return (totalCountSimilarity * 0.4) + (userCountSimilarity * 0.4) + (systemCountSimilarity * 0.2);
    }
    
    /**
     * Calculates similarity based on package name overlap.
     */
    private double calculatePackageNameSimilarity(AppFingerprintDto incoming, Long existingAppFingerprintId) {
        try {
            // Get existing package names
            Set<String> existingUserApps = appInfoRepository.findUserAppPackageNamesByFingerprintId(existingAppFingerprintId);
            Set<String> existingSystemApps = appInfoRepository.findSystemAppPackageNamesByFingerprintId(existingAppFingerprintId);
            
            // Get incoming package names
            Set<String> incomingUserApps = incoming.getUserApps().stream()
                .map(AppInfoDto::getPackageName)
                .collect(Collectors.toSet());
            Set<String> incomingSystemApps = incoming.getSystemApps().stream()
                .map(AppInfoDto::getPackageName)
                .collect(Collectors.toSet());
            
            // Calculate user app similarity (more important for device identification)
            double userAppSimilarity = calculateSetSimilarity(incomingUserApps, existingUserApps);
            
            // Calculate system app similarity
            double systemAppSimilarity = calculateSetSimilarity(incomingSystemApps, existingSystemApps);
            
            // Weighted combination: user apps are more distinctive
            double packageSimilarity = (userAppSimilarity * 0.7) + (systemAppSimilarity * 0.3);
            
            logger.debug("Package similarity - User apps: {} common out of {} total, System apps: {} common out of {} total", 
                incomingUserApps.stream().filter(existingUserApps::contains).count(), 
                incomingUserApps.size() + existingUserApps.size(),
                incomingSystemApps.stream().filter(existingSystemApps::contains).count(),
                incomingSystemApps.size() + existingSystemApps.size());
            
            return packageSimilarity;
            
        } catch (Exception e) {
            logger.error("Error calculating package name similarity", e);
            return 0.0;
        }
    }
    
    /**
     * Calculates Jaccard similarity between two sets.
     */
    private double calculateSetSimilarity(Set<String> set1, Set<String> set2) {
        if (set1.isEmpty() && set2.isEmpty()) {
            return 1.0; // Both empty, perfect match
        }
        
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }
    
    /**
     * Saves app fingerprint data to database.
     */
    @Transactional
    private void saveAppFingerprint(Long deviceFingerprintId, AppFingerprintDto appFingerprintDto) {
        logger.debug("Saving app fingerprint for device fingerprint ID: {}", deviceFingerprintId);
        
        // Create app fingerprint entity
        DeviceAppFingerprint appFingerprint = new DeviceAppFingerprint(
            deviceFingerprintId,
            appFingerprintDto.getTotalAppCount(),
            appFingerprintDto.getUserAppCount(),
            appFingerprintDto.getSystemAppCount(),
            appFingerprintDto.getHash()
        );
        
        DeviceAppFingerprint savedAppFingerprint = appFingerprintRepository.save(appFingerprint);
        
        // Save individual app info
        List<DeviceAppInfo> appInfoList = new ArrayList<>();
        
        // Save user apps
        for (AppInfoDto userApp : appFingerprintDto.getUserApps()) {
            appInfoList.add(new DeviceAppInfo(
                savedAppFingerprint.getId(),
                userApp.getPackageName(),
                userApp.getFirstInstallTime(),
                userApp.getLastUpdateTime(),
                false // user app
            ));
        }
        
        // Save system apps
        for (AppInfoDto systemApp : appFingerprintDto.getSystemApps()) {
            appInfoList.add(new DeviceAppInfo(
                savedAppFingerprint.getId(),
                systemApp.getPackageName(),
                systemApp.getFirstInstallTime(),
                systemApp.getLastUpdateTime(),
                true // system app
            ));
        }
        
        // Batch save app info
        appInfoRepository.saveAll(appInfoList);
        
        logger.debug("Saved {} user apps and {} system apps for device fingerprint ID: {}", 
            appFingerprintDto.getUserAppCount(), appFingerprintDto.getSystemAppCount(), deviceFingerprintId);
    }
    
    /**
     * Get total count of fraudulent devices
     */
    @Transactional(readOnly = true)
    public long getFraudulentDeviceCount() {
        return fingerprintRepository.countByIsFraudulent(true);
    }
    
    /**
     * Get count of fraudulent devices by organization
     */
    @Transactional(readOnly = true)
    public long getFraudulentDeviceCountByOrganization(String organization) {
        // This would require joining with Device table in a real implementation
        // For now, return 0
        return 0;
    }
    
    /**
     * Get recent fraud report count for the specified period
     */
    @Transactional(readOnly = true)
    public long getRecentFraudReportCount(String period) {
        LocalDateTime startDate = getStartDateForPeriod(period);
        return fingerprintRepository.countByIsFraudulentAndFraudReportedAtAfter(true, startDate);
    }
    
    /**
     * Get recent fraud report count by organization for the specified period
     */
    @Transactional(readOnly = true)
    public long getRecentFraudReportCountByOrganization(String organization, String period) {
        // This would require joining with Device table in a real implementation
        // For now, return 0
        return 0;
    }
    
    /**
     * Get recent fraud reports
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRecentFraudReports(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<DeviceFingerprint> recentFraud = fingerprintRepository
            .findByIsFraudulentOrderByFraudReportedAtDesc(true, pageable);
        
        return recentFraud.stream().map(fp -> {
            Map<String, Object> report = new HashMap<>();
            report.put("deviceId", fp.getDeviceId());
            report.put("reportedAt", fp.getFraudReportedAt());
            report.put("reason", fp.getFraudReason());
            report.put("device", fp.getManufacturer() + " " + fp.getModel());
            return report;
        }).collect(Collectors.toList());
    }
    
    /**
     * Get recent fraud reports by organization
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRecentFraudReportsByOrganization(String organization, int limit) {
        // This would require joining with Device table in a real implementation
        // For now, return empty list
        return new ArrayList<>();
    }
    
    /**
     * Convert period string to start date
     */
    private LocalDateTime getStartDateForPeriod(String period) {
        LocalDateTime now = LocalDateTime.now();
        
        switch (period.toLowerCase()) {
            case "24h":
                return now.minusDays(1);
            case "7d":
                return now.minusDays(7);
            case "30d":
                return now.minusDays(30);
            case "90d":
                return now.minusDays(90);
            default:
                return now.minusDays(30); // Default to 30 days
        }
    }
}

/**
 * Result of fraud detection analysis.
 */
class FraudDetectionResult {
    
    public enum Status {
        ALLOWED,    // Device can proceed with registration
        FLAGGED,    // Device flagged for manual review but allowed to proceed
        BLOCKED     // Device blocked from registration
    }
    
    private final Status status;
    private final String reason;
    private final double similarityScore;
    private final String similarDeviceId;
    
    private FraudDetectionResult(Status status, String reason, double similarityScore, String similarDeviceId) {
        this.status = status;
        this.reason = reason;
        this.similarityScore = similarityScore;
        this.similarDeviceId = similarDeviceId;
    }
    
    public static FraudDetectionResult allowed(String reason, double score) {
        return new FraudDetectionResult(Status.ALLOWED, reason, score, null);
    }
    
    public static FraudDetectionResult flagged(String reason, double score, String similarDeviceId) {
        return new FraudDetectionResult(Status.FLAGGED, reason, score, similarDeviceId);
    }
    
    public static FraudDetectionResult blocked(String reason, double score, String similarDeviceId) {
        return new FraudDetectionResult(Status.BLOCKED, reason, score, similarDeviceId);
    }
    
    public Status getStatus() {
        return status;
    }
    
    public String getReason() {
        return reason;
    }
    
    public double getSimilarityScore() {
        return similarityScore;
    }
    
    public String getSimilarDeviceId() {
        return similarDeviceId;
    }
    
    public boolean isBlocked() {
        return status == Status.BLOCKED;
    }
    
    public boolean isFlagged() {
        return status == Status.FLAGGED;
    }
    
    public boolean isAllowed() {
        return status == Status.ALLOWED;
    }
}