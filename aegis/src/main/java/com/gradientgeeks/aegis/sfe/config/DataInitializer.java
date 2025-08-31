package com.gradientgeeks.aegis.sfe.config;

import com.gradientgeeks.aegis.sfe.entity.User;
import com.gradientgeeks.aegis.sfe.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Data initializer to set up demo users on application startup
 */
@Configuration
public class DataInitializer {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Initialize admin user
            if (!userRepository.existsByEmail("admin@aegis.com")) {
                User adminUser = new User();
                adminUser.setEmail("admin@aegis.com");
                adminUser.setPassword(passwordEncoder.encode("admin123"));
                adminUser.setName("Admin User");
                adminUser.setOrganization("Aegis Security");
                adminUser.setRole(User.UserRole.ADMIN);
                adminUser.setContactPerson("System Administrator");
                adminUser.setPhone("+1 (555) 123-4567");
                adminUser.setAddress("123 Security Street, Tech City, TC 12345");
                // Admin users are always approved
                adminUser.setApprovalStatus(User.ApprovalStatus.APPROVED);
                adminUser.setApprovedAt(java.time.LocalDateTime.now());
                adminUser.setApprovedBy("SYSTEM");
                
                userRepository.save(adminUser);
                logger.info("Created demo admin user: admin@aegis.com");
            }
            
            // Initialize demo bank user
            if (!userRepository.existsByEmail("bank@ucobank.com")) {
                User bankUser = new User();
                bankUser.setEmail("bank@ucobank.com");
                bankUser.setPassword(passwordEncoder.encode("bank123"));
                bankUser.setName("UCO Bank Admin");
                bankUser.setOrganization("UCO Bank");
                bankUser.setRole(User.UserRole.USER);
                bankUser.setContactPerson("Bank Administrator");
                bankUser.setPhone("+1 (555) 234-5678");
                bankUser.setAddress("456 Banking Avenue, Finance District, FD 67890");
                // Demo user - already approved
                bankUser.setApprovalStatus(User.ApprovalStatus.APPROVED);
                bankUser.setApprovedAt(java.time.LocalDateTime.now());
                bankUser.setApprovedBy("SYSTEM");
                
                userRepository.save(bankUser);
                logger.info("Created demo bank user: bank@ucobank.com");
            }
            
            // Initialize demo fintech user
            if (!userRepository.existsByEmail("fintech@paytm.com")) {
                User fintechUser = new User();
                fintechUser.setEmail("fintech@paytm.com");
                fintechUser.setPassword(passwordEncoder.encode("fintech123"));
                fintechUser.setName("Paytm Admin");
                fintechUser.setOrganization("Paytm");
                fintechUser.setRole(User.UserRole.USER);
                fintechUser.setContactPerson("Technical Administrator");
                fintechUser.setPhone("+1 (555) 345-6789");
                fintechUser.setAddress("789 Innovation Hub, Tech Park, TP 13579");
                // Demo user - already approved
                fintechUser.setApprovalStatus(User.ApprovalStatus.APPROVED);
                fintechUser.setApprovedAt(java.time.LocalDateTime.now());
                fintechUser.setApprovedBy("SYSTEM");
                
                userRepository.save(fintechUser);
                logger.info("Created demo fintech user: fintech@paytm.com");
            }
            
            logger.info("Database initialization completed");
        };
    }
}