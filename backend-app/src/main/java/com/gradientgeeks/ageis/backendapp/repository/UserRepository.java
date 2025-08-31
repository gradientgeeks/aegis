package com.gradientgeeks.ageis.backendapp.repository;

import com.gradientgeeks.ageis.backendapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    Optional<User> findByUsernameAndIsActiveTrue(String username);
    
    @Query("SELECT u FROM User u WHERE u.deviceIds IS NOT NULL AND SIZE(u.deviceIds) > 0")
    List<User> findByDeviceIdsIsNotNull();
}