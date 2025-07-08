package com.log430.tp5.domain.inventory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InventoryItem Domain Tests")
class InventoryItemTest {

    private InventoryItem inventoryItem;

    @BeforeEach
    void setUp() {
        inventoryItem = new InventoryItem();
        inventoryItem.setNom("Test Product");
        inventoryItem.setCategorie("Electronics");
        inventoryItem.setPrix(99.99);
        inventoryItem.setDescription("Test description");
        inventoryItem.setStockCentral(100);
        inventoryItem.setStockMinimum(10);
    }

    @Test
    @DisplayName("Should create inventory item with valid data")
    void shouldCreateInventoryItemWithValidData() {
        // Given
        String expectedName = "Test Product";
        String expectedCategory = "Electronics";
        Double expectedPrice = 99.99;
        Integer expectedStock = 100;

        // When & Then
        assertEquals(expectedName, inventoryItem.getNom());
        assertEquals(expectedCategory, inventoryItem.getCategorie());
        assertEquals(expectedPrice, inventoryItem.getPrix());
        assertEquals(expectedStock, inventoryItem.getStockCentral());
        assertTrue(inventoryItem.isActive());
    }

    @Test
    @DisplayName("Should indicate when stock is low")
    void shouldIndicateWhenStockIsLow() {
        // Given
        inventoryItem.setStockCentral(5);
        inventoryItem.setStockMinimum(10);

        // When
        boolean needsRestock = inventoryItem.needsRestock();

        // Then
        assertTrue(needsRestock);
    }

    @Test
    @DisplayName("Should indicate when stock is sufficient")
    void shouldIndicateWhenStockIsSufficient() {
        // Given
        inventoryItem.setStockCentral(50);
        inventoryItem.setStockMinimum(10);

        // When
        boolean needsRestock = inventoryItem.needsRestock();

        // Then
        assertFalse(needsRestock);
    }

    @Test
    @DisplayName("Should reduce stock correctly")
    void shouldReduceStockCorrectly() {
        // Given
        int initialStock = 100;
        int quantityToReduce = 25;
        inventoryItem.setStockCentral(initialStock);

        // When
        inventoryItem.reduceStock(quantityToReduce);

        // Then
        assertEquals(initialStock - quantityToReduce, inventoryItem.getStockCentral());
    }

    @Test
    @DisplayName("Should not reduce stock below zero")
    void shouldNotReduceStockBelowZero() {
        // Given
        inventoryItem.setStockCentral(10);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            inventoryItem.reduceStock(15);
        });
    }

    @Test
    @DisplayName("Should increase stock correctly")
    void shouldIncreaseStockCorrectly() {
        // Given
        int initialStock = 50;
        int quantityToAdd = 25;
        inventoryItem.setStockCentral(initialStock);

        // When
        inventoryItem.increaseStock(quantityToAdd);

        // Then
        assertEquals(initialStock + quantityToAdd, inventoryItem.getStockCentral());
    }

    @Test
    @DisplayName("Should not allow negative quantity for stock increase")
    void shouldNotAllowNegativeQuantityForStockIncrease() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            inventoryItem.increaseStock(-5);
        });
    }

    @Test
    @DisplayName("Should deactivate item correctly")
    void shouldDeactivateItemCorrectly() {
        // Given
        assertTrue(inventoryItem.isActive());

        // When
        inventoryItem.deactivate();

        // Then
        assertFalse(inventoryItem.isActive());
    }

    @Test
    @DisplayName("Should activate item correctly")
    void shouldActivateItemCorrectly() {
        // Given
        inventoryItem.deactivate();
        assertFalse(inventoryItem.isActive());

        // When
        inventoryItem.activate();

        // Then
        assertTrue(inventoryItem.isActive());
    }

    @Test
    @DisplayName("Should update last modified date when stock changes")
    void shouldUpdateLastModifiedDateWhenStockChanges() {
        // Given
        LocalDate beforeUpdate = LocalDate.now();

        // When
        inventoryItem.updateStock(75);

        // Then
        assertNotNull(inventoryItem.getDateDerniereMaj());
        assertTrue(inventoryItem.getDateDerniereMaj().isEqual(beforeUpdate) || 
                  inventoryItem.getDateDerniereMaj().isAfter(beforeUpdate));
    }

    @Test
    @DisplayName("Should validate required fields")
    void shouldValidateRequiredFields() {
        // Given
        InventoryItem emptyItem = new InventoryItem();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            emptyItem.validate();
        });
    }

    @Test
    @DisplayName("Should pass validation with valid data")
    void shouldPassValidationWithValidData() {
        // When & Then
        assertDoesNotThrow(() -> {
            inventoryItem.validate();
        });
    }

    @Test
    @DisplayName("Should not allow negative price")
    void shouldNotAllowNegativePrice() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            inventoryItem.setPrix(-10.0);
            inventoryItem.validate();
        });
    }

    @Test
    @DisplayName("Should not allow empty name")
    void shouldNotAllowEmptyName() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            inventoryItem.setNom("");
            inventoryItem.validate();
        });
    }

    @Test
    @DisplayName("Should calculate total value correctly")
    void shouldCalculateTotalValueCorrectly() {
        // Given
        inventoryItem.setPrix(25.50);
        inventoryItem.setStockCentral(10);

        // When
        Double totalValue = inventoryItem.calculateTotalValue();

        // Then
        assertEquals(255.0, totalValue);
    }
}
