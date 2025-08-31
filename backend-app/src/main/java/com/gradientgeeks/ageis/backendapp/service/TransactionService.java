package com.gradientgeeks.ageis.backendapp.service;

import com.gradientgeeks.ageis.backendapp.dto.TransferRequest;
import com.gradientgeeks.ageis.backendapp.dto.TransferResponse;
import com.gradientgeeks.ageis.backendapp.entity.Account;
import com.gradientgeeks.ageis.backendapp.entity.Transaction;
import com.gradientgeeks.ageis.backendapp.entity.TransactionStatus;
import com.gradientgeeks.ageis.backendapp.entity.TransactionType;
import com.gradientgeeks.ageis.backendapp.exception.InvalidTransactionException;
import com.gradientgeeks.ageis.backendapp.repository.AccountRepository;
import com.gradientgeeks.ageis.backendapp.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for processing financial transactions.
 */
@Service
@Transactional
public class TransactionService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final AegisIntegrationService aegisIntegrationService;
    
    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository,
                            AccountService accountService, AegisIntegrationService aegisIntegrationService) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.accountService = accountService;
        this.aegisIntegrationService = aegisIntegrationService;
    }
    
    /**
     * Processes a money transfer between accounts.
     * Uses pessimistic locking to prevent race conditions.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TransferResponse processTransfer(TransferRequest request, String deviceId, 
                                          String signature, String nonce, 
                                          LocalDateTime signatureTimestamp) {
        logger.info("Processing transfer from {} to {} for amount {}", 
                   request.getFromAccount(), request.getToAccount(), request.getAmount());
        
        // Validate the transfer request
        validateTransferRequest(request);
        
        // Create transaction record
        Transaction transaction = createTransaction(request, deviceId, signature, nonce, signatureTimestamp);
        
        try {
            // Get accounts with pessimistic lock to prevent concurrent modifications
            Account fromAccount = accountService.getAccountWithLock(request.getFromAccount());
            Account toAccount = accountService.getAccountWithLock(request.getToAccount());
            
            // Validate accounts
            validateAccounts(fromAccount, toAccount, request);
            
            // Update transaction with account IDs
            transaction.setFromAccountId(fromAccount.getId());
            transaction.setToAccountId(toAccount.getId());
            transaction.setStatus(TransactionStatus.PROCESSING);
            transactionRepository.save(transaction);
            
            // Perform the transfer
            accountService.updateBalance(fromAccount, request.getAmount().negate());
            accountService.updateBalance(toAccount, request.getAmount());
            
            // Mark transaction as completed
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setCompletedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
            
            logger.info("Transfer completed successfully: {}", transaction.getTransactionReference());
            
            // Post-transaction fraud analysis (asynchronous)
            performPostTransactionFraudAnalysis(transaction, deviceId, request);
            
            return new TransferResponse(
                    transaction.getTransactionReference(),
                    transaction.getStatus().name(),
                    transaction.getAmount(),
                    transaction.getCurrency(),
                    request.getFromAccount(),
                    request.getToAccount(),
                    transaction.getCompletedAt(),
                    "Transfer completed successfully"
            );
            
        } catch (Exception e) {
            // Mark transaction as failed
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setRemarks("Error: " + e.getMessage());
            transaction.setCompletedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
            
            logger.error("Transfer failed: {}", e.getMessage(), e);
            throw new InvalidTransactionException("Transfer failed: " + e.getMessage());
        }
    }
    
    /**
     * Retrieves transaction history for an account.
     */
    public Page<Transaction> getTransactionHistory(String accountNumber, Pageable pageable) {
        Account account = accountService.getAccountWithLock(accountNumber);
        return transactionRepository.findByAccountId(account.getId(), pageable);
    }
    
    /**
     * Retrieves a transaction by reference number.
     */
    public Transaction getTransactionByReference(String transactionReference) {
        return transactionRepository.findByTransactionReference(transactionReference)
                .orElseThrow(() -> new InvalidTransactionException("Transaction not found: " + transactionReference));
    }
    
    /**
     * Performs post-transaction fraud analysis.
     * This runs after the transaction is completed to analyze patterns and report suspicious activity.
     * 
     * @param transaction The completed transaction
     * @param deviceId The device that initiated the transaction
     * @param request The original transfer request
     */
    private void performPostTransactionFraudAnalysis(Transaction transaction, String deviceId, TransferRequest request) {
        try {
            logger.debug("Performing fraud analysis for transaction: {}", transaction.getTransactionReference());
            
            // Calculate risk score based on various factors
            int riskScore = calculateTransactionRiskScore(transaction, request);
            
            logger.debug("Risk score for transaction {}: {}", transaction.getTransactionReference(), riskScore);
            
            // Report to Aegis if risk score is above threshold
            if (riskScore >= 70) {
                String description = generateFraudDescription(transaction, request, riskScore);
                
                logger.warn("High-risk transaction detected - Risk Score: {}, Transaction: {}, Device: {}", 
                    riskScore, transaction.getTransactionReference(), deviceId);
                
                // Report fraud asynchronously to avoid impacting transaction performance
                boolean reported = aegisIntegrationService.reportFraudWithRiskScore(
                    deviceId, 
                    transaction.getTransactionReference(), 
                    riskScore, 
                    description
                );
                
                if (reported) {
                    logger.info("Fraud report submitted for transaction: {}", transaction.getTransactionReference());
                } else {
                    logger.error("Failed to submit fraud report for transaction: {}", transaction.getTransactionReference());
                }
            }
            
        } catch (Exception e) {
            // Fraud analysis should not impact the transaction itself
            logger.error("Error during post-transaction fraud analysis for transaction: {}", 
                transaction.getTransactionReference(), e);
        }
    }
    
    /**
     * Calculates a risk score for the transaction based on various factors.
     * This is a simplified implementation - real banks would have much more sophisticated ML models.
     * 
     * @param transaction The transaction to analyze
     * @param request The original request
     * @return Risk score from 0-100 (higher is more risky)
     */
    private int calculateTransactionRiskScore(Transaction transaction, TransferRequest request) {
        int riskScore = 0;
        
        // High amount transactions are riskier
        if (transaction.getAmount().compareTo(new BigDecimal("50000")) > 0) {
            riskScore += 30;
        } else if (transaction.getAmount().compareTo(new BigDecimal("10000")) > 0) {
            riskScore += 15;
        }
        
        // Weekend/late night transactions are slightly riskier
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        if (hour < 6 || hour > 22) {
            riskScore += 10;
        }
        
        // Check if it's to a new beneficiary (simplified - in reality you'd check transaction history)
        if (isNewBeneficiary(request.getFromAccount(), request.getToAccount())) {
            riskScore += 25;
        }
        
        // International transfers (simplified check based on account format)
        if (request.getToAccount().length() > 15) { // Assume longer account numbers are international
            riskScore += 20;
        }
        
        // Round number amounts are sometimes suspicious
        if (transaction.getAmount().remainder(new BigDecimal("1000")).compareTo(BigDecimal.ZERO) == 0) {
            riskScore += 5;
        }
        
        return Math.min(riskScore, 100); // Cap at 100
    }
    
    /**
     * Generates a description for the fraud report.
     */
    private String generateFraudDescription(Transaction transaction, TransferRequest request, int riskScore) {
        StringBuilder description = new StringBuilder();
        description.append("High-risk transaction detected by bank ML model. ");
        description.append("Amount: ").append(transaction.getAmount()).append(" ").append(transaction.getCurrency()).append(". ");
        
        if (transaction.getAmount().compareTo(new BigDecimal("50000")) > 0) {
            description.append("Large amount transfer. ");
        }
        
        if (isNewBeneficiary(request.getFromAccount(), request.getToAccount())) {
            description.append("Transfer to new/infrequent beneficiary. ");
        }
        
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        if (hour < 6 || hour > 22) {
            description.append("Transaction outside normal hours. ");
        }
        
        description.append("Requires review.");
        return description.toString();
    }
    
    /**
     * Simplified check for new beneficiary.
     * In a real implementation, this would check the customer's transaction history.
     */
    private boolean isNewBeneficiary(String fromAccount, String toAccount) {
        // Simplified implementation - assume some accounts are "new" based on patterns
        // In reality, you'd check the customer's transaction history
        return toAccount.endsWith("999") || toAccount.endsWith("888") || toAccount.length() > 15;
    }
    
    /**
     * Creates a new transaction record.
     */
    private Transaction createTransaction(TransferRequest request, String deviceId, 
                                        String signature, String nonce, 
                                        LocalDateTime signatureTimestamp) {
        Transaction transaction = new Transaction();
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setDescription(request.getDescription());
        transaction.setRemarks(request.getRemarks());
        transaction.setDeviceId(deviceId);
        transaction.setSignature(signature);
        transaction.setNonce(nonce);
        transaction.setSignatureTimestamp(signatureTimestamp);
        transaction.setStatus(TransactionStatus.PENDING);
        
        // Don't save yet - we need to set the account IDs first
        return transaction;
    }
    
    /**
     * Validates a transfer request.
     */
    private void validateTransferRequest(TransferRequest request) {
        if (request.getFromAccount().equals(request.getToAccount())) {
            throw new InvalidTransactionException("Cannot transfer to the same account");
        }
        
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Transfer amount must be positive");
        }
        
        if (request.getAmount().compareTo(new BigDecimal("1000000")) > 0) {
            throw new InvalidTransactionException("Transfer amount exceeds maximum limit");
        }
    }
    
    /**
     * Validates accounts for transfer.
     */
    private void validateAccounts(Account fromAccount, Account toAccount, TransferRequest request) {
        if (!fromAccount.getCurrency().equals(request.getCurrency()) || 
            !toAccount.getCurrency().equals(request.getCurrency())) {
            throw new InvalidTransactionException("Currency mismatch");
        }
        
        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InvalidTransactionException("Insufficient balance");
        }
    }
    
    /**
     * Get all transactions
     */
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }
    
    /**
     * Get recent transactions
     */
    public List<Transaction> getRecentTransactions(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Transaction> page = transactionRepository.findAll(pageable);
        return page.getContent();
    }
    
    /**
     * Get transactions by date
     */
    public List<Transaction> getTransactionsByDate(LocalDateTime date) {
        LocalDateTime startOfDay = date.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = date.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        return transactionRepository.findByCreatedAtBetween(startOfDay, endOfDay);
    }
    
    /**
     * Get user transaction statistics for policy enforcement.
     * Calculates daily, weekly and monthly transaction counts and amounts.
     * 
     * @param userId The user ID to get statistics for
     * @return Map containing transaction statistics
     */
    public Map<String, Object> getUserTransactionStatistics(Long userId) {
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            // Get all accounts for the user
            List<Account> userAccounts = accountRepository.findByUserId(userId.toString());
            
            if (userAccounts.isEmpty()) {
                logger.debug("No accounts found for user: {}", userId);
                return getEmptyStatistics();
            }
            
            // Calculate time boundaries
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfDay = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime startOfWeek = now.minusDays(7);
            LocalDateTime startOfMonth = now.minusDays(30);
            
            // Initialize counters
            int dailyCount = 0;
            BigDecimal dailyAmount = BigDecimal.ZERO;
            int weeklyCount = 0;
            BigDecimal weeklyAmount = BigDecimal.ZERO;
            int monthlyCount = 0;
            BigDecimal monthlyAmount = BigDecimal.ZERO;
            
            // Get all account IDs for the user
            List<UUID> accountIds = userAccounts.stream()
                    .map(Account::getId)
                    .toList();
            
            // Get all transactions for user's accounts in the last 30 days
            List<Transaction> transactions = transactionRepository.findByAccountIdsAndDateRange(accountIds, startOfMonth, now);
            
            // Process transactions
            for (Transaction transaction : transactions) {
                // Only count outgoing transactions (from user's accounts) and completed ones
                boolean isOutgoing = accountIds.contains(transaction.getFromAccountId());
                if (isOutgoing && transaction.getStatus() == TransactionStatus.COMPLETED) {
                    
                    BigDecimal amount = transaction.getAmount();
                    
                    // Monthly statistics (all transactions in the period)
                    monthlyCount++;
                    monthlyAmount = monthlyAmount.add(amount);
                    
                    // Weekly statistics
                    if (transaction.getCreatedAt().isAfter(startOfWeek)) {
                        weeklyCount++;
                        weeklyAmount = weeklyAmount.add(amount);
                    }
                    
                    // Daily statistics
                    if (transaction.getCreatedAt().isAfter(startOfDay)) {
                        dailyCount++;
                        dailyAmount = dailyAmount.add(amount);
                    }
                }
            }
            
            // Populate statistics map
            statistics.put("dailyCount", dailyCount);
            statistics.put("dailyAmount", dailyAmount.doubleValue());
            statistics.put("weeklyCount", weeklyCount);
            statistics.put("weeklyAmount", weeklyAmount.doubleValue());
            statistics.put("monthlyCount", monthlyCount);
            statistics.put("monthlyAmount", monthlyAmount.doubleValue());
            
            logger.debug("User {} transaction statistics - Daily: {} transactions, {} amount", 
                        userId, dailyCount, dailyAmount);
            
            return statistics;
            
        } catch (Exception e) {
            logger.error("Error calculating transaction statistics for user: {}", userId, e);
            return getEmptyStatistics();
        }
    }
    
    /**
     * Returns empty statistics map with zero values.
     */
    private Map<String, Object> getEmptyStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("dailyCount", 0);
        statistics.put("dailyAmount", 0.0);
        statistics.put("weeklyCount", 0);
        statistics.put("weeklyAmount", 0.0);
        statistics.put("monthlyCount", 0);
        statistics.put("monthlyAmount", 0.0);
        return statistics;
    }
}