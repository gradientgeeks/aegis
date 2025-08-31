package com.gradientgeeks.ageis.backendapp.service;

import com.gradientgeeks.ageis.backendapp.dto.LoginRequest;
import com.gradientgeeks.ageis.backendapp.dto.LoginResponse;
import com.gradientgeeks.ageis.backendapp.dto.UserResponse;
import com.gradientgeeks.ageis.backendapp.entity.DeviceRebindingLog;
import com.gradientgeeks.ageis.backendapp.entity.User;
import com.gradientgeeks.ageis.backendapp.exception.AuthenticationException;
import com.gradientgeeks.ageis.backendapp.repository.DeviceRebindingLogRepository;
import com.gradientgeeks.ageis.backendapp.repository.UserRepository;
import com.gradientgeeks.ageis.backendapp.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final DeviceRebindingLogRepository deviceRebindingLogRepository;
    
    @Value("${app.jwt.expiration:86400}")
    private Long jwtExpirationInSeconds;
    
    @Autowired
    public AuthService(UserRepository userRepository, 
                      PasswordEncoder passwordEncoder,
                      JwtTokenProvider jwtTokenProvider,
                      DeviceRebindingLogRepository deviceRebindingLogRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.deviceRebindingLogRepository = deviceRebindingLogRepository;
    }
    
    public LoginResponse login(LoginRequest loginRequest, String deviceId) {
        logger.info("Login attempt for username: {} from device: {}", loginRequest.getUsername(), deviceId);
        
        // Find user by username
        User user = userRepository.findByUsernameAndIsActiveTrue(loginRequest.getUsername())
                .orElseThrow(() -> {
                    logger.warn("Login failed: User not found or inactive - {}", loginRequest.getUsername());
                    return new AuthenticationException("Invalid username or password");
                });
        
        // Verify password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            logger.warn("Login failed: Invalid password for user - {}", loginRequest.getUsername());
            throw new AuthenticationException("Invalid username or password");
        }
        
        // Device binding validation
        DeviceBindingResult bindingResult = validateDeviceBinding(user, deviceId);
        
        if (bindingResult.isBlocked()) {
            logger.warn("Login blocked due to device binding violation - User: {}, Device: {}, Reason: {}", 
                user.getUsername(), deviceId, bindingResult.getReason());
            throw new AuthenticationException(bindingResult.getReason());
        }
        
        if (bindingResult.requiresRebinding()) {
            logger.info("Device rebinding required for user: {} on device: {}", user.getUsername(), deviceId);
            throw new AuthenticationException("Device verification required. Please complete identity verification.");
        }
        
        // If this is a first login (no device bound), bind the device
        if (!user.hasDeviceBinding()) {
            user.bindToDevice(deviceId);
            userRepository.save(user);
            logger.info("Device bound successfully - User: {}, Device: {}", user.getUsername(), deviceId);
        }
        
        // Generate JWT token
        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getId());
        
        // Create user response
        UserResponse userResponse = UserResponse.fromUser(user);
        
        // Create and return login response
        LoginResponse loginResponse = new LoginResponse(token, jwtExpirationInSeconds, userResponse);
        
        logger.info("Login successful for user: {} from device: {}", user.getUsername(), deviceId);
        return loginResponse;
    }
    
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("User not found"));
    }
    
    public boolean validateUser(String username) {
        return userRepository.findByUsernameAndIsActiveTrue(username).isPresent();
    }
    
    /**
     * Validates device binding for a user.
     * 
     * @param user The user attempting to log in
     * @param deviceId The device ID from the request
     * @return DeviceBindingResult indicating the validation result
     */
    private DeviceBindingResult validateDeviceBinding(User user, String deviceId) {
        // If user has no device binding, allow (will bind on successful login)
        if (!user.hasDeviceBinding()) {
            logger.debug("No device binding found for user: {}, will bind to device: {}", user.getUsername(), deviceId);
            return DeviceBindingResult.allow();
        }
        
        // If user requires rebinding, block login until verification
        if (user.getRequiresDeviceRebinding() != null && user.getRequiresDeviceRebinding()) {
            logger.warn("User {} requires device rebinding", user.getUsername());
            return DeviceBindingResult.requireRebinding("Account requires device verification");
        }
        
        // If device matches bound device, allow
        if (user.isDeviceBound(deviceId)) {
            logger.debug("Device binding validated for user: {} on device: {}", user.getUsername(), deviceId);
            return DeviceBindingResult.allow();
        }
        
        // No special handling for device ID format migration here
        // Device consistency is now handled by Aegis using hardware fingerprint
        
        // Different device attempting login - require rebinding
        logger.warn("Login attempt from unbound device - User: {}, Bound Device: {}, Attempted Device: {}", 
            user.getUsername(), user.getBoundDeviceId(), deviceId);
        
        // Mark user as requiring rebinding
        user.requireDeviceRebinding();
        userRepository.save(user);
        
        return DeviceBindingResult.requireRebinding(
            "Login detected from new device. Please complete identity verification to continue.");
    }
    
    
    /**
     * Initiates device rebinding process for a user.
     * This would typically involve additional identity verification steps.
     * 
     * @param username The username
     * @param newDeviceId The new device ID to bind to
     * @param verificationMethod The verification method used (e.g., "AADHAAR_OTP", "EMAIL_VERIFICATION")
     * @return true if rebinding was successful
     */
    public boolean rebindDevice(String username, String newDeviceId, String verificationMethod) {
        logger.info("Device rebinding requested - User: {}, New Device: {}, Method: {}", 
            username, newDeviceId, verificationMethod);
        
        User user = null;
        String oldDeviceId = null;
        DeviceRebindingLog rebindingLog = null;
        
        try {
            user = getUserByUsername(username);
            oldDeviceId = user.getBoundDeviceId();
            
            // Create rebinding log entry
            rebindingLog = new DeviceRebindingLog(user, oldDeviceId, newDeviceId, verificationMethod, false);
            
            // Update user's device binding
            user.bindToDevice(newDeviceId);
            userRepository.save(user);
            
            // Mark rebinding as successful
            rebindingLog.setSuccess(true);
            deviceRebindingLogRepository.save(rebindingLog);
            
            logger.info("Device rebinding successful - User: {}, Old Device: {}, New Device: {}", 
                username, oldDeviceId, newDeviceId);
            
            return true;
            
        } catch (Exception e) {
            logger.error("Device rebinding failed - User: {}, Device: {}", username, newDeviceId, e);
            
            // Log failed rebinding attempt
            if (user != null && rebindingLog == null) {
                rebindingLog = new DeviceRebindingLog(user, oldDeviceId, newDeviceId, verificationMethod, false);
            }
            if (rebindingLog != null) {
                rebindingLog.setSuccess(false);
                rebindingLog.setFailureReason(e.getMessage());
                deviceRebindingLogRepository.save(rebindingLog);
            }
            
            return false;
        }
    }
    
    /**
     * Checks if a user requires device rebinding.
     * 
     * @param username The username to check
     * @return true if rebinding is required
     */
    public boolean requiresDeviceRebinding(String username) {
        try {
            User user = getUserByUsername(username);
            return user.getRequiresDeviceRebinding() != null && user.getRequiresDeviceRebinding();
        } catch (Exception e) {
            logger.error("Error checking device rebinding requirement for user: {}", username, e);
            return false;
        }
    }
}

/**
 * Result of device binding validation.
 */
class DeviceBindingResult {
    private final boolean allowed;
    private final boolean requiresRebinding;
    private final String reason;
    
    private DeviceBindingResult(boolean allowed, boolean requiresRebinding, String reason) {
        this.allowed = allowed;
        this.requiresRebinding = requiresRebinding;
        this.reason = reason;
    }
    
    public static DeviceBindingResult allow() {
        return new DeviceBindingResult(true, false, null);
    }
    
    public static DeviceBindingResult block(String reason) {
        return new DeviceBindingResult(false, false, reason);
    }
    
    public static DeviceBindingResult requireRebinding(String reason) {
        return new DeviceBindingResult(false, true, reason);
    }
    
    public boolean isAllowed() {
        return allowed;
    }
    
    public boolean isBlocked() {
        return !allowed && !requiresRebinding;
    }
    
    public boolean requiresRebinding() {
        return requiresRebinding;
    }
    
    public String getReason() {
        return reason;
    }
}