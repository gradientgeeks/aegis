package com.gradientgeeks.ageis.backendapp.config;

import com.gradientgeeks.ageis.backendapp.filter.AegisAuthenticationFilter;
import com.gradientgeeks.ageis.backendapp.filter.JwtAuthenticationFilter;
import com.gradientgeeks.ageis.backendapp.security.AegisSecurityInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Security configuration for the UCO Bank Backend.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig implements WebMvcConfigurer {
    
    private final AegisSecurityInterceptor aegisSecurityInterceptor;
    private final AegisAuthenticationFilter aegisAuthenticationFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    public SecurityConfig(AegisSecurityInterceptor aegisSecurityInterceptor,
                         AegisAuthenticationFilter aegisAuthenticationFilter,
                         JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.aegisSecurityInterceptor = aegisSecurityInterceptor;
        this.aegisAuthenticationFilter = aegisAuthenticationFilter;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(request -> {
                var corsConfig = new org.springframework.web.cors.CorsConfiguration();
                corsConfig.setAllowedOrigins(java.util.List.of("*"));
                corsConfig.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                corsConfig.setAllowedHeaders(java.util.List.of("*"));
                corsConfig.setMaxAge(3600L);
                return corsConfig;
            }))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()  // Allow all OPTIONS requests
                .requestMatchers("/api/v1/health/**", "/actuator/**").permitAll()
                .requestMatchers("/api/v1/auth/**").permitAll()  // Allow auth endpoints
                .requestMatchers("/api/admin/login").permitAll()  // Allow admin login
                .requestMatchers("/api/admin/**").authenticated()  // Protect other admin endpoints
                .requestMatchers("/api/v1/**").authenticated()
                .anyRequest().denyAll()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(aegisAuthenticationFilter, JwtAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(aegisSecurityInterceptor)
                .addPathPatterns("/api/v1/**")
                .excludePathPatterns("/api/v1/health/**", "/api/v1/auth/**");
    }
}