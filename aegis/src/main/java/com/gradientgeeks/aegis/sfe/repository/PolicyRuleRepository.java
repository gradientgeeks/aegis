package com.gradientgeeks.aegis.sfe.repository;

import com.gradientgeeks.aegis.sfe.entity.PolicyRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyRuleRepository extends JpaRepository<PolicyRule, Long> {
    
    /**
     * Find all active rules for a specific policy
     */
    List<PolicyRule> findByPolicyIdAndIsActiveTrueOrderByPriorityAsc(Long policyId);
    
    /**
     * Find all rules for a specific policy (including inactive)
     */
    List<PolicyRule> findByPolicyIdOrderByPriorityAsc(Long policyId);
    
    /**
     * Find rules by condition field for a policy
     */
    List<PolicyRule> findByPolicyIdAndConditionFieldAndIsActiveTrueOrderByPriorityAsc(
            Long policyId, String conditionField);
    
    /**
     * Find active rules by operator type for a policy
     */
    List<PolicyRule> findByPolicyIdAndOperatorAndIsActiveTrueOrderByPriorityAsc(
            Long policyId, PolicyRule.RuleOperator operator);
    
    /**
     * Count active rules for a policy
     */
    long countByPolicyIdAndIsActiveTrue(Long policyId);
    
    /**
     * Find rules by priority range for a policy
     */
    @Query("SELECT pr FROM PolicyRule pr WHERE pr.policy.id = :policyId " +
           "AND pr.priority BETWEEN :minPriority AND :maxPriority " +
           "AND pr.isActive = true ORDER BY pr.priority ASC")
    List<PolicyRule> findByPolicyIdAndPriorityRangeAndIsActive(
            @Param("policyId") Long policyId,
            @Param("minPriority") Integer minPriority,
            @Param("maxPriority") Integer maxPriority);
    
    /**
     * Find rules that check specific condition fields across all policies of an organization
     */
    @Query("SELECT pr FROM PolicyRule pr JOIN pr.policy p " +
           "WHERE p.organization = :organization " +
           "AND pr.conditionField = :conditionField " +
           "AND pr.isActive = true AND p.isActive = true " +
           "ORDER BY p.priority ASC, pr.priority ASC")
    List<PolicyRule> findByOrganizationAndConditionFieldAndIsActive(
            @Param("organization") String organization,
            @Param("conditionField") String conditionField);
    
    /**
     * Find all active rules for an organization, ordered by policy and rule priority
     */
    @Query("SELECT pr FROM PolicyRule pr JOIN pr.policy p " +
           "WHERE p.organization = :organization " +
           "AND pr.isActive = true AND p.isActive = true " +
           "ORDER BY p.priority ASC, pr.priority ASC")
    List<PolicyRule> findByOrganizationAndIsActive(@Param("organization") String organization);
}