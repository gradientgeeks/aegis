package com.gradientgeeks.ageis.backendapp.controller;

import com.gradientgeeks.ageis.backendapp.dto.*;
import com.gradientgeeks.ageis.backendapp.entity.*;
import com.gradientgeeks.ageis.backendapp.service.*;
import com.gradientgeeks.ageis.backendapp.security.JwtTokenProvider;
import com.gradientgeeks.ageis.backendapp.exception.AuthenticationException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    
    private final UserService userService;
    private final TransactionService transactionService;
    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    public AdminController(UserService userService, TransactionService transactionService,
                         AuthService authService, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.transactionService = transactionService;
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    // Admin login endpoint
    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            logger.info("Admin login request for username: {}", loginRequest.getUsername());
            
            // Verify admin credentials (in production, check against admin users table)
            if (!isAdminUser(loginRequest.getUsername(), loginRequest.getPassword())) {
                throw new AuthenticationException("Invalid admin credentials");
            }
            
            // Generate admin token
            String token = jwtTokenProvider.generateToken(loginRequest.getUsername(), 999L); // Admin ID
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", 999L);
            response.put("username", loginRequest.getUsername());
            response.put("role", "ADMIN");
            response.put("token", token);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Admin login failed: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Invalid credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
    
    // Get admin profile
    @GetMapping("/profile")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAdminProfile(@RequestHeader("Authorization") String token) {
        try {
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", 999L);
            profile.put("username", "admin");
            profile.put("role", "ADMIN");
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            logger.error("Error fetching admin profile: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Dashboard statistics
    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getDashboardStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Get user statistics
            List<User> allUsers = userService.getAllUsers();
            stats.put("totalUsers", allUsers.size());
            stats.put("activeUsers", allUsers.stream()
                .filter(u -> u.getDeviceIds() != null && !u.getDeviceIds().isEmpty())
                .count());
            
            // Get transaction statistics
            List<Transaction> todayTransactions = transactionService.getTransactionsByDate(LocalDateTime.now());
            stats.put("todayTransactions", todayTransactions.size());
            stats.put("failedTransactions", todayTransactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.FAILED)
                .count());
            
            // Get device statistics (mock data for now)
            stats.put("blockedDevices", 3);
            stats.put("suspiciousActivities", 5);
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            logger.error("Error fetching dashboard stats: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Get all users
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            List<Map<String, Object>> userList = users.stream()
                .map(this::mapUserToResponse)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(userList);
            
        } catch (Exception e) {
            logger.error("Error fetching users: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Get user by ID
    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(mapUserToResponse(user));
            
        } catch (Exception e) {
            logger.error("Error fetching user {}: ", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Get user devices
    @GetMapping("/users/{userId}/devices")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserDevices(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            List<Map<String, Object>> devices = new ArrayList<>();
            if (user.getDeviceIds() != null) {
                for (String deviceId : user.getDeviceIds()) {
                    Map<String, Object> device = new HashMap<>();
                    device.put("deviceId", deviceId);
                    device.put("userId", userId);
                    device.put("username", user.getUsername());
                    device.put("status", "active"); // In production, check with Aegis
                    device.put("lastUsed", LocalDateTime.now().minusHours(2));
                    device.put("registeredAt", user.getCreatedAt());
                    
                    // Mock device info
                    Map<String, String> deviceInfo = new HashMap<>();
                    deviceInfo.put("model", "Samsung Galaxy S21");
                    deviceInfo.put("os", "Android");
                    deviceInfo.put("version", "12");
                    device.put("deviceInfo", deviceInfo);
                    
                    devices.add(device);
                }
            }
            
            return ResponseEntity.ok(devices);
            
        } catch (Exception e) {
            logger.error("Error fetching user devices: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Get all transactions with filters
    @GetMapping("/transactions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllTransactions(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String status) {
        
        try {
            List<Transaction> transactions = transactionService.getAllTransactions();
            
            // Apply filters
            if (userId != null) {
                transactions = transactions.stream()
                    .filter(t -> {
                    // For now, we'll need to look up the account to get the user
                    // In a real app, you'd join this in the query
                    return false; // TODO: Implement account lookup
                })
                    .collect(Collectors.toList());
            }
            
            if (deviceId != null && !deviceId.isEmpty()) {
                transactions = transactions.stream()
                    .filter(t -> deviceId.equals(t.getDeviceId()))
                    .collect(Collectors.toList());
            }
            
            if (status != null && !status.isEmpty()) {
                TransactionStatus txStatus = TransactionStatus.valueOf(status);
                transactions = transactions.stream()
                    .filter(t -> t.getStatus() == txStatus)
                    .collect(Collectors.toList());
            }
            
            List<Map<String, Object>> result = transactions.stream()
                .map(this::mapTransactionToResponse)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Error fetching transactions: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Get recent transactions
    @GetMapping("/transactions/recent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getRecentTransactions(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<Transaction> transactions = transactionService.getRecentTransactions(limit);
            List<Map<String, Object>> result = transactions.stream()
                .map(this::mapTransactionToResponse)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Error fetching recent transactions: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Get all devices
    @GetMapping("/devices")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllDevices() {
        try {
            List<Map<String, Object>> devices = new ArrayList<>();
            
            // Get all users and their devices
            List<User> users = userService.getAllUsers();
            for (User user : users) {
                if (user.getDeviceIds() != null) {
                    for (String deviceId : user.getDeviceIds()) {
                        Map<String, Object> device = new HashMap<>();
                        device.put("deviceId", deviceId);
                        device.put("userId", user.getId());
                        device.put("username", user.getUsername());
                        device.put("status", "active");
                        device.put("lastUsed", LocalDateTime.now().minusHours(1));
                        device.put("registeredAt", user.getCreatedAt());
                        
                        // Mock device info
                        Map<String, String> deviceInfo = new HashMap<>();
                        deviceInfo.put("model", "Device Model");
                        deviceInfo.put("os", "Android");
                        deviceInfo.put("version", "12");
                        device.put("deviceInfo", deviceInfo);
                        
                        devices.add(device);
                    }
                }
            }
            
            return ResponseEntity.ok(devices);
            
        } catch (Exception e) {
            logger.error("Error fetching devices: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Get suspicious activities
    @GetMapping("/suspicious-activities")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getSuspiciousActivities() {
        try {
            List<Map<String, Object>> activities = new ArrayList<>();
            
            // Mock suspicious activities
            Map<String, Object> activity1 = new HashMap<>();
            activity1.put("id", "SA001");
            activity1.put("type", "multiple_failed_transactions");
            activity1.put("userId", 1L);
            activity1.put("username", "john_doe");
            activity1.put("deviceId", "device_123");
            activity1.put("description", "5 failed transaction attempts in 10 minutes");
            activity1.put("timestamp", LocalDateTime.now().minusHours(2));
            activity1.put("severity", "high");
            activity1.put("details", Map.of("failedAttempts", 5, "timeWindow", "10 minutes"));
            
            activities.add(activity1);
            
            return ResponseEntity.ok(activities);
            
        } catch (Exception e) {
            logger.error("Error fetching suspicious activities: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Helper methods
    private boolean isAdminUser(String username, String password) {
        // In production, verify against admin users table
        return "admin".equals(username) && "admin123".equals(password);
    }
    
    private Map<String, Object> mapUserToResponse(User user) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("name", user.getFullName());
        response.put("deviceIds", user.getDeviceIds() != null ? user.getDeviceIds() : new ArrayList<>());
        response.put("createdAt", user.getCreatedAt());
        response.put("lastLogin", user.getLastLogin());
        response.put("status", "active");
        return response;
    }
    
    private Map<String, Object> mapTransactionToResponse(Transaction tx) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", tx.getId());
        // For simplicity, we'll use mock user data for now
        // In a real app, you'd join with accounts and users tables
        response.put("userId", 1L); // Mock user ID
        response.put("username", "user1"); // Mock username
        response.put("fromAccountId", tx.getFromAccountId());
        response.put("toAccountId", tx.getToAccountId());
        response.put("amount", tx.getAmount());
        response.put("type", tx.getTransactionType().toString());
        response.put("status", tx.getStatus().toString());
        response.put("deviceId", tx.getDeviceId() != null ? tx.getDeviceId() : "unknown");
        response.put("timestamp", tx.getCreatedAt());
        response.put("description", tx.getDescription());
        return response;
    }
}