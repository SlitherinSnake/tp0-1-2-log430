package com.log430.tp6.application.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.log430.tp6.domain.transaction.Transaction;
import com.log430.tp6.domain.transaction.Transaction.StatutTransaction;
import com.log430.tp6.domain.transaction.Transaction.TypeTransaction;
import com.log430.tp6.infrastructure.repository.TransactionRepository;

/**
 * Application service for transaction management operations.
 * Coordinates domain logic and data access for transactions.
 */
@Service
@Transactional
public class TransactionService {
    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
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
        
        return savedTransaction;
    }

    /**
     * Create a new return transaction.
     */
    public Transaction createReturnTransaction(Long personnelId, Long storeId, Long originalTransactionId, String motifRetour) {
        log.info("Creating return transaction for personnel: {} at store: {}", personnelId, storeId);
        
        Transaction transaction = new Transaction(personnelId, storeId, originalTransactionId, motifRetour);
        transaction.setStatut(StatutTransaction.EN_COURS);
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Created return transaction with id: {}", savedTransaction.getId());
        
        return savedTransaction;
    }

    /**
     * Get all transactions.
     */
    @Transactional(readOnly = true)
    public List<Transaction> getAllTransactions() {
        log.info("Fetching all transactions");
        return transactionRepository.findAll();
    }

    /**
     * Get transaction by ID.
     */
    public Optional<Transaction> getTransactionById(Long id) {
        log.info("Getting transaction by id: {}", id);
        return transactionRepository.findById(id);
    }

    /**
     * Get transactions by personnel ID.
     */
    public List<Transaction> getTransactionsByPersonnel(Long personnelId) {
        log.info("Getting transactions for personnel: {}", personnelId);
        return transactionRepository.findByPersonnelId(personnelId);
    }

    /**
     * Get transactions by store ID.
     */
    public List<Transaction> getTransactionsByStore(Long storeId) {
        log.info("Getting transactions for store: {}", storeId);
        return transactionRepository.findByStoreId(storeId);
    }

    /**
     * Get transactions by type.
     */
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByType(TypeTransaction type) {
        log.info("Fetching transactions by type: {}", type);
        return transactionRepository.findByTypeTransaction(type);
    }

    /**
     * Get transactions by status.
     */
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByStatus(StatutTransaction status) {
        log.info("Fetching transactions by status: {}", status);
        return transactionRepository.findByStatut(status);
    }

    /**
     * Get transactions by date range.
     */
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching transactions between {} and {}", startDate, endDate);
        return transactionRepository.findByDateTransactionBetween(startDate, endDate);
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
        
        log.info("Transaction {} cancelled successfully", transactionId);
        return savedTransaction;
    }

    /**
     * Add item to transaction.
     */
    public Transaction addItemToTransaction(Long transactionId, Long inventoryItemId, Integer quantity, Double unitPrice) {
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
     * Calculate total sales for a store within a date range.
     */
    @Transactional(readOnly = true)
    public Double calculateTotalSales(Long storeId, LocalDate startDate, LocalDate endDate) {
        log.info("Calculating total sales for store {} between {} and {}", storeId, startDate, endDate);
        Double totalSales = transactionRepository.calculateTotalSales(storeId, startDate, endDate);
        return totalSales != null ? totalSales : 0.0;
    }

    /**
     * Get returns by original transaction ID.
     */
    @Transactional(readOnly = true)
    public List<Transaction> getReturnsByOriginalTransactionId(Long originalTransactionId) {
        log.info("Fetching returns for original transaction: {}", originalTransactionId);
        return transactionRepository.findByTransactionOriginaleId(originalTransactionId);
    }

    /**
     * Get total transaction count.
     */
    public long getTransactionCount() {
        log.info("Getting total transaction count");
        return transactionRepository.count();
    }

    /**
     * Get total sales amount.
     */
    public double getTotalSalesAmount() {
        log.info("Getting total sales amount");
        return transactionRepository.findAll().stream()
                .filter(t -> t.getTypeTransaction() == TypeTransaction.VENTE)
                .mapToDouble(t -> t.getMontantTotal() != null ? t.getMontantTotal() : 0.0)
                .sum();
    }
}
