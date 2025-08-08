package com.log430.tp7.infrastructure.repository;

import com.log430.tp7.domain.readmodel.TransactionReadModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for TransactionReadModel - optimized for query operations.
 * Part of the CQRS implementation for read-side operations.
 */
@Repository
public interface TransactionReadModelRepository extends JpaRepository<TransactionReadModel, Long> {
    
    /**
     * Find transactions by personnel ID.
     */
    List<TransactionReadModel> findByPersonnelId(Long personnelId);
    
    /**
     * Find transactions by store ID.
     */
    List<TransactionReadModel> findByStoreId(Long storeId);
    
    /**
     * Find transactions by type.
     */
    List<TransactionReadModel> findByTransactionType(String transactionType);
    
    /**
     * Find transactions by status.
     */
    List<TransactionReadModel> findByStatus(String status);
    
    /**
     * Find transactions by date range.
     */
    List<TransactionReadModel> findByTransactionDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Find transactions by correlation ID.
     */
    List<TransactionReadModel> findByCorrelationId(String correlationId);
    
    /**
     * Find return transactions by original transaction ID.
     */
    List<TransactionReadModel> findByOriginalTransactionId(Long originalTransactionId);
    
    /**
     * Find transactions by store and date range for reporting.
     */
    @Query("SELECT t FROM TransactionReadModel t WHERE t.storeId = :storeId " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.transactionDate DESC")
    List<TransactionReadModel> findByStoreAndDateRange(@Param("storeId") Long storeId,
                                                       @Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);
    
    /**
     * Calculate total sales for a store within a date range.
     */
    @Query("SELECT COALESCE(SUM(t.totalAmount), 0.0) FROM TransactionReadModel t " +
           "WHERE t.storeId = :storeId AND t.transactionType = 'VENTE' " +
           "AND t.status = 'COMPLETEE' " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate")
    Double calculateTotalSales(@Param("storeId") Long storeId,
                              @Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate);
    
    /**
     * Get transaction count by status for dashboard.
     */
    @Query("SELECT t.status, COUNT(t) FROM TransactionReadModel t GROUP BY t.status")
    List<Object[]> getTransactionCountByStatus();
    
    /**
     * Get daily sales summary for a store.
     */
    @Query("SELECT t.transactionDate, COUNT(t), SUM(t.totalAmount) " +
           "FROM TransactionReadModel t " +
           "WHERE t.storeId = :storeId AND t.transactionType = 'VENTE' " +
           "AND t.status = 'COMPLETEE' " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "GROUP BY t.transactionDate " +
           "ORDER BY t.transactionDate DESC")
    List<Object[]> getDailySalesSummary(@Param("storeId") Long storeId,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);
    
    /**
     * Find top performing stores by sales volume.
     */
    @Query("SELECT t.storeId, COUNT(t), SUM(t.totalAmount) " +
           "FROM TransactionReadModel t " +
           "WHERE t.transactionType = 'VENTE' AND t.status = 'COMPLETEE' " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "GROUP BY t.storeId " +
           "ORDER BY SUM(t.totalAmount) DESC")
    List<Object[]> getTopPerformingStores(@Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);
    
    /**
     * Find transactions with high value (above threshold).
     */
    @Query("SELECT t FROM TransactionReadModel t " +
           "WHERE t.totalAmount > :threshold " +
           "ORDER BY t.totalAmount DESC")
    List<TransactionReadModel> findHighValueTransactions(@Param("threshold") Double threshold);
    
    /**
     * Get personnel performance metrics.
     */
    @Query("SELECT t.personnelId, COUNT(t), SUM(t.totalAmount), AVG(t.totalAmount) " +
           "FROM TransactionReadModel t " +
           "WHERE t.transactionType = 'VENTE' AND t.status = 'COMPLETEE' " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "GROUP BY t.personnelId " +
           "ORDER BY SUM(t.totalAmount) DESC")
    List<Object[]> getPersonnelPerformance(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);
}