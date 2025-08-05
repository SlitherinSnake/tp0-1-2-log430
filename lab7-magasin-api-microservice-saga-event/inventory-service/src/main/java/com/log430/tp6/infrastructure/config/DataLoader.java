package com.log430.tp7.infrastructure.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.log430.tp7.domain.inventory.InventoryItem;
import com.log430.tp7.infrastructure.repository.InventoryItemRepository;

/**
 * Data loader to initialize sample inventory items.
 */
@Component
public class DataLoader implements CommandLineRunner {
    
    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);
    
    private final InventoryItemRepository inventoryItemRepository;
    
    public DataLoader(InventoryItemRepository inventoryItemRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
    }
    
    @Override
    public void run(String... args) throws Exception {
        if (inventoryItemRepository.count() == 0) {
            log.info("Database is empty, loading sample data...");
            loadSampleData();
            log.info("Sample data loaded successfully");
        } else {
            log.info("Database already contains {} items, skipping data loading", inventoryItemRepository.count());
        }
    }
    
    private static final String CATEGORY_ELECTRONICS = "Électronique";
    private static final String CATEGORY_AUDIO = "Audio";
    private static final String CATEGORY_CLOTHING = "Vêtements";
    private static final String CATEGORY_PHOTO = "Photo";
    private static final String CATEGORY_BOOKS = "Livres";
    private static final String CATEGORY_FURNITURE = "Meubles";

    private void loadSampleData() {
        List<InventoryItem> sampleItems = List.of(
            new InventoryItem("iPhone 14", CATEGORY_ELECTRONICS, 999.99, 25),
            new InventoryItem("Samsung Galaxy S23", CATEGORY_ELECTRONICS, 899.99, 30),
            new InventoryItem("MacBook Pro", CATEGORY_ELECTRONICS, 1999.99, 15),
            new InventoryItem("Dell XPS 13", CATEGORY_ELECTRONICS, 1299.99, 20),
            new InventoryItem("Sony WH-1000XM4", CATEGORY_AUDIO, 349.99, 40),
            new InventoryItem("AirPods Pro", CATEGORY_AUDIO, 249.99, 35),
            new InventoryItem("Nike Air Max", CATEGORY_CLOTHING, 129.99, 50),
            new InventoryItem("Adidas Ultraboost", CATEGORY_CLOTHING, 149.99, 45),
            new InventoryItem("Levi's Jeans", CATEGORY_CLOTHING, 79.99, 60),
            new InventoryItem("Canon EOS R5", CATEGORY_PHOTO, 3899.99, 8),
            new InventoryItem("Sony A7 IV", CATEGORY_PHOTO, 2499.99, 12),
            new InventoryItem("GoPro Hero 11", CATEGORY_PHOTO, 399.99, 22),
            new InventoryItem("Kindle Paperwhite", CATEGORY_BOOKS, 139.99, 18),
            new InventoryItem("Fire TV Stick", CATEGORY_ELECTRONICS, 39.99, 75),
            new InventoryItem("Echo Dot", CATEGORY_ELECTRONICS, 49.99, 65),
            new InventoryItem("Gaming Chair", CATEGORY_FURNITURE, 299.99, 12),
            new InventoryItem("Standing Desk", CATEGORY_FURNITURE, 599.99, 8),
            new InventoryItem("Monitor 4K", CATEGORY_ELECTRONICS, 449.99, 16),
            new InventoryItem("Mechanical Keyboard", CATEGORY_ELECTRONICS, 149.99, 28),
            new InventoryItem("Gaming Mouse", CATEGORY_ELECTRONICS, 79.99, 32)
        );
        
        // Set minimum stock levels for some items
        sampleItems.get(0).setStockMinimum(10); // iPhone 14
        sampleItems.get(1).setStockMinimum(15); // Samsung Galaxy S23
        sampleItems.get(2).setStockMinimum(5);  // MacBook Pro
        sampleItems.get(9).setStockMinimum(5);  // Canon EOS R5
        sampleItems.get(10).setStockMinimum(8); // Sony A7 IV
        
        inventoryItemRepository.saveAll(sampleItems);
        log.info("Loaded {} sample inventory items", sampleItems.size());
    }
}
