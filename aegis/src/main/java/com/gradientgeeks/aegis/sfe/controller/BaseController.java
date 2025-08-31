package com.gradientgeeks.aegis.sfe.controller;

import com.gradientgeeks.aegis.sfe.entity.User;
import com.gradientgeeks.aegis.sfe.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Base controller with common functionality
 */
public abstract class BaseController {
    
    @Autowired
    protected AuthService authService;
    
    /**
     * Gets the current user's organization from the security context
     * @return organization name or null if not authenticated
     */
    protected String getCurrentUserOrganization() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            if (email != null && !email.equals("anonymousUser")) {
                try {
                    User user = authService.getUserByEmail(email);
                    if (user != null) {
                        return user.getOrganization();
                    }
                } catch (Exception e) {
                    // Log error but don't fail
                }
            }
        }
        return null;
    }
}