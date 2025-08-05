package com.log430.tp6.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.log430.tp6.domain.inventory.StoreInventory;

/**
 * Repository interface for StoreInventory entities.
 * Provides data access operations for store-specific inventory management.
 */
public interface StoreInventoryRepository extends JpaRepository<StoreInventory, Long> {

    /**
     * Find store inventory by store ID.
     */
    List<StoreInventory> findByStoreId(Long storeId);

    /**
     * Find store inventory by inventory item ID.
     */
    List<StoreInventory> findByInventoryItemId(Long inventoryItemId);

    /**
     * Find store inventory for a specific item in a specific store.
     */
    Optional<StoreInventory> findByInventoryItemIdAndStoreId(Long inventoryItemId, Long storeId);

    /**
     * Find all pending transfer requests.
     */
    @Query("SELECT si FROM StoreInventory si WHERE si.statutDemande = 'EN_ATTENTE'")
    List<StoreInventory> findPendingTransferRequests();

    /**
     * Find transfer requests for a specific store.
     */
    @Query("SELECT si FROM StoreInventory si WHERE si.storeId = :storeId AND si.statutDemande = 'EN_ATTENTE'")
    List<StoreInventory> findPendingTransferRequestsByStore(@Param("storeId") Long storeId);

    /**
     * Calculate total local stock value for a store.
     */
    @Query("SELECT SUM(si.quantiteLocale * si.inventoryItem.prix) FROM StoreInventory si WHERE si.storeId = :storeId")
    Double calculateTotalStoreInventoryValue(@Param("storeId") Long storeId);
}
