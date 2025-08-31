package com.gradientgeeks.aegis.sfe.service;

import com.gradientgeeks.aegis.sfe.dto.OrganizationDto;
import com.gradientgeeks.aegis.sfe.entity.User;
import com.gradientgeeks.aegis.sfe.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for admin operations
 */
@Service
@Transactional
public class AdminService {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
    
    private final UserRepository userRepository;
    
    @Autowired
    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Get all organizations pending approval
     * @return List of pending organizations
     */
    public List<OrganizationDto> getPendingOrganizations() {
        List<User> pendingUsers = userRepository.findByApprovalStatusAndRole(
            User.ApprovalStatus.PENDING, User.UserRole.USER);
        
        return pendingUsers.stream()
            .map(OrganizationDto::new)
            .collect(Collectors.toList());
    }
    
    /**
     * Get all organizations
     * @return List of all organizations
     */
    public List<OrganizationDto> getAllOrganizations() {
        List<User> allUsers = userRepository.findByRole(User.UserRole.USER);
        
        return allUsers.stream()
            .map(OrganizationDto::new)
            .collect(Collectors.toList());
    }
    
    /**
     * Approve an organization
     * @param userId the user ID to approve
     * @param approvedBy the admin who approved
     * @return Updated organization details
     */
    public OrganizationDto approveOrganization(Long userId, String approvedBy) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getRole() != User.UserRole.USER) {
            throw new RuntimeException("Cannot approve admin users");
        }
        
        if (user.getApprovalStatus() == User.ApprovalStatus.APPROVED) {
            throw new RuntimeException("Organization already approved");
        }
        
        user.setApprovalStatus(User.ApprovalStatus.APPROVED);
        user.setApprovedAt(LocalDateTime.now());
        user.setApprovedBy(approvedBy);
        user.setActive(true);
        
        User savedUser = userRepository.save(user);
        logger.info("Organization {} approved by {}", user.getOrganization(), approvedBy);
        
        return new OrganizationDto(savedUser);
    }
    
    /**
     * Reject an organization
     * @param userId the user ID to reject
     * @param rejectedBy the admin who rejected
     * @return Updated organization details
     */
    public OrganizationDto rejectOrganization(Long userId, String rejectedBy) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getRole() != User.UserRole.USER) {
            throw new RuntimeException("Cannot reject admin users");
        }
        
        if (user.getApprovalStatus() == User.ApprovalStatus.REJECTED) {
            throw new RuntimeException("Organization already rejected");
        }
        
        user.setApprovalStatus(User.ApprovalStatus.REJECTED);
        user.setApprovedAt(LocalDateTime.now());
        user.setApprovedBy(rejectedBy);
        user.setActive(false);
        
        User savedUser = userRepository.save(user);
        logger.info("Organization {} rejected by {}", user.getOrganization(), rejectedBy);
        
        return new OrganizationDto(savedUser);
    }
}