package com.log430.tp7.infrastructure.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.log430.tp7.domain.inventory.readmodel.InventoryReadModel;

/**
 * Repository interface for InventoryReadModel entities.
 * Provides optimized read operations for inventory queries.
 */
public interface InventoryReadModelRepository extends JpaRepository<InventoryReadModel, Long> {
    
    /**
     * Find all active inventory items.
     */
    List<InventoryReadModel> findByIsActiveTrueOrderByNomAsc();
    
    /**
     * Find items by category.
     */
    List<InventoryReadModel> findByCategorieAndIsActiveTrueOrderByNomAsc(String categorie);
    
    /**
     * Find items needing restock.
     */
    List<InventoryReadModel> findByNeedsRestockTrueAndIsActiveTrueOrderByNomAsc();
    
    /**
     * Search items by name (case-insensitive).
     */
    List<InventoryReadModel> findByNomContainingIgnoreCaseAndIsActiveTrueOrderByNomAsc(String nom);
    
    /**
     * Find items with low stock (available stock below minimum).
     */
    @Query("SELECT i FROM InventoryReadModel i WHERE i.stockAvailable <= i.stockMinimum AND i.isActive = true ORDER BY i.nom ASC")
    List<InventoryReadModel> findLowStockItems();
    
    /**
     * Find items with no available stock.
     */
    @Query("SELECT i FROM InventoryReadModel i WHERE i.stockAvailable <= 0 AND i.isActive = true ORDER BY i.nom ASC")
    List<InventoryReadModel> findOutOfStockItems();
    
    /**
     * Get distinct categories.
     */
    @Query("SELECT DISTINCT i.categorie FROM InventoryReadModel i WHERE i.isActive = true ORDER BY i.categorie ASC")
    List<String> findDistinctCategories();
    
    /**
     * Calculate total inventory value.
     */
    @Query("SELECT COALESCE(SUM(i.totalValue), 0.0) FROM InventoryReadModel i WHERE i.isActive = true")
    Double calculateTotalInventoryValue();
    
    /**
     * Calculate total available stock value.
     */
    @Query("SELECT COALESCE(SUM(i.prix * i.stockAvailable), 0.0) FROM InventoryReadModel i WHERE i.isActive = true")
    Double calculateTotalAvailableValue();
    
    /**
     * Calculate total reserved stock value.
     */
    @Query("SELECT COALESCE(SUM(i.prix * i.stockReserved), 0.0) FROM InventoryReadModel i WHERE i.isActive = true")
    Double calculateTotalReservedValue();
    
    /**
     * Find items with stock above a certain threshold.
     */
    @Query("SELECT i FROM InventoryReadModel i WHERE i.stockAvailable >= :threshold AND i.isActive = true ORDER BY i.nom ASC")
    List<InventoryReadModel> findItemsWithStockAbove(@Param("threshold") Integer threshold);
    
    /**
     * Find items by price range.
     */
    @Query("SELECT i FROM InventoryReadModel i WHERE i.prix BETWEEN :minPrice AND :maxPrice AND i.isActive = true ORDER BY i.prix ASC")
    List<InventoryReadModel> findItemsByPriceRange(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);
    
    /**
     * Get inventory summary statistics.
     */
    @Query("SELECT COUNT(i), COALESCE(SUM(i.stockCentral), 0), COALESCE(SUM(i.stockReserved), 0), COALESCE(SUM(i.stockAvailable), 0) " +
           "FROM InventoryReadModel i WHERE i.isActive = true")
    Object[] getInventorySummary();
    
    /**
     * Find items with reservations.
     */
    @Query("SELECT i FROM InventoryReadModel i WHERE i.stockReserved > 0 AND i.isActive = true ORDER BY i.stockReserved DESC")
    List<InventoryReadModel> findItemsWithReservations();
    
    /**
     * Count items by category.
     */
    @Query("SELECT i.categorie, COUNT(i) FROM InventoryReadModel i WHERE i.isActive = true GROUP BY i.categorie ORDER BY i.categorie ASC")
    List<Object[]> countItemsByCategory();
}