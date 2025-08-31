package com.gradientgeeks.aegis.sfe.repository;

import com.gradientgeeks.aegis.sfe.entity.Policy;
import com.gradientgeeks.aegis.sfe.entity.PolicyViolation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PolicyViolationRepository extends JpaRepository<PolicyViolation, Long> {
    
    /**
     * Find violations by device ID
     */
    Page<PolicyViolation> findByDeviceIdOrderByCreatedAtDesc(String deviceId, Pageable pageable);
    
    /**
     * Find violations by anonymized user ID
     */
    Page<PolicyViolation> findByAnonymizedUserIdOrderByCreatedAtDesc(String anonymizedUserId, Pageable pageable);
    
    /**
     * Find violations by organization
     */
    Page<PolicyViolation> findByOrganizationOrderByCreatedAtDesc(String organization, Pageable pageable);
    
    /**
     * Find violations by organization and device ID
     */
    Page<PolicyViolation> findByOrganizationAndDeviceIdOrderByCreatedAtDesc(
            String organization, String deviceId, Pageable pageable);
    
    /**
     * Find violations by organization within time range
     */
    @Query("SELECT pv FROM PolicyViolation pv WHERE pv.organization = :organization " +
           "AND pv.createdAt BETWEEN :fromDate AND :toDate " +
           "ORDER BY pv.createdAt DESC")
    Page<PolicyViolation> findByOrganizationAndDateRange(
            @Param("organization") String organization,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);
    
    /**
     * Find violations by enforcement action taken
     */
    List<PolicyViolation> findByOrganizationAndActionTakenOrderByCreatedAtDesc(
            String organization, Policy.EnforcementLevel actionTaken);
    
    /**
     * Count violations by organization in time period
     */
    @Query("SELECT COUNT(pv) FROM PolicyViolation pv WHERE pv.organization = :organization " +
           "AND pv.createdAt BETWEEN :fromDate AND :toDate")
    long countByOrganizationAndDateRange(
            @Param("organization") String organization,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);
    
    /**
     * Count violations by device in time period
     */
    @Query("SELECT COUNT(pv) FROM PolicyViolation pv WHERE pv.deviceId = :deviceId " +
           "AND pv.createdAt BETWEEN :fromDate AND :toDate")
    long countByDeviceIdAndDateRange(
            @Param("deviceId") String deviceId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);
    
    /**
     * Count violations by anonymized user in time period
     */
    @Query("SELECT COUNT(pv) FROM PolicyViolation pv WHERE pv.anonymizedUserId = :anonymizedUserId " +
           "AND pv.createdAt BETWEEN :fromDate AND :toDate")
    long countByAnonymizedUserIdAndDateRange(
            @Param("anonymizedUserId") String anonymizedUserId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);
    
    /**
     * Find recent violations for dashboard
     */
    @Query("SELECT pv FROM PolicyViolation pv WHERE pv.organization = :organization " +
           "ORDER BY pv.createdAt DESC")
    List<PolicyViolation> findRecentViolationsByOrganization(
            @Param("organization") String organization, Pageable pageable);
    
    /**
     * Get violation statistics by action taken
     */
    @Query("SELECT pv.actionTaken, COUNT(pv) FROM PolicyViolation pv " +
           "WHERE pv.organization = :organization " +
           "AND pv.createdAt BETWEEN :fromDate AND :toDate " +
           "GROUP BY pv.actionTaken")
    List<Object[]> getViolationStatsByActionTaken(
            @Param("organization") String organization,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);
    
    /**
     * Get violation trends by day
     */
    @Query("SELECT DATE(pv.createdAt), COUNT(pv) FROM PolicyViolation pv " +
           "WHERE pv.organization = :organization " +
           "AND pv.createdAt BETWEEN :fromDate AND :toDate " +
           "GROUP BY DATE(pv.createdAt) ORDER BY DATE(pv.createdAt)")
    List<Object[]> getViolationTrendsByDay(
            @Param("organization") String organization,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);
    
    /**
     * Find top violating devices
     */
    @Query("SELECT pv.deviceId, COUNT(pv) FROM PolicyViolation pv " +
           "WHERE pv.organization = :organization " +
           "AND pv.createdAt BETWEEN :fromDate AND :toDate " +
           "GROUP BY pv.deviceId ORDER BY COUNT(pv) DESC")
    List<Object[]> getTopViolatingDevices(
            @Param("organization") String organization,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);
    
    /**
     * Find violations by policy type
     */
    @Query("SELECT p.policyType, COUNT(pv) FROM PolicyViolation pv JOIN pv.policy p " +
           "WHERE pv.organization = :organization " +
           "AND pv.createdAt BETWEEN :fromDate AND :toDate " +
           "GROUP BY p.policyType ORDER BY COUNT(pv) DESC")
    List<Object[]> getViolationsByPolicyType(
            @Param("organization") String organization,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);
}