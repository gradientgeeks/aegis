package com.gradientgeeks.aegis.sfe.repository;

import com.gradientgeeks.aegis.sfe.entity.RegistrationKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationKeyRepository extends JpaRepository<RegistrationKey, Long> {
    
    Optional<RegistrationKey> findByClientId(String clientId);
    
    Optional<RegistrationKey> findByRegistrationKey(String registrationKey);
    
    @Query("SELECT rk FROM RegistrationKey rk WHERE rk.registrationKey = :registrationKey AND rk.isActive = true")
    Optional<RegistrationKey> findActiveByRegistrationKey(@Param("registrationKey") String registrationKey);
    
    boolean existsByClientId(String clientId);
    
    boolean existsByRegistrationKey(String registrationKey);
    
    List<RegistrationKey> findByOrganization(String organization);
    
    Optional<RegistrationKey> findByClientIdAndOrganization(String clientId, String organization);
}