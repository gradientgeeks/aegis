package com.gradientgeeks.ageis.backendapp.service;

import com.gradientgeeks.ageis.backendapp.entity.User;
import com.gradientgeeks.ageis.backendapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public List<User> getAllUsers() {
        logger.info("Fetching all users");
        return userRepository.findAll();
    }
    
    public User getUserById(Long userId) {
        logger.info("Fetching user by ID: {}", userId);
        Optional<User> user = userRepository.findById(userId);
        return user.orElse(null);
    }
    
    public User getUserByUsername(String username) {
        logger.info("Fetching user by username: {}", username);
        return userRepository.findByUsername(username).orElse(null);
    }
    
    public User getUserByEmail(String email) {
        logger.info("Fetching user by email: {}", email);
        return userRepository.findByEmail(email).orElse(null);
    }
    
    public User createUser(User user) {
        logger.info("Creating new user: {}", user.getUsername());
        return userRepository.save(user);
    }
    
    public User updateUser(User user) {
        logger.info("Updating user: {}", user.getUsername());
        return userRepository.save(user);
    }
    
    public void deleteUser(Long userId) {
        logger.info("Deleting user with ID: {}", userId);
        userRepository.deleteById(userId);
    }
    
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    public long countUsers() {
        return userRepository.count();
    }
    
    public List<User> getUsersWithDevices() {
        logger.info("Fetching users with registered devices");
        return userRepository.findByDeviceIdsIsNotNull();
    }
    
    public void addDeviceToUser(String username, String deviceId) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null) {
            user.getDeviceIds().add(deviceId);
            userRepository.save(user);
            logger.info("Added device {} to user {}", deviceId, username);
        }
    }
    
    public void removeDeviceFromUser(String username, String deviceId) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null && user.getDeviceIds() != null) {
            user.getDeviceIds().remove(deviceId);
            userRepository.save(user);
            logger.info("Removed device {} from user {}", deviceId, username);
        }
    }
}