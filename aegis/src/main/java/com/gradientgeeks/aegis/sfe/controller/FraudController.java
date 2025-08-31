package com.gradientgeeks.aegis.sfe.controller;

import com.gradientgeeks.aegis.sfe.dto.*;
import com.gradientgeeks.aegis.sfe.entity.Device;
import com.gradientgeeks.aegis.sfe.entity.DeviceFingerprint;
import com.gradientgeeks.aegis.sfe.service.DeviceFraudDetectionService;
import com.gradientgeeks.aegis.sfe.service.DeviceRegistrationService;
import com.gradientgeeks.aegis.sfe.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for fraud detection and device management endpoints
 * Accessible by both ADMIN and USER roles with organization-based filtering
 */
@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class FraudController {
    
    private static final Logger logger = LoggerFactory.getLogger(FraudController.class);
    
    private final SecurityUtils securityUtils;
    private final DeviceRegistrationService deviceRegistrationService;
    private final DeviceFraudDetectionService deviceFraudDetectionService;
    
    @Autowired
    public FraudController(SecurityUtils securityUtils, 
                          DeviceRegistrationService deviceRegistrationService,
                          DeviceFraudDetectionService deviceFraudDetectionService) {
        this.securityUtils = securityUtils;
        this.deviceRegistrationService = deviceRegistrationService;
        this.deviceFraudDetectionService = deviceFraudDetectionService;
    }
    
    /**
     * Get fraud statistics
     * Admins see all statistics, users see organization-specific statistics
     * 
     * @param period Time period for statistics (e.g., "30d", "7d", "24h")
     * @return Fraud statistics
     */
    @GetMapping("/fraud/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> getFraudStatistics(@RequestParam(defaultValue = "30d") String period) {
        
        String organization = securityUtils.getCurrentUserOrganization();
        if (organization == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        logger.info("Fetching fraud statistics for organization: {} (period: {})", 
            securityUtils.isAdmin() ? "ALL" : organization, period);
        
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            if (securityUtils.isAdmin()) {
                // Admin sees all statistics
                statistics.put("totalDevices", deviceRegistrationService.getTotalDeviceCount());
                statistics.put("blockedDevices", deviceRegistrationService.getBlockedDeviceCount());
                statistics.put("fraudulentDevices", deviceFraudDetectionService.getFraudulentDeviceCount());
                statistics.put("recentReports", deviceFraudDetectionService.getRecentFraudReportCount(period));
                statistics.put("organizationBreakdown", deviceRegistrationService.getDeviceCountByOrganization());
            } else {
                // User sees only their organization's statistics
                statistics.put("totalDevices", deviceRegistrationService.getTotalDeviceCountByOrganization(organization));
                statistics.put("blockedDevices", deviceRegistrationService.getBlockedDeviceCountByOrganization(organization));
                statistics.put("fraudulentDevices", deviceFraudDetectionService.getFraudulentDeviceCountByOrganization(organization));
                statistics.put("recentReports", deviceFraudDetectionService.getRecentFraudReportCountByOrganization(organization, period));
            }
            
            statistics.put("period", period);
            statistics.put("generatedAt", System.currentTimeMillis());
            
            return ResponseEntity.ok(statistics);
            
        } catch (Exception e) {
            logger.error("Error fetching fraud statistics", e);
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Get recent fraud reports
     * Admins see all reports, users see organization-specific reports
     * 
     * @param limit Number of reports to retrieve
     * @return List of recent fraud reports
     */
    @GetMapping("/fraud/reports/recent")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> getRecentFraudReports(@RequestParam(defaultValue = "10") int limit) {
        
        String organization = securityUtils.getCurrentUserOrganization();
        if (organization == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        logger.info("Fetching recent fraud reports for organization: {} (limit: {})", 
            securityUtils.isAdmin() ? "ALL" : organization, limit);
        
        try {
            List<Map<String, Object>> reports;
            
            if (securityUtils.isAdmin()) {
                // Admin sees all reports
                reports = deviceFraudDetectionService.getRecentFraudReports(limit);
            } else {
                // User sees only their organization's reports
                reports = deviceFraudDetectionService.getRecentFraudReportsByOrganization(organization, limit);
            }
            
            return ResponseEntity.ok(reports);
            
        } catch (Exception e) {
            logger.error("Error fetching recent fraud reports", e);
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Search devices by various criteria
     * Admins see all devices, users see only devices that have interacted with their organization
     * 
     * @param deviceId Optional device ID to search for
     * @param clientId Optional client ID (organization) to filter by
     * @param status Optional device status to filter by
     * @param page Page number (0-based)
     * @param size Page size
     * @return Paginated list of devices
     */
    @GetMapping("/devices/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> searchDevices(
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) String clientId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        String organization = securityUtils.getCurrentUserOrganization();
        if (organization == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        logger.info("Searching devices - deviceId: {}, clientId: {}, status: {}, org: {}", 
            deviceId, clientId, status, securityUtils.isAdmin() ? "ALL" : organization);
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Device> devices;
            
            if (securityUtils.isAdmin()) {
                // Admin sees all devices
                devices = deviceRegistrationService.searchDevices(deviceId, clientId, status, pageable);
            } else {
                // User sees only devices that belong to their organization's client IDs
                devices = deviceRegistrationService.searchDevicesByOrganization(
                    deviceId, organization, status, pageable);
            }
            
            // Get fingerprint data for each device
            List<Map<String, Object>> deviceList = devices.getContent().stream().map(device -> {
                Map<String, Object> deviceInfo = new HashMap<>();
                deviceInfo.put("deviceId", device.getDeviceId());
                deviceInfo.put("clientId", device.getClientId());
                deviceInfo.put("status", device.getStatus().toString());
                deviceInfo.put("registrationDate", device.getCreatedAt());
                deviceInfo.put("lastActivity", device.getUpdatedAt());
                
                // Add fingerprint info if available
                Optional<DeviceFingerprint> fingerprintOpt = 
                    deviceRegistrationService.findFingerprintByDeviceId(device.getDeviceId());
                if (fingerprintOpt.isPresent()) {
                    DeviceFingerprint fingerprint = fingerprintOpt.get();
                    deviceInfo.put("isFraudulent", fingerprint.getIsFraudulent());
                    deviceInfo.put("hardwareInfo", Map.of(
                        "manufacturer", fingerprint.getManufacturer(),
                        "model", fingerprint.getModel(),
                        "device", fingerprint.getDeviceName()
                    ));
                }
                
                return deviceInfo;
            }).collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("devices", deviceList);
            response.put("pagination", Map.of(
                "page", devices.getNumber(),
                "size", devices.getSize(),
                "totalElements", devices.getTotalElements(),
                "totalPages", devices.getTotalPages(),
                "hasNext", devices.hasNext(),
                "hasPrevious", devices.hasPrevious()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error searching devices", e);
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Get device fraud status and details
     * Users can only see devices that have interacted with their organization
     * 
     * @param deviceId The device identifier to check
     * @return Device fraud information and details
     */
    @GetMapping("/devices/{deviceId}/fraud-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> getDeviceFraudStatus(@PathVariable String deviceId) {
        
        String organization = securityUtils.getCurrentUserOrganization();
        if (organization == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        logger.info("Checking fraud status for device: {} by organization: {}", 
            deviceId, securityUtils.isAdmin() ? "ADMIN" : organization);
        
        try {
            List<Device> devices = deviceRegistrationService.findAllDevicesByDeviceId(deviceId);
            if (devices.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("status", "error");
                error.put("message", "Device not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            // For fraud check, we just need to check if any device entry exists
            // We'll use the first device's status as they should all have the same status
            Device device = devices.get(0);
            
            // Check if non-admin user has access to this device
            if (!securityUtils.isAdmin()) {
                boolean hasAccess = deviceRegistrationService.hasDeviceInteractedWithBank(deviceId, organization);
                if (!hasAccess) {
                    Map<String, String> error = new HashMap<>();
                    error.put("status", "error");
                    error.put("message", "Access denied");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("deviceId", deviceId);
            response.put("status", device.getStatus().toString());
            response.put("organization", device.getClientId());
            response.put("registrationDate", device.getCreatedAt());
            response.put("lastValidation", device.getUpdatedAt());
            
            // Add fingerprint fraud information if available
            Optional<DeviceFingerprint> fingerprintOpt = deviceRegistrationService.findFingerprintByDeviceId(deviceId);
            if (fingerprintOpt.isPresent()) {
                DeviceFingerprint fingerprint = fingerprintOpt.get();
                response.put("isFraudulent", fingerprint.getIsFraudulent());
                response.put("fraudReportedAt", fingerprint.getFraudReportedAt());
                response.put("fraudReason", fingerprint.getFraudReason());
                response.put("hardwareInfo", Map.of(
                    "manufacturer", fingerprint.getManufacturer(),
                    "model", fingerprint.getModel(),
                    "device", fingerprint.getDeviceName()
                ));
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error checking device fraud status: {}", deviceId, e);
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Mark a device as fraudulent
     * Users can only mark devices that have interacted with their organization
     * 
     * @param deviceId The device identifier to mark as fraudulent
     * @param request Request containing fraud reason
     * @return Success or error response
     */
    @PostMapping("/devices/{deviceId}/mark-fraudulent")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> markDeviceAsFraudulent(
            @PathVariable String deviceId,
            @RequestBody Map<String, String> request) {
        
        String organization = securityUtils.getCurrentUserOrganization();
        if (organization == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        logger.warn("Marking device as fraudulent: {} by organization: {}", 
            deviceId, securityUtils.isAdmin() ? "ADMIN" : organization);
        
        try {
            // Check if non-admin user has access to this device
            if (!securityUtils.isAdmin()) {
                boolean hasAccess = deviceRegistrationService.hasDeviceInteractedWithBank(deviceId, organization);
                if (!hasAccess) {
                    Map<String, String> error = new HashMap<>();
                    error.put("status", "error");
                    error.put("message", "Access denied - no interaction history with this device");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                }
            }
            
            String reason = request.getOrDefault("reason", 
                "Marked fraudulent by " + (securityUtils.isAdmin() ? "admin" : organization));
            
            boolean success = deviceRegistrationService.markDeviceAsFraudulent(deviceId, reason);
            
            if (success) {
                logger.info("Device marked as fraudulent successfully: {} by {}", 
                    deviceId, securityUtils.isAdmin() ? "ADMIN" : organization);
                Map<String, String> response = new HashMap<>();
                response.put("status", "success");
                response.put("message", "Device marked as fraudulent");
                response.put("deviceId", deviceId);
                return ResponseEntity.ok(response);
            } else {
                logger.error("Failed to mark device as fraudulent: {}", deviceId);
                Map<String, String> error = new HashMap<>();
                error.put("status", "error");
                error.put("message", "Failed to mark device as fraudulent");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
            }
            
        } catch (Exception e) {
            logger.error("Error marking device as fraudulent: {}", deviceId, e);
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Get device transaction history
     * Shows anonymized transaction validation history for a device
     * Users can only see history for devices that have interacted with their organization
     * 
     * @param deviceId The device identifier
     * @param page Page number (0-based)
     * @param size Page size
     * @return Paginated validation history
     */
    @GetMapping("/devices/{deviceId}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> getDeviceHistory(
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        String organization = securityUtils.getCurrentUserOrganization();
        if (organization == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        logger.info("Retrieving history for device: {} by organization: {}", 
            deviceId, securityUtils.isAdmin() ? "ADMIN" : organization);
        
        try {
            // Check if non-admin user has access to this device
            if (!securityUtils.isAdmin()) {
                boolean hasAccess = deviceRegistrationService.hasDeviceInteractedWithBank(deviceId, organization);
                if (!hasAccess) {
                    Map<String, String> error = new HashMap<>();
                    error.put("status", "error");
                    error.put("message", "Access denied");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                }
            }
            
            Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
            
            // This would require implementing a validation history service
            // For now, return a placeholder response
            Map<String, Object> response = new HashMap<>();
            response.put("deviceId", deviceId);
            response.put("validations", List.of());
            response.put("pagination", Map.of(
                "page", page,
                "size", size,
                "totalElements", 0,
                "totalPages", 0
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error retrieving device history: {}", deviceId, e);
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}