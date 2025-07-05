package com.log430.tp4.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.log430.tp4.domain.inventory.InventoryItem;
import com.log430.tp4.infrastructure.repository.InventoryItemRepository;

/**
 * Application service for inventory management operations.
 * Coordinates domain logic and data access for inventory items.
 */
@Service
@Transactional
public class InventoryService {

    private static final String ITEM_NOT_FOUND_MSG = "Item not found with id: ";

    private final InventoryItemRepository inventoryItemRepository;

    public InventoryService(InventoryItemRepository inventoryItemRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
    }

    /**
     * Get all active inventory items.
     */
    @Transactional(readOnly = true)
    public List<InventoryItem> getAllActiveItems() {
        return inventoryItemRepository.findByIsActiveTrue();
    }

    /**
     * Get inventory item by ID.
     */
    @Transactional(readOnly = true)
    public Optional<InventoryItem> getItemById(Long id) {
        return inventoryItemRepository.findById(id);
    }

    /**
     * Create new inventory item.
     */
    public InventoryItem createItem(String nom, String categorie, Double prix, Integer stockCentral) {
        InventoryItem item = new InventoryItem(nom, categorie, prix, stockCentral);
        return inventoryItemRepository.save(item);
    }

    /**
     * Update inventory item.
     */
    public InventoryItem updateItem(Long id, String nom, String categorie, Double prix, String description) {
        return inventoryItemRepository.findById(id)
                .map(item -> {
                    item.setNom(nom);
                    item.setCategorie(categorie);
                    item.setPrix(prix);
                    item.setDescription(description);
                    return inventoryItemRepository.save(item);
                })
                .orElseThrow(() -> new IllegalArgumentException(ITEM_NOT_FOUND_MSG + id));
    }

    /**
     * Update stock levels.
     */
    public InventoryItem updateStock(Long id, Integer newStock) {
        return inventoryItemRepository.findById(id)
                .map(item -> {
                    item.setStockCentral(newStock);
                    return inventoryItemRepository.save(item);
                })
                .orElseThrow(() -> new IllegalArgumentException(ITEM_NOT_FOUND_MSG + id));
    }

    /**
     * Increase stock (receiving new inventory).
     */
    public InventoryItem increaseStock(Long id, Integer quantity) {
        return inventoryItemRepository.findById(id)
                .map(item -> {
                    item.increaseStock(quantity);
                    return inventoryItemRepository.save(item);
                })
                .orElseThrow(() -> new IllegalArgumentException(ITEM_NOT_FOUND_MSG + id));
    }

    /**
     * Decrease stock (sales or transfers).
     */
    public InventoryItem decreaseStock(Long id, Integer quantity) {
        return inventoryItemRepository.findById(id)
                .map(item -> {
                    item.decreaseStock(quantity);
                    return inventoryItemRepository.save(item);
                })
                .orElseThrow(() -> new IllegalArgumentException(ITEM_NOT_FOUND_MSG + id));
    }

    /**
     * Get items needing restock.
     */
    @Transactional(readOnly = true)
    public List<InventoryItem> getItemsNeedingRestock() {
        return inventoryItemRepository.findItemsNeedingRestock();
    }

    /**
     * Get items by category.
     */
    @Transactional(readOnly = true)
    public List<InventoryItem> getItemsByCategory(String categorie) {
        return inventoryItemRepository.findByCategorieAndIsActiveTrue(categorie);
    }

    /**
     * Search items by name.
     */
    @Transactional(readOnly = true)
    public List<InventoryItem> searchItemsByName(String name) {
        return inventoryItemRepository.findByNomContainingIgnoreCaseAndIsActiveTrue(name);
    }

    /**
     * Get all distinct categories.
     */
    @Transactional(readOnly = true)
    public List<String> getDistinctCategories() {
        return inventoryItemRepository.findDistinctCategories();
    }

    /**
     * Calculate total inventory value.
     */
    @Transactional(readOnly = true)
    public Double calculateTotalInventoryValue() {
        return inventoryItemRepository.calculateTotalInventoryValue();
    }

    /**
     * Deactivate inventory item.
     */
    public void deactivateItem(Long id) {
        inventoryItemRepository.findById(id)
                .ifPresentOrElse(
                        item -> {
                            item.setActive(false);
                            inventoryItemRepository.save(item);
                        },
                        () -> {
                            throw new IllegalArgumentException(ITEM_NOT_FOUND_MSG + id);
                        }
                );
    }
}
