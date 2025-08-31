package com.gradientgeeks.aegis.sfe.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Interceptor to extract organization from authenticated user and add as header
 */
@Component
public class OrganizationHeaderInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Get organization from request attribute (set by JWT filter)
        Object userOrganization = request.getAttribute("userOrganization");
        
        if (userOrganization != null) {
            // Add organization as a header for controllers to access
            response.setHeader("X-User-Organization", userOrganization.toString());
            // Also keep it in request attribute
            request.setAttribute("X-User-Organization", userOrganization.toString());
        }
        
        return true;
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // No post-processing needed
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // No cleanup needed
    }
}