package com.gradientgeeks.aegis.sfe.repository;

import com.gradientgeeks.aegis.sfe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository interface for User entity operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find a user by email address
     * @param email the email address
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if a user exists with the given email
     * @param email the email address
     * @return true if user exists, false otherwise
     */
    boolean existsByEmail(String email);
    
    /**
     * Find all active users
     * @return List of active users
     */
    java.util.List<User> findByActiveTrue();
    
    /**
     * Find users by organization
     * @param organization the organization name
     * @return List of users in the organization
     */
    java.util.List<User> findByOrganization(String organization);
    
    /**
     * Find users by approval status and role
     * @param approvalStatus the approval status
     * @param role the user role
     * @return List of users matching the criteria
     */
    java.util.List<User> findByApprovalStatusAndRole(User.ApprovalStatus approvalStatus, User.UserRole role);
    
    /**
     * Find users by role
     * @param role the user role
     * @return List of users with the specified role
     */
    java.util.List<User> findByRole(User.UserRole role);
}