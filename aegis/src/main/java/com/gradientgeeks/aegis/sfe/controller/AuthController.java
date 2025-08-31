package com.gradientgeeks.aegis.sfe.controller;

import com.gradientgeeks.aegis.sfe.dto.AuthResponse;
import com.gradientgeeks.aegis.sfe.dto.LoginRequest;
import com.gradientgeeks.aegis.sfe.dto.RegistrationRequest;
import com.gradientgeeks.aegis.sfe.entity.User;
import com.gradientgeeks.aegis.sfe.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for handling authentication endpoints
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final AuthService authService;
    
    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    /**
     * Login endpoint
     * @param loginRequest the login credentials
     * @return AuthResponse with token and user details
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Login request received for email: {}", loginRequest.getEmail());
        
        try {
            AuthResponse authResponse = authService.authenticate(loginRequest);
            return ResponseEntity.ok(authResponse);
        } catch (RuntimeException e) {
            logger.error("Login failed for email: {}", loginRequest.getEmail(), e);
            
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            error.put("status", "error");
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
    
    /**
     * Register a new organization
     * @param registrationRequest the registration details
     * @return AuthResponse with token and user details
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegistrationRequest registrationRequest) {
        logger.info("Registration request received for organization: {}", registrationRequest.getOrganizationName());
        
        try {
            // Create user from registration request
            User newUser = new User();
            newUser.setEmail(registrationRequest.getEmail());
            newUser.setPassword(registrationRequest.getPassword()); // Will be encrypted in service
            newUser.setName(registrationRequest.getContactPerson());
            newUser.setOrganization(registrationRequest.getOrganizationName());
            newUser.setContactPerson(registrationRequest.getContactPerson());
            newUser.setPhone(registrationRequest.getPhone());
            newUser.setAddress(registrationRequest.getAddress());
            newUser.setRole(User.UserRole.USER);
            
            User createdUser = authService.createUser(newUser);
            
            // Auto-login after registration
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setEmail(registrationRequest.getEmail());
            loginRequest.setPassword(registrationRequest.getPassword());
            
            AuthResponse authResponse = authService.authenticate(loginRequest);
            
            logger.info("Organization registered successfully: {}", registrationRequest.getOrganizationName());
            return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
            
        } catch (RuntimeException e) {
            logger.error("Registration failed for organization: {}", registrationRequest.getOrganizationName(), e);
            
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            error.put("status", "error");
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    /**
     * Health check endpoint
     * @return status message
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        response.put("message", "Authentication service is running");
        return ResponseEntity.ok(response);
    }
}