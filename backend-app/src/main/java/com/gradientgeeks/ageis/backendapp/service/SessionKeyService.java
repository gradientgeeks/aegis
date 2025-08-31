package com.gradientgeeks.ageis.backendapp.service;

import com.gradientgeeks.ageis.backendapp.dto.KeyExchangeRequest;
import com.gradientgeeks.ageis.backendapp.dto.KeyExchangeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;

/**
 * Service for managing ECDH key exchange and session keys.
 */
@Service
public class SessionKeyService {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionKeyService.class);
    private static final String EC_ALGORITHM = "EC";
    private static final String EC_CURVE = "secp256r1";
    private static final String KEY_AGREEMENT_ALGORITHM = "ECDH";
    private static final String SESSION_KEY_ALGORITHM = "AES";
    private static final long SESSION_DURATION_HOURS = 1;
    private static final String REDIS_KEY_PREFIX = "session_key:";
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    /**
     * Initiates key exchange by generating server's ECDH key pair and deriving session key.
     */
    public KeyExchangeResponse initiateKeyExchange(KeyExchangeRequest request) throws Exception {
        logger.info("Initiating key exchange for session: {}", request.getSessionId());
        
        // Generate server's ECDH key pair
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(EC_ALGORITHM);
        keyPairGenerator.initialize(new ECGenParameterSpec(EC_CURVE));
        KeyPair serverKeyPair = keyPairGenerator.generateKeyPair();
        
        // Decode client's public key
        byte[] clientPublicKeyBytes = Base64.getDecoder().decode(request.getClientPublicKey());
        KeyFactory keyFactory = KeyFactory.getInstance(EC_ALGORITHM);
        PublicKey clientPublicKey = keyFactory.generatePublic(new X509EncodedKeySpec(clientPublicKeyBytes));
        
        // Perform ECDH key agreement
        KeyAgreement keyAgreement = KeyAgreement.getInstance(KEY_AGREEMENT_ALGORITHM);
        keyAgreement.init(serverKeyPair.getPrivate());
        keyAgreement.doPhase(clientPublicKey, true);
        
        // Generate shared secret
        byte[] sharedSecret = keyAgreement.generateSecret();
        
        // Derive AES-256 session key from shared secret using SHA-256
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] sessionKeyBytes = sha256.digest(sharedSecret);
        SecretKey sessionKey = new SecretKeySpec(sessionKeyBytes, SESSION_KEY_ALGORITHM);
        
        // Store session key in Redis with expiration
        String sessionKeyBase64 = Base64.getEncoder().encodeToString(sessionKey.getEncoded());
        String redisKey = REDIS_KEY_PREFIX + request.getSessionId();
        redisTemplate.opsForValue().set(redisKey, sessionKeyBase64, Duration.ofHours(SESSION_DURATION_HOURS));
        
        // Prepare response
        String serverPublicKeyBase64 = Base64.getEncoder().encodeToString(serverKeyPair.getPublic().getEncoded());
        long expiresAt = System.currentTimeMillis() + (SESSION_DURATION_HOURS * 3600 * 1000);
        
        logger.info("Key exchange completed for session: {}", request.getSessionId());
        
        return new KeyExchangeResponse(
            serverPublicKeyBase64,
            request.getSessionId(),
            "ECDH-P256",
            expiresAt
        );
    }
    
    /**
     * Retrieves session key from Redis.
     */
    public SecretKey getSessionKey(String sessionId) {
        String redisKey = REDIS_KEY_PREFIX + sessionId;
        String sessionKeyBase64 = redisTemplate.opsForValue().get(redisKey);
        
        if (sessionKeyBase64 == null) {
            logger.warn("Session key not found for session: {}", sessionId);
            return null;
        }
        
        try {
            byte[] keyBytes = Base64.getDecoder().decode(sessionKeyBase64);
            return new SecretKeySpec(keyBytes, SESSION_KEY_ALGORITHM);
        } catch (Exception e) {
            logger.error("Failed to decode session key", e);
            return null;
        }
    }
    
    /**
     * Removes session key from Redis.
     */
    public void clearSession(String sessionId) {
        String redisKey = REDIS_KEY_PREFIX + sessionId;
        redisTemplate.delete(redisKey);
        logger.info("Cleared session: {}", sessionId);
    }
    
    /**
     * Checks if a session exists and is valid.
     */
    public boolean isSessionValid(String sessionId) {
        String redisKey = REDIS_KEY_PREFIX + sessionId;
        return redisTemplate.hasKey(redisKey);
    }
}