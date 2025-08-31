package com.gradientgeeks.aegis.sfe.service;

import com.gradientgeeks.aegis.sfe.dto.RegistrationKeyRequest;
import com.gradientgeeks.aegis.sfe.dto.RegistrationKeyResponse;
import com.gradientgeeks.aegis.sfe.entity.RegistrationKey;
import com.gradientgeeks.aegis.sfe.repository.RegistrationKeyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class RegistrationKeyService {
    
    private static final Logger logger = LoggerFactory.getLogger(RegistrationKeyService.class);
    
    private final RegistrationKeyRepository registrationKeyRepository;
    private final CryptographyService cryptographyService;
    
    @Autowired
    public RegistrationKeyService(
            RegistrationKeyRepository registrationKeyRepository,
            CryptographyService cryptographyService) {
        this.registrationKeyRepository = registrationKeyRepository;
        this.cryptographyService = cryptographyService;
    }
    
    public RegistrationKeyResponse generateRegistrationKey(RegistrationKeyRequest request) {
        logger.info("Generating registration key for clientId: {}", request.getClientId());
        
        try {
            if (registrationKeyRepository.existsByClientId(request.getClientId())) {
                logger.warn("Registration key already exists for clientId: {}", request.getClientId());
                return new RegistrationKeyResponse("error", "Registration key already exists for this client");
            }
            
            String registrationKey = cryptographyService.generateRegistrationKey();
            
            RegistrationKey entity = new RegistrationKey(
                request.getClientId(),
                registrationKey,
                request.getDescription()
            );
            entity.setOrganization(request.getOrganization());
            entity.setExpiresAt(request.getExpiresAt());
            
            RegistrationKey savedEntity = registrationKeyRepository.save(entity);
            
            logger.info("Registration key generated successfully for clientId: {}", request.getClientId());
            
            return new RegistrationKeyResponse(
                savedEntity.getId(),
                savedEntity.getClientId(),
                savedEntity.getRegistrationKey(),
                savedEntity.getDescription(),
                savedEntity.getIsActive(),
                savedEntity.getExpiresAt(),
                savedEntity.getCreatedAt()
            );
            
        } catch (Exception e) {
            logger.error("Error generating registration key for clientId: {}", request.getClientId(), e);
            return new RegistrationKeyResponse("error", "Internal server error during key generation");
        }
    }
    
    @Transactional(readOnly = true)
    public List<RegistrationKeyResponse> getAllRegistrationKeys() {
        logger.info("Retrieving all registration keys");
        
        return registrationKeyRepository.findAll()
            .stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Optional<RegistrationKeyResponse> getRegistrationKeyByClientId(String clientId) {
        logger.info("Retrieving registration key for clientId: {}", clientId);
        
        return registrationKeyRepository.findByClientId(clientId)
            .map(this::convertToResponse);
    }
    
    public RegistrationKeyResponse revokeRegistrationKey(String clientId) {
        logger.info("Revoking registration key for clientId: {}", clientId);
        
        try {
            Optional<RegistrationKey> optionalKey = registrationKeyRepository.findByClientId(clientId);
            
            if (optionalKey.isEmpty()) {
                logger.warn("Registration key not found for clientId: {}", clientId);
                return new RegistrationKeyResponse("error", "Registration key not found");
            }
            
            RegistrationKey registrationKey = optionalKey.get();
            registrationKey.setIsActive(false);
            
            RegistrationKey savedEntity = registrationKeyRepository.save(registrationKey);
            
            logger.info("Registration key revoked successfully for clientId: {}", clientId);
            
            return convertToResponse(savedEntity);
            
        } catch (Exception e) {
            logger.error("Error revoking registration key for clientId: {}", clientId, e);
            return new RegistrationKeyResponse("error", "Internal server error during key revocation");
        }
    }
    
    public RegistrationKeyResponse regenerateRegistrationKey(String clientId) {
        logger.info("Regenerating registration key for clientId: {}", clientId);
        
        try {
            Optional<RegistrationKey> optionalKey = registrationKeyRepository.findByClientId(clientId);
            
            if (optionalKey.isEmpty()) {
                logger.warn("Registration key not found for clientId: {}", clientId);
                return new RegistrationKeyResponse("error", "Registration key not found");
            }
            
            RegistrationKey registrationKey = optionalKey.get();
            String newKey = cryptographyService.generateRegistrationKey();
            registrationKey.setRegistrationKey(newKey);
            registrationKey.setIsActive(true);
            
            RegistrationKey savedEntity = registrationKeyRepository.save(registrationKey);
            
            logger.info("Registration key regenerated successfully for clientId: {}", clientId);
            
            return convertToResponse(savedEntity);
            
        } catch (Exception e) {
            logger.error("Error regenerating registration key for clientId: {}", clientId, e);
            return new RegistrationKeyResponse("error", "Internal server error during key regeneration");
        }
    }
    
    private RegistrationKeyResponse convertToResponse(RegistrationKey entity) {
        return new RegistrationKeyResponse(
            entity.getId(),
            entity.getClientId(),
            entity.getRegistrationKey(),
            entity.getDescription(),
            entity.getIsActive(),
            entity.getExpiresAt(),
            entity.getCreatedAt()
        );
    }
    
    public List<RegistrationKeyResponse> getRegistrationKeysByOrganization(String organization) {
        logger.info("Retrieving registration keys for organization: {}", organization);
        
        List<RegistrationKey> keys = registrationKeyRepository.findByOrganization(organization);
        
        return keys.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    public Optional<RegistrationKeyResponse> getRegistrationKeyByClientIdAndOrganization(
            String clientId, String organization) {
        logger.info("Retrieving registration key for clientId: {} and organization: {}", 
            clientId, organization);
        
        return registrationKeyRepository.findByClientIdAndOrganization(clientId, organization)
            .map(this::convertToResponse);
    }
}