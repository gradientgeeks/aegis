package com.gradientgeeks.ageis.backendapp.service;

import com.gradientgeeks.ageis.backendapp.entity.Account;
import com.gradientgeeks.ageis.backendapp.entity.AccountType;
import com.gradientgeeks.ageis.backendapp.entity.User;
import com.gradientgeeks.ageis.backendapp.repository.AccountRepository;
import com.gradientgeeks.ageis.backendapp.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service to initialize sample data for demo purposes.
 */
@Service
@Transactional
@org.springframework.context.annotation.DependsOn("userInitializationService")
public class DataInitializationService {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializationService.class);
    
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    
    public DataInitializationService(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }
    
    @PostConstruct
    public void initializeData() {
        logger.info("Checking and initializing accounts for all users...");
        
        logger.info("Initializing sample data...");
        
        // Get the actual user IDs from the database
        Optional<User> demo1Opt = userRepository.findByUsername("demo1");
        Optional<User> demo2Opt = userRepository.findByUsername("demo2");
        Optional<User> demo3Opt = userRepository.findByUsername("demo3");
        Optional<User> demo4Opt = userRepository.findByUsername("demo4");
        Optional<User> demo5Opt = userRepository.findByUsername("demo5");
        
        User demo1 = demo1Opt.orElse(null);
        User demo2 = demo2Opt.orElse(null);
        User demo3 = demo3Opt.orElse(null);
        User demo4 = demo4Opt.orElse(null);
        User demo5 = demo5Opt.orElse(null);
        
        if (demo1 != null) {
            // Check if accounts already exist for demo1
            List<Account> demo1Accounts = accountRepository.findByUserId(String.valueOf(demo1.getId()));
            if (demo1Accounts.isEmpty()) {
                // Create accounts for demo1 user
                createSampleAccount("123456789012", String.valueOf(demo1.getId()), "Anurag Sharma", 
                                  BigDecimal.valueOf(50000), AccountType.SAVINGS);
                createSampleAccount("123456789013", String.valueOf(demo1.getId()), "Anurag Sharma", 
                                  BigDecimal.valueOf(100000), AccountType.CURRENT);
                logger.info("Created accounts for user demo1 with ID: {}", demo1.getId());
            } else {
                logger.info("Accounts already exist for demo1");
            }
        }
        
        if (demo2 != null) {
            // Check if accounts already exist for demo2
            List<Account> demo2Accounts = accountRepository.findByUserId(String.valueOf(demo2.getId()));
            if (demo2Accounts.isEmpty()) {
                // Create account for demo2 user
                createSampleAccount("987654321098", String.valueOf(demo2.getId()), "Priya Patel", 
                                  BigDecimal.valueOf(75000), AccountType.SAVINGS);
                logger.info("Created account for user demo2 with ID: {}", demo2.getId());
            } else {
                logger.info("Accounts already exist for demo2");
            }
        }
        
        if (demo3 != null) {
            // Check if accounts already exist for demo3
            List<Account> demo3Accounts = accountRepository.findByUserId(String.valueOf(demo3.getId()));
            if (demo3Accounts.isEmpty()) {
                // Create accounts for demo3 user
                createSampleAccount("456789012345", String.valueOf(demo3.getId()), "Rahul Kumar", 
                                  BigDecimal.valueOf(35000), AccountType.SAVINGS);
                createSampleAccount("456789012346", String.valueOf(demo3.getId()), "Rahul Kumar", 
                                  BigDecimal.valueOf(150000), AccountType.FIXED_DEPOSIT);
                logger.info("Created accounts for user demo3 with ID: {}", demo3.getId());
            } else {
                logger.info("Accounts already exist for demo3");
            }
        }
        
        if (demo4 != null) {
            // Check if accounts already exist for demo4
            List<Account> demo4Accounts = accountRepository.findByUserId(String.valueOf(demo4.getId()));
            if (demo4Accounts.isEmpty()) {
                // Create accounts for demo4 user
                createSampleAccount("789012345678", String.valueOf(demo4.getId()), "Neha Singh", 
                                  BigDecimal.valueOf(65000), AccountType.SAVINGS);
                createSampleAccount("789012345679", String.valueOf(demo4.getId()), "Neha Singh", 
                                  BigDecimal.valueOf(25000), AccountType.CURRENT);
                logger.info("Created accounts for user demo4 with ID: {}", demo4.getId());
            } else {
                logger.info("Accounts already exist for demo4");
            }
        }
        
        if (demo5 != null) {
            // Check if accounts already exist for demo5
            List<Account> demo5Accounts = accountRepository.findByUserId(String.valueOf(demo5.getId()));
            if (demo5Accounts.isEmpty()) {
                // Create account for demo5 user
                createSampleAccount("012345678901", String.valueOf(demo5.getId()), "Amit Verma", 
                                  BigDecimal.valueOf(80000), AccountType.SAVINGS);
                logger.info("Created account for user demo5 with ID: {}", demo5.getId());
            } else {
                logger.info("Accounts already exist for demo5");
            }
        }
        
        logger.info("Sample data initialization completed");
    }
    
    private void createSampleAccount(String accountNumber, String userId, String holderName, 
                                   BigDecimal balance, AccountType type) {
        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setUserId(userId);
        account.setAccountHolderName(holderName);
        account.setBalance(balance);
        account.setAccountType(type);
        account.setCurrency("INR");
        
        accountRepository.save(account);
        logger.info("Created sample account: {} for {}", accountNumber, holderName);
    }
}