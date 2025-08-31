package com.gradientgeeks.ageis.backendapp.repository;

import com.gradientgeeks.ageis.backendapp.entity.UserKYC;
import com.gradientgeeks.ageis.backendapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserKYCRepository extends JpaRepository<UserKYC, Long> {
    
    Optional<UserKYC> findByUser(User user);
    
    Optional<UserKYC> findByUserId(Long userId);
    
    boolean existsByUser(User user);
    
    boolean existsByPanNumber(String panNumber);
}