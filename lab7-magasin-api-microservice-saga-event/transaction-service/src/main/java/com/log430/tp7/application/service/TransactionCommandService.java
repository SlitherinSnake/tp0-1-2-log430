package com.log430.tp7.application.service;

import com.log430.tp7.domain.transaction.Transaction;
import com.log430.tp7.domain.transaction.Transaction.StatutTransaction;
import com.log430.tp7.infrastructure.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Command service for transaction write operations.
 * Part of CQRS implementation - handles commands that modify state.
 */
@Service
@Transactional
public class TransactionCommandService {
    
    private static final Logger log = LoggerFactory.getLogger(TransactionCommandService.class);
    
    private final TransactionRepository transactionRepository;
    private final TransactionEventService transactionEventService;
    
    public TransactionCommandService(TransactionRepository transactionRepository,
                                   TransactionEventService transactionEventService) {
        this.transactionRepository = transactionRepository;
        this.transactionEventService = transactionEventService;
    }
    
    /**
     * Create a new sale transaction.
     */
    public Transaction createSaleTransaction(Long personnelId, Long storeId, Double montantTotal) {
        log.info("Creating sale transaction for personnel: {} at store: {}", personnelId, storeId);
        
        Transaction transaction = new Transaction(personnelId, storeId);
        transaction.setMontantTotal(montantTotal);
        transaction.setStatut(StatutTransaction.EN_COURS);
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Created sale transaction with id: {}", savedTransaction.getId());
        
        // Publish TransactionCreated event
        transactionEventService.publishTransactionCreated(savedTransaction, null);
        
        return savedTransaction;
    }
    
    /**
     * Create a new return transaction.
     */
    public Transaction createReturnTransaction(Long personnelId, Long storeId, 
                                             Long originalTransactionId, String motifRetour) {
        log.info("Creating return transaction for personnel: {} at store: {}", personnelId, storeId);
        
        Transaction transaction = new Transaction(personnelId, storeId, originalTransactionId, motifRetour);
        transaction.setStatut(StatutTransaction.EN_COURS);
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Created return transaction with id: {}", savedTransaction.getId());
        
        // Publish TransactionCreated event
        transactionEventService.publishTransactionCreated(savedTransaction, null);
        
        return savedTransaction;
    }
    
    /**
     * Complete a transaction.
     */
    public Transaction completeTransaction(Long transactionId) {
        log.info("Completing transaction with id: {}", transactionId);
        
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found with id: " + transactionId));
        
        transaction.complete();
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // Publish TransactionCompleted event
        transactionEventService.publishTransactionCompleted(savedTransaction, null, null);
        
        log.info("Transaction {} completed successfully", transactionId);
        return savedTransaction;
    }
    
    /**
     * Complete a transaction with correlation context.
     */
    public Transaction completeTransaction(Long transactionId, String correlationId, String causationId) {
        log.info("Completing transaction with id: {} with correlation: {}", transactionId, correlationId);
        
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found with id: " + transactionId));
        
        transaction.complete();
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // Publish TransactionCompleted event with context
        transactionEventService.publishTransactionCompleted(savedTransaction, correlationId, causationId);
        
        log.info("Transaction {} completed successfully", transactionId);
        return savedTransaction;
    }
    
    /**
     * Cancel a transaction.
     */
    public Transaction cancelTransaction(Long transactionId) {
        log.info("Cancelling transaction with id: {}", transactionId);
        
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found with id: " + transactionId));
        
        transaction.cancel();
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // Publish TransactionCancelled event
        transactionEventService.publishTransactionCancelled(savedTransaction, "Manual cancellation", null, null);
        
        log.info("Transaction {} cancelled successfully", transactionId);
        return savedTransaction;
    }
    
    /**
     * Cancel a transaction with specific reason and correlation context.
     */
    public Transaction cancelTransaction(Long transactionId, String cancellationReason, 
                                       String correlationId, String causationId) {
        log.info("Cancelling transaction with id: {} for reason: {}", transactionId, cancellationReason);
        
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found with id: " + transactionId));
        
        transaction.cancel();
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // Publish TransactionCancelled event with context
        transactionEventService.publishTransactionCancelled(savedTransaction, cancellationReason, correlationId, causationId);
        
        log.info("Transaction {} cancelled successfully", transactionId);
        return savedTransaction;
    }
    
    /**
     * Add item to transaction.
     */
    public Transaction addItemToTransaction(Long transactionId, Long inventoryItemId, 
                                          Integer quantity, Double unitPrice) {
        log.info("Adding item {} to transaction {}", inventoryItemId, transactionId);
        
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found with id: " + transactionId));
        
        transaction.addItem(inventoryItemId, quantity, unitPrice);
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        log.info("Item {} added to transaction {}", inventoryItemId, transactionId);
        return savedTransaction;
    }
    
    /**
     * Remove item from transaction.
     */
    public Transaction removeItemFromTransaction(Long transactionId, Long inventoryItemId) {
        log.info("Removing item {} from transaction {}", inventoryItemId, transactionId);
        
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found with id: " + transactionId));
        
        transaction.removeItem(inventoryItemId);
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        log.info("Item {} removed from transaction {}", inventoryItemId, transactionId);
        return savedTransaction;
    }
    
    /**
     * Update item quantity in transaction.
     */
    public Transaction updateItemQuantity(Long transactionId, Long inventoryItemId, Integer newQuantity) {
        log.info("Updating item {} quantity to {} in transaction {}", inventoryItemId, newQuantity, transactionId);
        
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found with id: " + transactionId));
        
        transaction.updateItemQuantity(inventoryItemId, newQuantity);
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        log.info("Item {} quantity updated to {} in transaction {}", inventoryItemId, newQuantity, transactionId);
        return savedTransaction;
    }
    
    /**
     * Get transaction by ID for command operations (write-side).
     */
    public Optional<Transaction> getTransactionById(Long id) {
        log.info("Getting transaction by id for command: {}", id);
        return transactionRepository.findById(id);
    }
}