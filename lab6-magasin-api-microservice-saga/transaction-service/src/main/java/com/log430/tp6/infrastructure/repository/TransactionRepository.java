package com.log430.tp6.infrastructure.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.log430.tp6.domain.transaction.Transaction;
import com.log430.tp6.domain.transaction.Transaction.StatutTransaction;
import com.log430.tp6.domain.transaction.Transaction.TypeTransaction;

/**
 * Repository interface for Transaction entities.
 * Provides data access operations for transaction management.
 */
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Find transactions by store ID.
     */
    List<Transaction> findByStoreId(Long storeId);

    /**
     * Find transactions by personnel ID.
     */
    List<Transaction> findByPersonnelId(Long personnelId);

    /**
     * Find transactions by type.
     */
    List<Transaction> findByTypeTransaction(TypeTransaction typeTransaction);

    /**
     * Find transactions by status.
     */
    List<Transaction> findByStatut(StatutTransaction statut);

    /**
     * Find transactions by date range.
     */
    List<Transaction> findByDateTransactionBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Find sales transactions for a store within a date range.
     */
    @Query("SELECT t FROM Transaction t WHERE t.storeId = :storeId AND t.typeTransaction = 'VENTE' AND t.dateTransaction BETWEEN :startDate AND :endDate")
    List<Transaction> findSalesByStoreAndDateRange(@Param("storeId") Long storeId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Find return transactions for a store within a date range.
     */
    @Query("SELECT t FROM Transaction t WHERE t.storeId = :storeId AND t.typeTransaction = 'RETOUR' AND t.dateTransaction BETWEEN :startDate AND :endDate")
    List<Transaction> findReturnsByStoreAndDateRange(@Param("storeId") Long storeId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Calculate total sales amount for a store within a date range.
     */
    @Query("SELECT SUM(t.montantTotal) FROM Transaction t WHERE t.storeId = :storeId AND t.typeTransaction = 'VENTE' AND t.statut = 'COMPLETEE' AND t.dateTransaction BETWEEN :startDate AND :endDate")
    Double calculateTotalSales(@Param("storeId") Long storeId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Find returns by original transaction ID.
     */
    List<Transaction> findByTransactionOriginaleId(Long transactionOriginaleId);

    /**
     * Find completed transactions for a store.
     */
    List<Transaction> findByStoreIdAndStatut(Long storeId, StatutTransaction statut);
}
