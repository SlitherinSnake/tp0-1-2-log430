package com.log430.tp4.application.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private static final String ITEM_NOT_FOUND_MSG = "Item not found with id: ";

    private final InventoryItemRepository inventoryItemRepository;

    public InventoryService(InventoryItemRepository inventoryItemRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
    }

    /**
     * Get all active inventory items.
     */
    @Transactional(readOnly = true)
    // @Cacheable("inventoryAll") // Temporarily disabled for debugging
    public List<InventoryItem> getAllActiveItems() {
        log.info("Fetching all active inventory items");
        List<InventoryItem> items = inventoryItemRepository.findByIsActiveTrue();
        log.info("Found {} active items in database", items.size());
        return items;
    }

    /**
     * Get inventory item by ID.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "inventoryById", key = "#id")
    public Optional<InventoryItem> getItemById(Long id) {
        log.info("Fetching inventory item by id: {}", id);
        return inventoryItemRepository.findById(id);
    }

    /**
     * Create new inventory item.
     */
    @Caching(evict = {
        @CacheEvict(value = "inventoryAll", allEntries = true),
        @CacheEvict(value = "inventoryById", key = "#result.id", condition = "#result != null")
    })
    public InventoryItem createItem(String nom, String categorie, Double prix, Integer stockCentral) {
        log.info("Creating new inventory item: {}", nom);
        InventoryItem item = new InventoryItem(nom, categorie, prix, stockCentral);
        return inventoryItemRepository.save(item);
    }

    /**
     * Update inventory item.
     */
    @Caching(evict = {
        @CacheEvict(value = "inventoryAll", allEntries = true),
        @CacheEvict(value = "inventoryById", key = "#id")
    })
    public InventoryItem updateItem(Long id, String nom, String categorie, Double prix, String description) {
        log.info("Updating inventory item with id: {}", id);
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
    @Caching(evict = {
        @CacheEvict(value = "inventoryAll", allEntries = true),
        @CacheEvict(value = "inventoryById", key = "#id")
    })
    public InventoryItem updateStock(Long id, Integer newStock) {
        log.info("Updating stock for item id: {} to {}", id, newStock);
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
    @Caching(evict = {
        @CacheEvict(value = "inventoryAll", allEntries = true),
        @CacheEvict(value = "inventoryById", key = "#id")
    })
    public InventoryItem increaseStock(Long id, Integer quantity) {
        log.info("Increasing stock for item id: {} by {}", id, quantity);
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
    @Caching(evict = {
        @CacheEvict(value = "inventoryAll", allEntries = true),
        @CacheEvict(value = "inventoryById", key = "#id")
    })
    public InventoryItem decreaseStock(Long id, Integer quantity) {
        log.info("Decreasing stock for item id: {} by {}", id, quantity);
        return inventoryItemRepository.findById(id)
                .map(item -> {
                    item.reduceStock(quantity);
                    return inventoryItemRepository.save(item);
                })
                .orElseThrow(() -> new IllegalArgumentException(ITEM_NOT_FOUND_MSG + id));
    }

    /**
     * Get items needing restock.
     */
    @Transactional(readOnly = true)
    @Cacheable("restockNeeded")
    public List<InventoryItem> getItemsNeedingRestock() {
        log.info("Fetching items needing restock");
        return inventoryItemRepository.findItemsNeedingRestock();
    }

    /**
     * Get items by category.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "inventoryByCategory", key = "#categorie")
    public List<InventoryItem> getItemsByCategory(String categorie) {
        log.info("Fetching items by category: {}", categorie);
        return inventoryItemRepository.findByCategorieAndIsActiveTrue(categorie);
    }

    /**
     * Search items by name.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "inventoryByName", key = "#name")
    public List<InventoryItem> searchItemsByName(String name) {
        log.info("Searching items by name: {}", name);
        return inventoryItemRepository.findByNomContainingIgnoreCaseAndIsActiveTrue(name);
    }

    /**
     * Get all distinct categories.
     */
    @Transactional(readOnly = true)
    @Cacheable("categories")
    public List<String> getDistinctCategories() {
        log.info("Fetching distinct categories");
        return inventoryItemRepository.findDistinctCategories();
    }

    /**
     * Calculate total inventory value.
     */
    @Transactional(readOnly = true)
    @Cacheable("totalInventoryValue")
    public Double calculateTotalInventoryValue() {
        log.info("Calculating total inventory value");
        return inventoryItemRepository.calculateTotalInventoryValue();
    }

    /**
     * Deactivate inventory item.
     */
    @Caching(evict = {
        @CacheEvict(value = "inventoryAll", allEntries = true),
        @CacheEvict(value = "inventoryById", key = "#id"),
        @CacheEvict(value = "restockNeeded", allEntries = true),
        @CacheEvict(value = "categories", allEntries = true),
        @CacheEvict(value = "totalInventoryValue", allEntries = true)
    })
    public void deactivateItem(Long id) {
        log.warn("Deactivating inventory item with id: {}", id);
        inventoryItemRepository.findById(id)
                .ifPresentOrElse(
                        item -> {
                            item.setActive(false);
                            inventoryItemRepository.save(item);
                        },
                        () -> {
                            log.error("Item not found for deactivation: {}", id);
                            throw new IllegalArgumentException(ITEM_NOT_FOUND_MSG + id);
                        }
                );
    }

    /**
     * Reduce stock for an inventory item.
     */
    @Caching(evict = {
        @CacheEvict(value = "inventoryAll", allEntries = true),
        @CacheEvict(value = "inventoryById", key = "#id")
    })
    public void reduceStock(Long id, Integer quantity) {
        log.info("Reducing stock for item id: {} by quantity: {}", id, quantity);
        inventoryItemRepository.findById(id)
                .ifPresentOrElse(
                        item -> {
                            item.reduceStock(quantity);
                            inventoryItemRepository.save(item);
                        },
                        () -> {
                            log.error("Item not found for stock reduction: {}", id);
                            throw new IllegalArgumentException(ITEM_NOT_FOUND_MSG + id);
                        }
                );
    }
}
