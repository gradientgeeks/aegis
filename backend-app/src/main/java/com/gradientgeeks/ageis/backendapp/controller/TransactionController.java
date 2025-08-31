package com.gradientgeeks.ageis.backendapp.controller;

import com.gradientgeeks.ageis.backendapp.dto.TransferRequest;
import com.gradientgeeks.ageis.backendapp.dto.TransferResponse;
import com.gradientgeeks.ageis.backendapp.entity.Transaction;
import com.gradientgeeks.ageis.backendapp.service.PayloadEncryptionService;
import com.gradientgeeks.ageis.backendapp.service.PayloadEncryptionService.SecureRequest;
import com.gradientgeeks.ageis.backendapp.service.PayloadEncryptionService.SecureResponse;
import com.gradientgeeks.ageis.backendapp.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * REST controller for transaction operations.
 */
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);
    
    private final TransactionService transactionService;
    
    @Autowired
    private PayloadEncryptionService payloadEncryptionService;
    
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    
    /**
     * Process a money transfer.
     */
    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request,
                                                   HttpServletRequest httpRequest) {
        // Extract security attributes set by the interceptor
        String deviceId = (String) httpRequest.getAttribute("deviceId");
        String signature = (String) httpRequest.getAttribute("signature");
        String nonce = (String) httpRequest.getAttribute("nonce");
        LocalDateTime signatureTimestamp = (LocalDateTime) httpRequest.getAttribute("signatureTimestamp");
        
        logger.info("Processing transfer request from device: {} for amount: {} from {} to {}", 
                   deviceId, request.getAmount(), request.getFromAccount(), request.getToAccount());
        
        TransferResponse response = transactionService.processTransfer(
                request, deviceId, signature, nonce, signatureTimestamp
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get transaction history for an account.
     */
    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<Page<Transaction>> getTransactionHistory(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") 
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
        
        Page<Transaction> transactions = transactionService.getTransactionHistory(accountNumber, pageable);
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * Get transaction details by reference.
     */
    @GetMapping("/reference/{transactionReference}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable String transactionReference) {
        Transaction transaction = transactionService.getTransactionByReference(transactionReference);
        return ResponseEntity.ok(transaction);
    }
    
    /**
     * Process an encrypted money transfer.
     * This endpoint accepts encrypted payloads and returns encrypted responses.
     */
    @PostMapping("/transfer/secure")
    public ResponseEntity<?> secureTransfer(@RequestBody SecureRequest secureRequest,
                                          HttpServletRequest httpRequest) {
        try {
            // Extract security attributes
            String deviceId = (String) httpRequest.getAttribute("deviceId");
            String signature = (String) httpRequest.getAttribute("signature");
            String nonce = (String) httpRequest.getAttribute("nonce");
            LocalDateTime signatureTimestamp = (LocalDateTime) httpRequest.getAttribute("signatureTimestamp");
            
            // Get session ID from request header
            String sessionId = httpRequest.getHeader("X-Session-Id");
            if (sessionId == null) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Session ID header is required"));
            }
            
            // Decrypt the request payload
            TransferRequest transferRequest = payloadEncryptionService.decryptSecureRequest(
                secureRequest, sessionId, TransferRequest.class
            );
            
            logger.info("Processing secure transfer request from device: {} for amount: {} from {} to {}", 
                       deviceId, transferRequest.getAmount(), transferRequest.getFromAccount(), 
                       transferRequest.getToAccount());
            
            // Process the transfer
            TransferResponse transferResponse = transactionService.processTransfer(
                transferRequest, deviceId, signature, nonce, signatureTimestamp
            );
            
            // Encrypt the response
            SecureResponse secureResponse = payloadEncryptionService.createSecureResponse(
                transferResponse, sessionId, HttpStatus.CREATED.value()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(secureResponse);
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Invalid request: " + e.getMessage()));
        } catch (IllegalStateException e) {
            logger.error("Session error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Session error: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Error processing secure transfer", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to process secure transfer"));
        }
    }
    
    // Error response class
    static class ErrorResponse {
        private final String error;
        
        public ErrorResponse(String error) {
            this.error = error;
        }
        
        public String getError() {
            return error;
        }
    }
}