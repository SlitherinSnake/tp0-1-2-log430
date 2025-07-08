package com.log430.tp4.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.log430.tp4.domain.inventory.InventoryItem;

/**
 * Repository interface for InventoryItem entities.
 * Provides data access operations for inventory management.
 */
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    /**
     * Find all active inventory items.
     */
    List<InventoryItem> findByIsActiveTrue();

    /**
     * Find inventory items by category.
     */
    List<InventoryItem> findByCategorie(String categorie);

    /**
     * Find inventory items by category (active only).
     */
    List<InventoryItem> findByCategorieAndIsActiveTrue(String categorie);

    /**
     * Find inventory items that need restocking.
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.stockCentral <= i.stockMinimum AND i.isActive = true")
    List<InventoryItem> findItemsNeedingRestock();

    /**
     * Find inventory item by name.
     */
    Optional<InventoryItem> findByNomAndIsActiveTrue(String nom);

    /**
     * Search inventory items by name containing the given text (case insensitive).
     */
    List<InventoryItem> findByNomContainingIgnoreCase(String nom);

    /**
     * Search inventory items by name containing the given text (active only).
     */
    List<InventoryItem> findByNomContainingIgnoreCaseAndIsActiveTrue(String nom);

    /**
     * Find all distinct categories.
     */
    @Query("SELECT DISTINCT i.categorie FROM InventoryItem i WHERE i.isActive = true")
    List<String> findDistinctCategories();

    /**
     * Calculate total inventory value.
     */
    @Query("SELECT SUM(i.prix * i.stockCentral) FROM InventoryItem i WHERE i.isActive = true")
    Double calculateTotalInventoryValue();
}
