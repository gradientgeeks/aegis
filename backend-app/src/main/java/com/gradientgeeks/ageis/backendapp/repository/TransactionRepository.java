package com.gradientgeeks.ageis.backendapp.repository;

import com.gradientgeeks.ageis.backendapp.entity.Transaction;
import com.gradientgeeks.ageis.backendapp.entity.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Transaction entity operations.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    
    Optional<Transaction> findByTransactionReference(String transactionReference);
    
    Page<Transaction> findByFromAccountId(UUID fromAccountId, Pageable pageable);
    
    Page<Transaction> findByToAccountId(UUID toAccountId, Pageable pageable);
    
    @Query("SELECT t FROM Transaction t WHERE (t.fromAccountId = :accountId OR t.toAccountId = :accountId) ORDER BY t.createdAt DESC")
    Page<Transaction> findByAccountId(@Param("accountId") UUID accountId, Pageable pageable);
    
    List<Transaction> findByStatus(TransactionStatus status);
    
    @Query("SELECT t FROM Transaction t WHERE t.createdAt BETWEEN :startDate AND :endDate")
    List<Transaction> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT t FROM Transaction t WHERE t.deviceId = :deviceId ORDER BY t.createdAt DESC")
    List<Transaction> findByDeviceId(@Param("deviceId") String deviceId);
    
    List<Transaction> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT t FROM Transaction t WHERE (t.fromAccountId IN :accountIds OR t.toAccountId IN :accountIds) AND t.createdAt BETWEEN :startDate AND :endDate")
    List<Transaction> findByAccountIdsAndDateRange(@Param("accountIds") List<UUID> accountIds, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}