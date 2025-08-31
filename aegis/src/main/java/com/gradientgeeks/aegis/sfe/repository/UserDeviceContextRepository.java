package com.gradientgeeks.aegis.sfe.repository;

import com.gradientgeeks.aegis.sfe.entity.UserDeviceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeviceContextRepository extends JpaRepository<UserDeviceContext, Long> {
    
    /**
     * Find user-device context by anonymized user ID, device ID and organization
     */
    Optional<UserDeviceContext> findByAnonymizedUserIdAndDeviceIdAndOrganization(
            String anonymizedUserId, String deviceId, String organization);
    
    /**
     * Find all contexts for an anonymized user ID within an organization
     */
    List<UserDeviceContext> findByAnonymizedUserIdAndOrganizationOrderByLastActivityAtDesc(
            String anonymizedUserId, String organization);
    
    /**
     * Find all contexts for a device ID within an organization
     */
    List<UserDeviceContext> findByDeviceIdAndOrganizationOrderByLastActivityAtDesc(
            String deviceId, String organization);
    
    /**
     * Find contexts by organization
     */
    Page<UserDeviceContext> findByOrganizationOrderByLastActivityAtDesc(String organization, Pageable pageable);
    
    /**
     * Find contexts with high risk scores
     */
    @Query("SELECT udc FROM UserDeviceContext udc WHERE udc.organization = :organization " +
           "AND udc.riskScore >= :minRiskScore ORDER BY udc.riskScore DESC")
    List<UserDeviceContext> findByOrganizationAndHighRiskScore(
            @Param("organization") String organization,
            @Param("minRiskScore") Integer minRiskScore);
    
    /**
     * Find contexts with unusual patterns
     */
    @Query("SELECT udc FROM UserDeviceContext udc WHERE udc.organization = :organization " +
           "AND udc.unusualPatterns IS NOT NULL AND udc.unusualPatterns != '' " +
           "ORDER BY udc.lastActivityAt DESC")
    List<UserDeviceContext> findByOrganizationWithUnusualPatterns(@Param("organization") String organization);
    
    /**
     * Find contexts with high transaction velocity
     */
    @Query("SELECT udc FROM UserDeviceContext udc WHERE udc.organization = :organization " +
           "AND udc.dailyTransactionCount >= :minDailyCount " +
           "ORDER BY udc.dailyTransactionCount DESC")
    List<UserDeviceContext> findByOrganizationAndHighTransactionVelocity(
            @Param("organization") String organization,
            @Param("minDailyCount") Integer minDailyCount);
    
    /**
     * Find dormant accounts that became active
     */
    @Query("SELECT udc FROM UserDeviceContext udc WHERE udc.organization = :organization " +
           "AND udc.isDormantAccount = true " +
           "AND udc.lastActivityAt BETWEEN :fromDate AND :toDate " +
           "ORDER BY udc.lastActivityAt DESC")
    List<UserDeviceContext> findReactivatedDormantAccounts(
            @Param("organization") String organization,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);
    
    /**
     * Find contexts with device changes
     */
    List<UserDeviceContext> findByOrganizationAndIsDeviceChangedTrueOrderByLastActivityAtDesc(String organization);
    
    /**
     * Find contexts with location changes
     */
    List<UserDeviceContext> findByOrganizationAndIsLocationChangedTrueOrderByLastActivityAtDesc(String organization);
    
    /**
     * Find contexts with failed attempts above threshold
     */
    @Query("SELECT udc FROM UserDeviceContext udc WHERE udc.organization = :organization " +
           "AND udc.failedAttemptsCount >= :minFailedAttempts " +
           "ORDER BY udc.failedAttemptsCount DESC")
    List<UserDeviceContext> findByOrganizationAndHighFailedAttempts(
            @Param("organization") String organization,
            @Param("minFailedAttempts") Integer minFailedAttempts);
    
    /**
     * Count active users by organization
     */
    @Query("SELECT COUNT(DISTINCT udc.anonymizedUserId) FROM UserDeviceContext udc " +
           "WHERE udc.organization = :organization " +
           "AND udc.lastActivityAt >= :sinceDate")
    long countActiveUsersByOrganization(
            @Param("organization") String organization,
            @Param("sinceDate") LocalDateTime sinceDate);
    
    /**
     * Count active devices by organization
     */
    @Query("SELECT COUNT(DISTINCT udc.deviceId) FROM UserDeviceContext udc " +
           "WHERE udc.organization = :organization " +
           "AND udc.lastActivityAt >= :sinceDate")
    long countActiveDevicesByOrganization(
            @Param("organization") String organization,
            @Param("sinceDate") LocalDateTime sinceDate);
    
    /**
     * Get user activity statistics
     */
    @Query("SELECT udc.accountTier, COUNT(udc), AVG(udc.dailyTransactionCount), AVG(udc.riskScore) " +
           "FROM UserDeviceContext udc WHERE udc.organization = :organization " +
           "AND udc.lastActivityAt >= :sinceDate " +
           "GROUP BY udc.accountTier")
    List<Object[]> getUserActivityStatsByTier(
            @Param("organization") String organization,
            @Param("sinceDate") LocalDateTime sinceDate);
    
    /**
     * Reset daily transaction counts (for daily batch jobs)
     */
    @Modifying
    @Query("UPDATE UserDeviceContext udc SET udc.dailyTransactionCount = 0 " +
           "WHERE udc.organization = :organization")
    int resetDailyTransactionCounts(@Param("organization") String organization);
    
    /**
     * Reset weekly transaction counts (for weekly batch jobs)
     */
    @Modifying
    @Query("UPDATE UserDeviceContext udc SET udc.weeklyTransactionCount = 0 " +
           "WHERE udc.organization = :organization")
    int resetWeeklyTransactionCounts(@Param("organization") String organization);
    
    /**
     * Reset monthly transaction counts (for monthly batch jobs)
     */
    @Modifying
    @Query("UPDATE UserDeviceContext udc SET udc.monthlyTransactionCount = 0 " +
           "WHERE udc.organization = :organization")
    int resetMonthlyTransactionCounts(@Param("organization") String organization);
    
    /**
     * Find contexts by client ID
     */
    List<UserDeviceContext> findByClientIdAndOrganizationOrderByLastActivityAtDesc(
            String clientId, String organization);
    
    /**
     * Find contexts that haven't been active for a period (for cleanup)
     */
    @Query("SELECT udc FROM UserDeviceContext udc WHERE udc.organization = :organization " +
           "AND udc.lastActivityAt < :beforeDate " +
           "ORDER BY udc.lastActivityAt ASC")
    List<UserDeviceContext> findInactiveContexts(
            @Param("organization") String organization,
            @Param("beforeDate") LocalDateTime beforeDate,
            Pageable pageable);
}