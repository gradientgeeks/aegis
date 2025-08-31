package com.gradientgeeks.aegis.sfe.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Autowired
    private OrganizationHeaderInterceptor organizationHeaderInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(organizationHeaderInterceptor)
                .addPathPatterns("/admin/**") // Apply to admin endpoints
                .excludePathPatterns("/auth/**", "/v1/**"); // Exclude auth and device endpoints
    }
}