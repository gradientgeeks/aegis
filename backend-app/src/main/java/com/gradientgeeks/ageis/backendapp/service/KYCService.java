package com.gradientgeeks.ageis.backendapp.service;

import com.gradientgeeks.ageis.backendapp.entity.SecurityQuestion;
import com.gradientgeeks.ageis.backendapp.entity.User;
import com.gradientgeeks.ageis.backendapp.entity.UserKYC;
import com.gradientgeeks.ageis.backendapp.repository.SecurityQuestionRepository;
import com.gradientgeeks.ageis.backendapp.repository.UserKYCRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class KYCService {
    
    private static final Logger logger = LoggerFactory.getLogger(KYCService.class);
    
    private final UserKYCRepository userKYCRepository;
    private final SecurityQuestionRepository securityQuestionRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public KYCService(UserKYCRepository userKYCRepository,
                      SecurityQuestionRepository securityQuestionRepository,
                      PasswordEncoder passwordEncoder) {
        this.userKYCRepository = userKYCRepository;
        this.securityQuestionRepository = securityQuestionRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * Verifies user identity by checking KYC data and security answers.
     */
    public boolean verifyUserIdentity(User user, String aadhaarLast4, String panNumber, 
                                    Map<String, String> securityAnswers) {
        try {
            // Verify KYC data
            boolean kycVerified = verifyKYCData(user, aadhaarLast4, panNumber);
            if (!kycVerified) {
                logger.warn("KYC verification failed for user: {}", user.getUsername());
                return false;
            }
            
            // Verify security answers
            boolean securityVerified = verifySecurityAnswers(user, securityAnswers);
            if (!securityVerified) {
                logger.warn("Security answer verification failed for user: {}", user.getUsername());
                return false;
            }
            
            logger.info("Identity verification successful for user: {}", user.getUsername());
            return true;
            
        } catch (Exception e) {
            logger.error("Error during identity verification for user: {}", user.getUsername(), e);
            return false;
        }
    }
    
    /**
     * Verifies KYC data against stored values.
     */
    private boolean verifyKYCData(User user, String providedAadhaarLast4, String providedPanNumber) {
        UserKYC userKYC = userKYCRepository.findByUser(user).orElse(null);
        
        if (userKYC == null) {
            logger.warn("No KYC data found for user: {}", user.getUsername());
            return false;
        }
        
        // Verify Aadhaar last 4 digits
        if (!userKYC.getAadhaarLast4().equals(providedAadhaarLast4)) {
            logger.warn("Aadhaar verification failed for user: {}", user.getUsername());
            return false;
        }
        
        // Verify PAN number (case-insensitive)
        if (!userKYC.getPanNumber().equalsIgnoreCase(providedPanNumber)) {
            logger.warn("PAN verification failed for user: {}", user.getUsername());
            return false;
        }
        
        return true;
    }
    
    /**
     * Verifies security question answers.
     */
    private boolean verifySecurityAnswers(User user, Map<String, String> providedAnswers) {
        if (providedAnswers == null || providedAnswers.isEmpty()) {
            return false;
        }
        
        List<SecurityQuestion> questions = securityQuestionRepository.findByUserAndIsActiveTrue(user);
        
        if (questions.isEmpty()) {
            logger.warn("No security questions found for user: {}", user.getUsername());
            return false;
        }
        
        // Verify each provided answer
        for (Map.Entry<String, String> entry : providedAnswers.entrySet()) {
            String questionKey = entry.getKey();
            String providedAnswer = entry.getValue();
            
            if (providedAnswer == null || providedAnswer.trim().isEmpty()) {
                return false;
            }
            
            // Find the question
            SecurityQuestion question = questions.stream()
                .filter(q -> q.getQuestionKey().equals(questionKey))
                .findFirst()
                .orElse(null);
            
            if (question == null) {
                logger.warn("Security question not found: {} for user: {}", questionKey, user.getUsername());
                return false;
            }
            
            // Verify answer (normalize by converting to lowercase and trimming)
            String normalizedAnswer = providedAnswer.toLowerCase().trim();
            if (!passwordEncoder.matches(normalizedAnswer, question.getAnswerHash())) {
                logger.warn("Security answer verification failed for question: {} for user: {}", 
                    questionKey, user.getUsername());
                return false;
            }
        }
        
        // Ensure minimum number of security questions are answered
        if (providedAnswers.size() < 2) {
            logger.warn("Insufficient security answers provided for user: {}", user.getUsername());
            return false;
        }
        
        return true;
    }
    
    /**
     * Creates or updates KYC data for a user.
     */
    public UserKYC createOrUpdateKYC(User user, String aadhaarLast4, String panNumber) {
        UserKYC userKYC = userKYCRepository.findByUser(user).orElse(new UserKYC());
        
        userKYC.setUser(user);
        userKYC.setAadhaarLast4(aadhaarLast4);
        userKYC.setPanNumber(panNumber.toUpperCase());
        userKYC.setIsVerified(true);
        userKYC.setVerificationDate(LocalDateTime.now());
        
        return userKYCRepository.save(userKYC);
    }
    
    /**
     * Creates or updates a security question for a user.
     */
    public SecurityQuestion createOrUpdateSecurityQuestion(User user, String questionKey, 
                                                         String questionText, String answer) {
        SecurityQuestion question = securityQuestionRepository
            .findByUserAndQuestionKeyAndIsActiveTrue(user, questionKey)
            .orElse(new SecurityQuestion());
        
        question.setUser(user);
        question.setQuestionKey(questionKey);
        question.setQuestionText(questionText);
        
        // Normalize and hash the answer
        String normalizedAnswer = answer.toLowerCase().trim();
        question.setAnswerHash(passwordEncoder.encode(normalizedAnswer));
        question.setIsActive(true);
        
        return securityQuestionRepository.save(question);
    }
    
    /**
     * Gets KYC data for a user.
     */
    public UserKYC getUserKYC(User user) {
        return userKYCRepository.findByUser(user).orElse(null);
    }
    
    /**
     * Gets active security questions for a user.
     */
    public List<SecurityQuestion> getUserSecurityQuestions(User user) {
        return securityQuestionRepository.findByUserAndIsActiveTrue(user);
    }
}