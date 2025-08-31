package com.gradientgeeks.aegis.sfe.service;

import com.gradientgeeks.aegis.sfe.dto.AuthResponse;
import com.gradientgeeks.aegis.sfe.dto.LoginRequest;
import com.gradientgeeks.aegis.sfe.entity.User;
import com.gradientgeeks.aegis.sfe.repository.UserRepository;
import com.gradientgeeks.aegis.sfe.security.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for handling authentication operations
 */
@Service
@Transactional
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    
    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }
    
    /**
     * Authenticate user with email and password
     * @param loginRequest the login credentials
     * @return AuthResponse with token and user details
     * @throws RuntimeException if authentication fails
     */
    public AuthResponse authenticate(LoginRequest loginRequest) {
        logger.info("Authentication attempt for email: {}", loginRequest.getEmail());
        
        User user = userRepository.findByEmail(loginRequest.getEmail())
            .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        
        if (!user.getActive()) {
            logger.warn("Inactive user attempted login: {}", loginRequest.getEmail());
            throw new RuntimeException("Account is inactive");
        }
        
        // Check approval status for organization users
        if (user.getRole() == User.UserRole.USER) {
            if (user.getApprovalStatus() == User.ApprovalStatus.PENDING) {
                logger.warn("Pending approval user attempted login: {}", loginRequest.getEmail());
                throw new RuntimeException("Account is pending approval");
            } else if (user.getApprovalStatus() == User.ApprovalStatus.REJECTED) {
                logger.warn("Rejected user attempted login: {}", loginRequest.getEmail());
                throw new RuntimeException("Account approval was rejected");
            }
        }
        
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            logger.warn("Invalid password attempt for email: {}", loginRequest.getEmail());
            throw new RuntimeException("Invalid email or password");
        }
        
        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        
        // Generate JWT token
        String token = jwtUtils.generateJwtToken(user);
        
        logger.info("User authenticated successfully: {}", user.getEmail());
        
        return new AuthResponse(
            token,
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getOrganization(),
            user.getRole().toString()
        );
    }
    
    /**
     * Validate token and get user
     * @param token the JWT token
     * @return User if token is valid
     */
    public User validateTokenAndGetUser(String token) {
        try {
            if (jwtUtils.validateJwtToken(token)) {
                String email = jwtUtils.getEmailFromJwtToken(token);
                return userRepository.findByEmail(email)
                    .filter(User::getActive)
                    .orElse(null);
            }
        } catch (Exception e) {
            logger.error("Token validation failed", e);
        }
        return null;
    }
    
    /**
     * Create a new user
     * @param user the user to create
     * @return created user
     */
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        // Encrypt password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        return userRepository.save(user);
    }
    
    /**
     * Get user by email
     * @param email the email to search for
     * @return the user or null if not found
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}