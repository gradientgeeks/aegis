package com.gradientgeeks.aegis.sfe.repository;

import com.gradientgeeks.aegis.sfe.entity.Device;
import com.gradientgeeks.aegis.sfe.entity.DeviceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, DeviceId>, JpaSpecificationExecutor<Device> {
    
    Optional<Device> findByDeviceIdAndClientId(String deviceId, String clientId);
    
    @Query("SELECT d FROM Device d WHERE d.deviceId = :deviceId AND d.clientId = :clientId AND d.isActive = true")
    Optional<Device> findActiveByDeviceIdAndClientId(@Param("deviceId") String deviceId, @Param("clientId") String clientId);
    
    @Query("SELECT d FROM Device d WHERE d.deviceId = :deviceId")
    List<Device> findAllByDeviceId(@Param("deviceId") String deviceId);
    
    boolean existsByDeviceIdAndClientId(String deviceId, String clientId);
    
    @Modifying
    @Query("UPDATE Device d SET d.lastSeen = :lastSeen WHERE d.deviceId = :deviceId AND d.clientId = :clientId")
    void updateLastSeen(@Param("deviceId") String deviceId, @Param("clientId") String clientId, @Param("lastSeen") LocalDateTime lastSeen);
    
    @Modifying
    @Query("UPDATE Device d SET d.isActive = false WHERE d.deviceId = :deviceId")
    void deactivateAllDevicesById(@Param("deviceId") String deviceId);
    
    @Modifying
    @Query("UPDATE Device d SET d.isActive = false WHERE d.deviceId = :deviceId AND d.clientId = :clientId")
    void deactivateDevice(@Param("deviceId") String deviceId, @Param("clientId") String clientId);
    
    @Query("SELECT COUNT(d) FROM Device d WHERE d.clientId = :clientId AND d.isActive = true")
    long countActiveDevicesByClientId(@Param("clientId") String clientId);
    
    @Query("SELECT d FROM Device d WHERE d.clientId = :clientId")
    java.util.List<Device> findByClientId(@Param("clientId") String clientId);
    
    @Query("SELECT d FROM Device d WHERE d.status = :status")
    java.util.List<Device> findByStatus(@Param("status") Device.DeviceStatus status);
    
    @Query("SELECT d FROM Device d WHERE d.clientId = :clientId AND d.status = :status")
    java.util.List<Device> findByClientIdAndStatus(@Param("clientId") String clientId, @Param("status") Device.DeviceStatus status);
    
    long countByClientId(String clientId);
    
    long countByStatusIn(java.util.List<Device.DeviceStatus> statuses);
    
    long countByClientIdAndStatusIn(String clientId, java.util.List<Device.DeviceStatus> statuses);
    
}