package com.gradientgeeks.ageis.backendapp.repository;

import com.gradientgeeks.ageis.backendapp.entity.DeviceRebindingLog;
import com.gradientgeeks.ageis.backendapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DeviceRebindingLogRepository extends JpaRepository<DeviceRebindingLog, Long> {
    
    List<DeviceRebindingLog> findByUserOrderByCreatedAtDesc(User user);
    
    List<DeviceRebindingLog> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<DeviceRebindingLog> findByNewDeviceId(String deviceId);
    
    List<DeviceRebindingLog> findByUserAndCreatedAtBetween(User user, LocalDateTime start, LocalDateTime end);
    
    long countByUserAndSuccessFalseAndCreatedAtAfter(User user, LocalDateTime after);
}