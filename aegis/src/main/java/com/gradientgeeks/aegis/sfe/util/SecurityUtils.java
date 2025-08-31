package com.gradientgeeks.aegis.sfe.util;

import com.gradientgeeks.aegis.sfe.entity.User;
import com.gradientgeeks.aegis.sfe.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utility class for security-related operations
 */
@Component
public class SecurityUtils {
    
    private final UserRepository userRepository;
    
    public SecurityUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Get the current authenticated user
     * @return the current user or null if not authenticated
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            return userRepository.findByEmail(email).orElse(null);
        }
        return null;
    }
    
    /**
     * Get the organization of the current authenticated user
     * @return the organization name or null if not authenticated
     */
    public String getCurrentUserOrganization() {
        User user = getCurrentUser();
        return user != null ? user.getOrganization() : null;
    }
    
    /**
     * Check if the current user is an admin
     * @return true if the user has ADMIN role
     */
    public boolean isAdmin() {
        User user = getCurrentUser();
        return user != null && User.UserRole.ADMIN.equals(user.getRole());
    }
}