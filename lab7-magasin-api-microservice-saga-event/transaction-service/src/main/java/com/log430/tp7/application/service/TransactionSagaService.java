package com.log430.tp7.application.service;

import com.log430.tp7.domain.event.PaymentFailed;
import com.log430.tp7.domain.event.PaymentProcessed;
import com.log430.tp7.domain.event.PaymentRefunded;
import com.log430.tp7.domain.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for saga coordination logic in the transaction service.
 * Handles choreographed saga steps and compensation logic.
 */
@Service
@Transactional
public class TransactionSagaService {
    
    private static final Logger log = LoggerFactory.getLogger(TransactionSagaService.class);
    
    private final TransactionCommandService transactionCommandService;
    private final TransactionQueryService transactionQueryService;
    private final TransactionCompensationService compensationService;
    
    public TransactionSagaService(TransactionCommandService transactionCommandService,
                                 TransactionQueryService transactionQueryService,
                                 TransactionCompensationService compensationService) {
        this.transactionCommandService = transactionCommandService;
        this.transactionQueryService = transactionQueryService;
        this.compensationService = compensationService;
    }
    
    /**
     * Handle PaymentProcessed event - complete the transaction.
     */
    public void handlePaymentProcessed(PaymentProcessed event) {
        log.info("Handling PaymentProcessed event for transaction: {} with saga: {}", 
                event.getTransactionId(), event.getSagaId());
        
        try {
            // Verify transaction exists and is in correct state
            var transactionOpt = transactionCommandService.getTransactionById(event.getTransactionId());
            
            if (transactionOpt.isEmpty()) {
                log.error("Transaction not found for PaymentProcessed event: {}", event.getTransactionId());
                return;
            }
            
            Transaction transaction = transactionOpt.get();
            
            // Check if transaction is in a state that can be completed
            if (transaction.isCompleted()) {
                log.warn("Transaction {} is already completed, ignoring PaymentProcessed event", 
                        event.getTransactionId());
                return;
            }
            
            if (transaction.isCancelled()) {
                log.warn("Transaction {} is cancelled, cannot process PaymentProcessed event", 
                        event.getTransactionId());
                return;
            }
            
            // Complete the transaction
            transactionCommandService.completeTransaction(
                event.getTransactionId(),
                event.getCorrelationId(),
                event.createCausationId()
            );
            
            log.info("Successfully completed transaction {} after payment processing", 
                    event.getTransactionId());
            
        } catch (Exception e) {
            log.error("Failed to handle PaymentProcessed event for transaction {}: {}", 
                     event.getTransactionId(), e.getMessage(), e);
            throw new RuntimeException("Failed to complete transaction after payment", e);
        }
    }
    
    /**
     * Handle PaymentFailed event - cancel the transaction.
     */
    public void handlePaymentFailed(PaymentFailed event) {
        log.info("Handling PaymentFailed event for transaction: {} with saga: {}", 
                event.getTransactionId(), event.getSagaId());
        
        try {
            // Verify transaction exists and is in correct state
            var transactionOpt = transactionCommandService.getTransactionById(event.getTransactionId());
            
            if (transactionOpt.isEmpty()) {
                log.error("Transaction not found for PaymentFailed event: {}", event.getTransactionId());
                return;
            }
            
            Transaction transaction = transactionOpt.get();
            
            // Check if transaction is in a state that can be cancelled
            if (transaction.isCancelled()) {
                log.warn("Transaction {} is already cancelled, ignoring PaymentFailed event", 
                        event.getTransactionId());
                return;
            }
            
            if (transaction.isCompleted()) {
                log.warn("Transaction {} is completed, cannot cancel for PaymentFailed event", 
                        event.getTransactionId());
                return;
            }
            
            // Use compensation service to handle payment failure
            String failureReason = String.format("Payment failed: %s (%s)", 
                                                event.getFailureReason(), event.getErrorCode());
            
            compensationService.compensateForPaymentFailure(
                event.getTransactionId(),
                failureReason,
                event.getCorrelationId(),
                event.createCausationId()
            );
            
            log.info("Successfully cancelled transaction {} after payment failure", 
                    event.getTransactionId());
            
        } catch (Exception e) {
            log.error("Failed to handle PaymentFailed event for transaction {}: {}", 
                     event.getTransactionId(), e.getMessage(), e);
            throw new RuntimeException("Failed to cancel transaction after payment failure", e);
        }
    }
    
    /**
     * Handle PaymentRefunded event - implement compensation logic.
     */
    public void handlePaymentRefunded(PaymentRefunded event) {
        log.info("Handling PaymentRefunded event for transaction: {} with saga: {}", 
                event.getTransactionId(), event.getSagaId());
        
        try {
            // Verify transaction exists
            var transactionOpt = transactionCommandService.getTransactionById(event.getTransactionId());
            
            if (transactionOpt.isEmpty()) {
                log.error("Transaction not found for PaymentRefunded event: {}", event.getTransactionId());
                return;
            }
            
            Transaction transaction = transactionOpt.get();
            
            // Handle refund based on current transaction state
            if (transaction.isCompleted()) {
                // If transaction was completed, we need to create a compensation transaction
                handleCompletedTransactionRefund(transaction, event);
            } else if (!transaction.isCancelled()) {
                // If transaction is still in progress, cancel it
                String cancellationReason = String.format("Payment refunded: %s", event.getRefundReason());
                
                transactionCommandService.cancelTransaction(
                    event.getTransactionId(),
                    cancellationReason,
                    event.getCorrelationId(),
                    event.createCausationId()
                );
                
                log.info("Cancelled in-progress transaction {} due to payment refund", 
                        event.getTransactionId());
            } else {
                log.info("Transaction {} is already cancelled, no action needed for refund", 
                        event.getTransactionId());
            }
            
        } catch (Exception e) {
            log.error("Failed to handle PaymentRefunded event for transaction {}: {}", 
                     event.getTransactionId(), e.getMessage(), e);
            throw new RuntimeException("Failed to handle payment refund", e);
        }
    }
    
    /**
     * Handle refund for a completed transaction by creating a return transaction.
     */
    private void handleCompletedTransactionRefund(Transaction originalTransaction, PaymentRefunded event) {
        log.info("Creating return transaction for completed transaction {} due to refund", 
                originalTransaction.getId());
        
        try {
            // Create a return transaction as compensation
            Transaction returnTransaction = transactionCommandService.createReturnTransaction(
                originalTransaction.getPersonnelId(),
                originalTransaction.getStoreId(),
                originalTransaction.getId(),
                "Payment refund: " + event.getRefundReason()
            );
            
            // Copy items from original transaction to return transaction
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
            transactionCommandService.completeTransaction(
                returnTransaction.getId(),
                event.getCorrelationId(),
                event.createCausationId()
            );
            
            log.info("Successfully created and completed return transaction {} for refund of transaction {}", 
                    returnTransaction.getId(), originalTransaction.getId());
            
        } catch (Exception e) {
            log.error("Failed to create return transaction for refund of transaction {}: {}", 
                     originalTransaction.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to create compensation transaction", e);
        }
    }
    
    /**
     * Check if a transaction is eligible for saga operations.
     */
    private boolean isTransactionEligibleForSaga(Transaction transaction) {
        return transaction != null && !transaction.isCancelled();
    }
    
    /**
     * Get saga correlation context for logging and tracing.
     */
    private String getSagaContext(String sagaId, String correlationId) {
        return String.format("Saga: %s, Correlation: %s", sagaId, correlationId);
    }
}