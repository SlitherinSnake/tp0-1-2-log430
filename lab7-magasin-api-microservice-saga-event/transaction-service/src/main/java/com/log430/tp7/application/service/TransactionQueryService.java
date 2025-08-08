package com.log430.tp7.application.service;

import com.log430.tp7.domain.readmodel.TransactionReadModel;
import com.log430.tp7.infrastructure.repository.TransactionReadModelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Query service for transaction read operations.
 * Part of CQRS implementation - handles queries that read data.
 */
@Service
@Transactional(readOnly = true)
public class TransactionQueryService {
    
    private static final Logger log = LoggerFactory.getLogger(TransactionQueryService.class);
    
    private final TransactionReadModelRepository readModelRepository;
    
    public TransactionQueryService(TransactionReadModelRepository readModelRepository) {
        this.readModelRepository = readModelRepository;
    }
    
    /**
     * Get all transactions from read model.
     */
    public List<TransactionReadModel> getAllTransactions() {
        log.info("Fetching all transactions from read model");
        return readModelRepository.findAll();
    }
    
    /**
     * Get transaction by ID from read model.
     */
    public Optional<TransactionReadModel> getTransactionById(Long id) {
        log.info("Getting transaction by id from read model: {}", id);
        return readModelRepository.findById(id);
    }
    
    /**
     * Get transactions by personnel ID.
     */
    public List<TransactionReadModel> getTransactionsByPersonnel(Long personnelId) {
        log.info("Getting transactions for personnel from read model: {}", personnelId);
        return readModelRepository.findByPersonnelId(personnelId);
    }
    
    /**
     * Get transactions by store ID.
     */
    public List<TransactionReadModel> getTransactionsByStore(Long storeId) {
        log.info("Getting transactions for store from read model: {}", storeId);
        return readModelRepository.findByStoreId(storeId);
    }
    
    /**
     * Get transactions by type.
     */
    public List<TransactionReadModel> getTransactionsByType(String type) {
        log.info("Fetching transactions by type from read model: {}", type);
        return readModelRepository.findByTransactionType(type);
    }
    
    /**
     * Get transactions by status.
     */
    public List<TransactionReadModel> getTransactionsByStatus(String status) {
        log.info("Fetching transactions by status from read model: {}", status);
        return readModelRepository.findByStatus(status);
    }
    
    /**
     * Get transactions by date range.
     */
    public List<TransactionReadModel> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching transactions between {} and {} from read model", startDate, endDate);
        return readModelRepository.findByTransactionDateBetween(startDate, endDate);
    }
    
    /**
     * Get transactions by correlation ID.
     */
    public List<TransactionReadModel> getTransactionsByCorrelationId(String correlationId) {
        log.info("Getting transactions by correlation ID from read model: {}", correlationId);
        return readModelRepository.findByCorrelationId(correlationId);
    }
    
    /**
     * Get return transactions by original transaction ID.
     */
    public List<TransactionReadModel> getReturnsByOriginalTransactionId(Long originalTransactionId) {
        log.info("Fetching returns for original transaction from read model: {}", originalTransactionId);
        return readModelRepository.findByOriginalTransactionId(originalTransactionId);
    }
    
    /**
     * Get transactions by store and date range for reporting.
     */
    public List<TransactionReadModel> getTransactionsByStoreAndDateRange(Long storeId, 
                                                                        LocalDate startDate, 
                                                                        LocalDate endDate) {
        log.info("Getting transactions for store {} between {} and {} from read model", 
                storeId, startDate, endDate);
        return readModelRepository.findByStoreAndDateRange(storeId, startDate, endDate);
    }
    
    /**
     * Calculate total sales for a store within a date range.
     */
    public Double calculateTotalSales(Long storeId, LocalDate startDate, LocalDate endDate) {
        log.info("Calculating total sales for store {} between {} and {} from read model", 
                storeId, startDate, endDate);
        Double totalSales = readModelRepository.calculateTotalSales(storeId, startDate, endDate);
        return totalSales != null ? totalSales : 0.0;
    }
    
    /**
     * Get transaction count by status for dashboard.
     */
    public List<Object[]> getTransactionCountByStatus() {
        log.info("Getting transaction count by status from read model");
        return readModelRepository.getTransactionCountByStatus();
    }
    
    /**
     * Get daily sales summary for a store.
     */
    public List<Object[]> getDailySalesSummary(Long storeId, LocalDate startDate, LocalDate endDate) {
        log.info("Getting daily sales summary for store {} from read model", storeId);
        return readModelRepository.getDailySalesSummary(storeId, startDate, endDate);
    }
    
    /**
     * Find top performing stores by sales volume.
     */
    public List<Object[]> getTopPerformingStores(LocalDate startDate, LocalDate endDate) {
        log.info("Getting top performing stores from read model");
        return readModelRepository.getTopPerformingStores(startDate, endDate);
    }
    
    /**
     * Find transactions with high value (above threshold).
     */
    public List<TransactionReadModel> getHighValueTransactions(Double threshold) {
        log.info("Getting high value transactions above {} from read model", threshold);
        return readModelRepository.findHighValueTransactions(threshold);
    }
    
    /**
     * Get personnel performance metrics.
     */
    public List<Object[]> getPersonnelPerformance(LocalDate startDate, LocalDate endDate) {
        log.info("Getting personnel performance metrics from read model");
        return readModelRepository.getPersonnelPerformance(startDate, endDate);
    }
    
    /**
     * Get total transaction count.
     */
    public long getTransactionCount() {
        log.info("Getting total transaction count from read model");
        return readModelRepository.count();
    }
    
    /**
     * Get total sales amount.
     */
    public double getTotalSalesAmount() {
        log.info("Getting total sales amount from read model");
        return readModelRepository.findAll().stream()
                .filter(t -> "VENTE".equals(t.getTransactionType()) && "COMPLETEE".equals(t.getStatus()))
                .mapToDouble(t -> t.getTotalAmount() != null ? t.getTotalAmount() : 0.0)
                .sum();
    }
}