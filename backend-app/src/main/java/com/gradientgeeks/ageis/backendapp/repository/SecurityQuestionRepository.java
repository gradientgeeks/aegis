package com.gradientgeeks.ageis.backendapp.repository;

import com.gradientgeeks.ageis.backendapp.entity.SecurityQuestion;
import com.gradientgeeks.ageis.backendapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SecurityQuestionRepository extends JpaRepository<SecurityQuestion, Long> {
    
    List<SecurityQuestion> findByUserAndIsActiveTrue(User user);
    
    Optional<SecurityQuestion> findByUserAndQuestionKeyAndIsActiveTrue(User user, String questionKey);
    
    List<SecurityQuestion> findByUserId(Long userId);
    
    void deleteByUser(User user);
}