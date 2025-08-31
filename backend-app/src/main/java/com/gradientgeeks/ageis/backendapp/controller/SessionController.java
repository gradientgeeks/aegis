package com.gradientgeeks.ageis.backendapp.controller;

import com.gradientgeeks.ageis.backendapp.dto.KeyExchangeRequest;
import com.gradientgeeks.ageis.backendapp.dto.KeyExchangeResponse;
import com.gradientgeeks.ageis.backendapp.service.SessionKeyService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for session key management and ECDH key exchange.
 */
@RestController
@RequestMapping("/api/v1/session")
public class SessionController {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionController.class);
    
    @Autowired
    private SessionKeyService sessionKeyService;
    
    /**
     * Initiates ECDH key exchange to establish a session key.
     * This endpoint requires HMAC signature validation.
     */
    @PostMapping("/key-exchange")
    public ResponseEntity<?> initiateKeyExchange(@Valid @RequestBody KeyExchangeRequest request) {
        logger.info("Received key exchange request for session: {}", request.getSessionId());
        
        try {
            // Validate algorithm
            if (!"ECDH-P256".equals(request.getAlgorithm())) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Unsupported algorithm: " + request.getAlgorithm()));
            }
            
            // Perform key exchange
            KeyExchangeResponse response = sessionKeyService.initiateKeyExchange(request);
            
            logger.info("Key exchange successful for session: {}", request.getSessionId());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Key exchange failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Key exchange failed: " + e.getMessage()));
        }
    }
    
    /**
     * Terminates a session and clears the session key.
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<?> terminateSession(@PathVariable String sessionId) {
        logger.info("Terminating session: {}", sessionId);
        
        try {
            sessionKeyService.clearSession(sessionId);
            return ResponseEntity.ok()
                .body(new MessageResponse("Session terminated successfully"));
        } catch (Exception e) {
            logger.error("Failed to terminate session", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to terminate session"));
        }
    }
    
    /**
     * Checks if a session is valid.
     */
    @GetMapping("/{sessionId}/status")
    public ResponseEntity<?> checkSessionStatus(@PathVariable String sessionId) {
        boolean isValid = sessionKeyService.isSessionValid(sessionId);
        
        return ResponseEntity.ok()
            .body(new SessionStatusResponse(sessionId, isValid));
    }
    
    // Response DTOs
    static class ErrorResponse {
        private final String error;
        
        public ErrorResponse(String error) {
            this.error = error;
        }
        
        public String getError() {
            return error;
        }
    }
    
    static class MessageResponse {
        private final String message;
        
        public MessageResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    static class SessionStatusResponse {
        private final String sessionId;
        private final boolean valid;
        
        public SessionStatusResponse(String sessionId, boolean valid) {
            this.sessionId = sessionId;
            this.valid = valid;
        }
        
        public String getSessionId() {
            return sessionId;
        }
        
        public boolean isValid() {
            return valid;
        }
    }
}