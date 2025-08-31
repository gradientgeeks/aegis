package com.gradientgeeks.ageis.backendapp.service;

import com.gradientgeeks.ageis.backendapp.entity.User;
import com.gradientgeeks.ageis.backendapp.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service to initialize sample users for demo purposes.
 */
@Service
public class UserInitializationService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserInitializationService.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KYCService kycService;
    
    @Autowired
    public UserInitializationService(UserRepository userRepository, PasswordEncoder passwordEncoder, KYCService kycService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.kycService = kycService;
    }
    
    @PostConstruct
    @Transactional
    public void initializeSampleUsers() {
        logger.info("Checking for sample users...");
        
        // Create sample user 1
        if (!userRepository.existsByUsername("demo1")) {
            User user1 = new User();
            user1.setUsername("demo1");
            user1.setPasswordHash(passwordEncoder.encode("password123"));
            user1.setEmail("demo1@ucobank.com");
            user1.setFullName("Anurag Sharma");
            user1.setPhoneNumber("+919876543210");
            user1.setIsActive(true);
            user1 = userRepository.save(user1);
            logger.info("Created sample user: demo1");
            
            // Create KYC data for demo1
            kycService.createOrUpdateKYC(user1, "1234", "ABCDE1234F");
            
            // Create security questions for demo1
            kycService.createOrUpdateSecurityQuestion(user1, "mother_maiden_name", 
                "What is your mother's maiden name?", "sharma");
            kycService.createOrUpdateSecurityQuestion(user1, "first_school", 
                "What was the name of your first school?", "dps");
            
            logger.info("Created KYC and security questions for demo1");
        }
        
        // Create sample user 2
        if (!userRepository.existsByUsername("demo2")) {
            User user2 = new User();
            user2.setUsername("demo2");
            user2.setPasswordHash(passwordEncoder.encode("password123"));
            user2.setEmail("demo2@ucobank.com");
            user2.setFullName("Priya Patel");
            user2.setPhoneNumber("+919876543211");
            user2.setIsActive(true);
            user2 = userRepository.save(user2);
            logger.info("Created sample user: demo2");
            
            // Create KYC data for demo2
            kycService.createOrUpdateKYC(user2, "5678", "FGHIJ5678K");
            
            // Create security questions for demo2
            kycService.createOrUpdateSecurityQuestion(user2, "mother_maiden_name", 
                "What is your mother's maiden name?", "patel");
            kycService.createOrUpdateSecurityQuestion(user2, "first_school", 
                "What was the name of your first school?", "kvs");
            
            logger.info("Created KYC and security questions for demo2");
        }
        
        // Create sample user 3
        if (!userRepository.existsByUsername("demo3")) {
            User user3 = new User();
            user3.setUsername("demo3");
            user3.setPasswordHash(passwordEncoder.encode("password123"));
            user3.setEmail("demo3@ucobank.com");
            user3.setFullName("Rahul Kumar");
            user3.setPhoneNumber("+919876543212");
            user3.setIsActive(true);
            user3 = userRepository.save(user3);
            logger.info("Created sample user: demo3");
            
            // Create KYC data for demo3
            kycService.createOrUpdateKYC(user3, "9012", "LMNOP9012Q");
            
            // Create security questions for demo3
            kycService.createOrUpdateSecurityQuestion(user3, "mother_maiden_name", 
                "What is your mother's maiden name?", "kumar");
            kycService.createOrUpdateSecurityQuestion(user3, "first_school", 
                "What was the name of your first school?", "jnv");
            
            logger.info("Created KYC and security questions for demo3");
        }
        
        // Create sample user 4
        if (!userRepository.existsByUsername("demo4")) {
            User user4 = new User();
            user4.setUsername("demo4");
            user4.setPasswordHash(passwordEncoder.encode("password123"));
            user4.setEmail("demo4@ucobank.com");
            user4.setFullName("Neha Singh");
            user4.setPhoneNumber("+919876543213");
            user4.setIsActive(true);
            user4 = userRepository.save(user4);
            logger.info("Created sample user: demo4");
            
            // Create KYC data for demo4
            kycService.createOrUpdateKYC(user4, "3456", "RSTUV3456W");
            
            // Create security questions for demo4
            kycService.createOrUpdateSecurityQuestion(user4, "mother_maiden_name", 
                "What is your mother's maiden name?", "singh");
            kycService.createOrUpdateSecurityQuestion(user4, "first_school", 
                "What was the name of your first school?", "cms");
            
            logger.info("Created KYC and security questions for demo4");
        }
        
        // Create sample user 5
        if (!userRepository.existsByUsername("demo5")) {
            User user5 = new User();
            user5.setUsername("demo5");
            user5.setPasswordHash(passwordEncoder.encode("password123"));
            user5.setEmail("demo5@ucobank.com");
            user5.setFullName("Amit Verma");
            user5.setPhoneNumber("+919876543214");
            user5.setIsActive(true);
            user5 = userRepository.save(user5);
            logger.info("Created sample user: demo5");
            
            // Create KYC data for demo5
            kycService.createOrUpdateKYC(user5, "7890", "XYZAB7890C");
            
            // Create security questions for demo5
            kycService.createOrUpdateSecurityQuestion(user5, "mother_maiden_name", 
                "What is your mother's maiden name?", "verma");
            kycService.createOrUpdateSecurityQuestion(user5, "first_school", 
                "What was the name of your first school?", "aps");
            
            logger.info("Created KYC and security questions for demo5");
        }
        
        // Create admin user
        if (!userRepository.existsByUsername("admin")) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPasswordHash(passwordEncoder.encode("admin123"));
            adminUser.setEmail("admin@ucobank.com");
            adminUser.setFullName("Bank Administrator");
            adminUser.setPhoneNumber("+919876543220");
            adminUser.setIsActive(true);
            userRepository.save(adminUser);
            logger.info("Created admin user");
        }
        
        logger.info("Sample users initialization completed");
    }
}