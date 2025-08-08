package com.log430.tp7.application.service;

import com.log430.tp7.domain.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for transaction compensation logic.
 * Handles rollback operations for failed saga steps.
 */
@Service
@Transactional
public class TransactionCompensationService {
    
    private static final Logger log = LoggerFactory.getLogger(TransactionCompensationService.class);
    
    private final TransactionCommandService transactionCommandService;
    private final TransactionEventService transactionEventService;
    
    public TransactionCompensationService(TransactionCommandService transactionCommandService,
                                        TransactionEventService transactionEventService) {
        this.transactionCommandService = transactionCommandService;
        this.transactionEventService = transactionEventService;
    }
    
    /**
     * Compensate a transaction due to payment failure.
     */
    public void compensateForPaymentFailure(Long transactionId, String failureReason, 
                                          String correlationId, String causationId) {
        log.info("Compensating transaction {} for payment failure: {}", transactionId, failureReason);
        
        try {
            var transactionOpt = transactionCommandService.getTransactionById(transactionId);
            
            if (transactionOpt.isEmpty()) {
                log.error("Cannot compensate - transaction not found: {}", transactionId);
                return;
            }
            
            Transaction transaction = transactionOpt.get();
            
            // Check if compensation is needed
            if (transaction.isCancelled()) {
                log.info("Transaction {} is already cancelled, no compensation needed", transactionId);
                return;
            }
            
            if (transaction.isCompleted()) {
                log.warn("Transaction {} is completed, cannot compensate for payment failure", transactionId);
                return;
            }
            
            // Cancel the transaction
            String compensationReason = "Compensation: " + failureReason;
            transactionCommandService.cancelTransaction(transactionId, compensationReason, 
                                                      correlationId, causationId);
            
            log.info("Successfully compensated transaction {} for payment failure", transactionId);
            
        } catch (Exception e) {
            log.error("Failed to compensate transaction {} for payment failure: {}", 
                     transactionId, e.getMessage(), e);
            throw new RuntimeException("Transaction compensation failed", e);
        }
    }
    
    /**
     * Compensate a completed transaction due to inventory unavailability.
     */
    public void compensateForInventoryUnavailability(Long transactionId, String inventoryReason,
                                                   String correlationId, String causationId) {
        log.info("Compensating transaction {} for inventory unavailability: {}", 
                transactionId, inventoryReason);
        
        try {
            var transactionOpt = transactionCommandService.getTransactionById(transactionId);
            
            if (transactionOpt.isEmpty()) {
                log.error("Cannot compensate - transaction not found: {}", transactionId);
                return;
            }
            
            Transaction transaction = transactionOpt.get();
            
            // For inventory issues, we might need to create a return transaction
            // if the original transaction was already completed
            if (transaction.isCompleted()) {
                createCompensationReturnTransaction(transaction, inventoryReason, 
                                                  correlationId, causationId);
            } else if (!transaction.isCancelled()) {
                // Cancel if still in progress
                String compensationReason = "Compensation: " + inventoryReason;
                transactionCommandService.cancelTransaction(transactionId, compensationReason,
                                                          correlationId, causationId);
            }
            
            log.info("Successfully compensated transaction {} for inventory unavailability", transactionId);
            
        } catch (Exception e) {
            log.error("Failed to compensate transaction {} for inventory unavailability: {}", 
                     transactionId, e.getMessage(), e);
            throw new RuntimeException("Transaction compensation failed", e);
        }
    }
    
    /**
     * Compensate for general saga failure.
     */
    public void compensateForSagaFailure(Long transactionId, String sagaFailureReason,
                                       String correlationId, String causationId) {
        log.info("Compensating transaction {} for saga failure: {}", transactionId, sagaFailureReason);
        
        try {
            var transactionOpt = transactionCommandService.getTransactionById(transactionId);
            
            if (transactionOpt.isEmpty()) {
                log.error("Cannot compensate - transaction not found: {}", transactionId);
                return;
            }
            
            Transaction transaction = transactionOpt.get();
            
            if (transaction.isCompleted()) {
                // Create a return transaction for completed transactions
                createCompensationReturnTransaction(transaction, sagaFailureReason,
                                                  correlationId, causationId);
            } else if (!transaction.isCancelled()) {
                // Cancel in-progress transactions
                String compensationReason = "Saga compensation: " + sagaFailureReason;
                transactionCommandService.cancelTransaction(transactionId, compensationReason,
                                                          correlationId, causationId);
            }
            
            log.info("Successfully compensated transaction {} for saga failure", transactionId);
            
        } catch (Exception e) {
            log.error("Failed to compensate transaction {} for saga failure: {}", 
                     transactionId, e.getMessage(), e);
            throw new RuntimeException("Saga compensation failed", e);
        }
    }
    
    /**
     * Create a return transaction as compensation for a completed transaction.
     */
    private void createCompensationReturnTransaction(Transaction originalTransaction, 
                                                   String compensationReason,
                                                   String correlationId, String causationId) {
        log.info("Creating compensation return transaction for completed transaction: {}", 
                originalTransaction.getId());
        
        try {
            // Create return transaction
            Transaction returnTransaction = transactionCommandService.createReturnTransaction(
                originalTransaction.getPersonnelId(),
                originalTransaction.getStoreId(),
                originalTransaction.getId(),
                "Compensation: " + compensationReason
            );
            
            // Copy items from original transaction
            if (originalTransaction.getItems() != null) {
                for (var item : originalTransaction.getItems()) {
                    transactionCommandService.addItemToTransaction(
                        returnTransaction.getId(),
                        item.getInventoryItemId(),
                        item.getQuantite(),
                        item.getPrixUnitaire()
                    );
                }
            }
            
            // Complete the return transaction immediately
            transactionCommandService.completeTransaction(returnTransaction.getId(),
                                                        correlationId, causationId);
            
            log.info("Successfully created compensation return transaction {} for original transaction {}", 
                    returnTransaction.getId(), originalTransaction.getId());
            
        } catch (Exception e) {
            log.error("Failed to create compensation return transaction for transaction {}: {}", 
                     originalTransaction.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to create compensation return transaction", e);
        }
    }
    
    /**
     * Check if a transaction requires compensation.
     */
    public boolean requiresCompensation(Long transactionId) {
        var transactionOpt = transactionCommandService.getTransactionById(transactionId);
        
        if (transactionOpt.isEmpty()) {
            return false;
        }
        
        Transaction transaction = transactionOpt.get();
        
        // Compensation is needed if transaction is not cancelled and not in a final state
        return !transaction.isCancelled() && !transaction.isCompleted();
    }
    
    /**
     * Get compensation status for a transaction.
     */
    public String getCompensationStatus(Long transactionId) {
        var transactionOpt = transactionCommandService.getTransactionById(transactionId);
        
        if (transactionOpt.isEmpty()) {
            return "TRANSACTION_NOT_FOUND";
        }
        
        Transaction transaction = transactionOpt.get();
        
        if (transaction.isCancelled()) {
            return "COMPENSATED";
        } else if (transaction.isCompleted()) {
            return "COMPLETED_NO_COMPENSATION_NEEDED";
        } else {
            return "COMPENSATION_PENDING";
        }
    }
}