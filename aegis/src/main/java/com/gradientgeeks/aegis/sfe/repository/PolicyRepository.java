package com.gradientgeeks.aegis.sfe.repository;

import com.gradientgeeks.aegis.sfe.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {
    
    /**
     * Find all active policies for a specific organization
     */
    List<Policy> findByOrganizationAndIsActiveTrueOrderByPriorityAsc(String organization);
    
    /**
     * Find all policies for a specific organization (including inactive)
     */
    List<Policy> findByOrganizationOrderByCreatedAtDesc(String organization);
    
    /**
     * Find active policies by organization and policy type
     */
    List<Policy> findByOrganizationAndPolicyTypeAndIsActiveTrueOrderByPriorityAsc(
            String organization, Policy.PolicyType policyType);
    
    /**
     * Find policy by name and organization
     */
    Optional<Policy> findByPolicyNameAndOrganization(String policyName, String organization);
    
    /**
     * Check if a policy name already exists for an organization
     */
    boolean existsByPolicyNameAndOrganization(String policyName, String organization);
    
    /**
     * Find policies with specific enforcement level for an organization
     */
    List<Policy> findByOrganizationAndEnforcementLevelAndIsActiveTrueOrderByPriorityAsc(
            String organization, Policy.EnforcementLevel enforcementLevel);
    
    /**
     * Count active policies for an organization
     */
    long countByOrganizationAndIsActiveTrue(String organization);
    
    /**
     * Find all organizations that have policies
     */
    @Query("SELECT DISTINCT p.organization FROM Policy p")
    List<String> findDistinctOrganizations();
    
    /**
     * Find policies that apply to specific policy types for an organization
     */
    @Query("SELECT p FROM Policy p WHERE p.organization = :organization " +
           "AND p.policyType IN :policyTypes AND p.isActive = true " +
           "ORDER BY p.priority ASC")
    List<Policy> findByOrganizationAndPolicyTypesAndIsActive(
            @Param("organization") String organization, 
            @Param("policyTypes") List<Policy.PolicyType> policyTypes);
    
    /**
     * Find policies by priority range for an organization
     */
    @Query("SELECT p FROM Policy p WHERE p.organization = :organization " +
           "AND p.priority BETWEEN :minPriority AND :maxPriority " +
           "AND p.isActive = true ORDER BY p.priority ASC")
    List<Policy> findByOrganizationAndPriorityRangeAndIsActive(
            @Param("organization") String organization,
            @Param("minPriority") Integer minPriority,
            @Param("maxPriority") Integer maxPriority);
}