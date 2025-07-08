package com.log430.tp4.application.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.log430.tp4.domain.inventory.InventoryItem;
import com.log430.tp4.infrastructure.repository.InventoryItemRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryService Tests")
class InventoryServiceTest {

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private InventoryItem inventoryItem1;
    private InventoryItem inventoryItem2;

    @BeforeEach
    void setUp() {
        inventoryItem1 = new InventoryItem();
        inventoryItem1.setId(1L);
        inventoryItem1.setNom("Product 1");
        inventoryItem1.setCategorie("Electronics");
        inventoryItem1.setPrix(99.99);
        inventoryItem1.setStockCentral(50);
        inventoryItem1.setStockMinimum(10);
        inventoryItem1.setActive(true);

        inventoryItem2 = new InventoryItem();
        inventoryItem2.setId(2L);
        inventoryItem2.setNom("Product 2");
        inventoryItem2.setCategorie("Books");
        inventoryItem2.setPrix(19.99);
        inventoryItem2.setStockCentral(5);
        inventoryItem2.setStockMinimum(10);
        inventoryItem2.setActive(true);
    }

    @Test
    @DisplayName("Should return all active items")
    void shouldReturnAllActiveItems() {
        // Given
        List<InventoryItem> expectedItems = Arrays.asList(inventoryItem1, inventoryItem2);
        when(inventoryItemRepository.findByIsActiveTrue()).thenReturn(expectedItems);

        // When
        List<InventoryItem> actualItems = inventoryService.getAllActiveItems();

        // Then
        assertEquals(2, actualItems.size());
        assertEquals(expectedItems, actualItems);
        verify(inventoryItemRepository).findByIsActiveTrue();
    }

    @Test
    @DisplayName("Should return item by ID when exists")
    void shouldReturnItemByIdWhenExists() {
        // Given
        Long itemId = 1L;
        when(inventoryItemRepository.findById(itemId)).thenReturn(Optional.of(inventoryItem1));

        // When
        Optional<InventoryItem> result = inventoryService.getItemById(itemId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(inventoryItem1, result.get());
        verify(inventoryItemRepository).findById(itemId);
    }

    @Test
    @DisplayName("Should return empty when item not found")
    void shouldReturnEmptyWhenItemNotFound() {
        // Given
        Long itemId = 999L;
        when(inventoryItemRepository.findById(itemId)).thenReturn(Optional.empty());

        // When
        Optional<InventoryItem> result = inventoryService.getItemById(itemId);

        // Then
        assertFalse(result.isPresent());
        verify(inventoryItemRepository).findById(itemId);
    }

    @Test
    @DisplayName("Should save new inventory item")
    void shouldSaveNewInventoryItem() {
        // Given
        String name = "New Product";
        String category = "Tools";
        Double price = 45.00;
        Integer stock = 25;

        InventoryItem savedItem = new InventoryItem();
        savedItem.setId(3L);
        savedItem.setNom(name);
        savedItem.setCategorie(category);
        savedItem.setPrix(price);
        savedItem.setStockCentral(stock);

        when(inventoryItemRepository.save(any(InventoryItem.class))).thenReturn(savedItem);

        // When
        InventoryItem result = inventoryService.createItem(name, category, price, stock);

        // Then
        assertEquals(savedItem, result);
        verify(inventoryItemRepository).save(any(InventoryItem.class));
    }

    @Test
    @DisplayName("Should update stock correctly")
    void shouldUpdateStockCorrectly() {
        // Given
        Long itemId = 1L;
        Integer newStock = 75;
        inventoryItem1.setStockCentral(50);
        
        when(inventoryItemRepository.findById(itemId)).thenReturn(Optional.of(inventoryItem1));
        when(inventoryItemRepository.save(any(InventoryItem.class))).thenReturn(inventoryItem1);

        // When
        InventoryItem updatedItem = inventoryService.updateStock(itemId, newStock);

        // Then
        assertEquals(newStock, updatedItem.getStockCentral());
        verify(inventoryItemRepository).findById(itemId);
        verify(inventoryItemRepository).save(inventoryItem1);
    }

    @Test
    @DisplayName("Should throw exception when updating stock of non-existent item")
    void shouldThrowExceptionWhenUpdatingStockOfNonExistentItem() {
        // Given
        Long itemId = 999L;
        Integer newStock = 75;
        when(inventoryItemRepository.findById(itemId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            inventoryService.updateStock(itemId, newStock);
        });
        verify(inventoryItemRepository).findById(itemId);
        verify(inventoryItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return items needing restock")
    void shouldReturnItemsNeedingRestock() {
        // Given
        List<InventoryItem> lowStockItems = Arrays.asList(inventoryItem2); // item2 has stock 5, minimum 10
        when(inventoryItemRepository.findItemsNeedingRestock()).thenReturn(lowStockItems);

        // When
        List<InventoryItem> result = inventoryService.getItemsNeedingRestock();

        // Then
        assertEquals(1, result.size());
        assertEquals(inventoryItem2, result.get(0));
        verify(inventoryItemRepository).findItemsNeedingRestock();
    }

    @Test
    @DisplayName("Should search items by name")
    void shouldSearchItemsByName() {
        // Given
        String searchTerm = "Product";
        List<InventoryItem> searchResults = Arrays.asList(inventoryItem1, inventoryItem2);
        when(inventoryItemRepository.findByNomContainingIgnoreCaseAndIsActiveTrue(searchTerm)).thenReturn(searchResults);

        // When
        List<InventoryItem> result = inventoryService.searchItemsByName(searchTerm);

        // Then
        assertEquals(2, result.size());
        assertEquals(searchResults, result);
        verify(inventoryItemRepository).findByNomContainingIgnoreCaseAndIsActiveTrue(searchTerm);
    }

    @Test
    @DisplayName("Should return items by category")
    void shouldReturnItemsByCategory() {
        // Given
        String category = "Electronics";
        List<InventoryItem> categoryItems = Arrays.asList(inventoryItem1);
        when(inventoryItemRepository.findByCategorieAndIsActiveTrue(category)).thenReturn(categoryItems);

        // When
        List<InventoryItem> result = inventoryService.getItemsByCategory(category);

        // Then
        assertEquals(1, result.size());
        assertEquals(inventoryItem1, result.get(0));
        verify(inventoryItemRepository).findByCategorieAndIsActiveTrue(category);
    }

    @Test
    @DisplayName("Should return distinct categories")
    void shouldReturnDistinctCategories() {
        // Given
        List<String> categories = Arrays.asList("Electronics", "Books", "Tools");
        when(inventoryItemRepository.findDistinctCategories()).thenReturn(categories);

        // When
        List<String> result = inventoryService.getDistinctCategories();

        // Then
        assertEquals(3, result.size());
        assertTrue(result.containsAll(categories));
        verify(inventoryItemRepository).findDistinctCategories();
    }

    @Test
    @DisplayName("Should calculate total inventory value")
    void shouldCalculateTotalInventoryValue() {
        // Given
        double expectedTotalValue = 1099.85; // (99.99 * 50) + (19.99 * 5)
        when(inventoryItemRepository.calculateTotalInventoryValue()).thenReturn(expectedTotalValue);

        // When
        Double result = inventoryService.calculateTotalInventoryValue();

        // Then
        assertEquals(expectedTotalValue, result);
        verify(inventoryItemRepository).calculateTotalInventoryValue();
    }

    @Test
    @DisplayName("Should deactivate item successfully")
    void shouldDeactivateItemSuccessfully() {
        // Given
        Long itemId = 1L;
        when(inventoryItemRepository.findById(itemId)).thenReturn(Optional.of(inventoryItem1));
        when(inventoryItemRepository.save(any(InventoryItem.class))).thenReturn(inventoryItem1);

        // When
        inventoryService.deactivateItem(itemId);

        // Then
        assertFalse(inventoryItem1.isActive());
        verify(inventoryItemRepository).findById(itemId);
        verify(inventoryItemRepository).save(inventoryItem1);
    }

    @Test
    @DisplayName("Should throw exception when deactivating non-existent item")
    void shouldThrowExceptionWhenDeactivatingNonExistentItem() {
        // Given
        Long itemId = 999L;
        when(inventoryItemRepository.findById(itemId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            inventoryService.deactivateItem(itemId);
        });
        verify(inventoryItemRepository).findById(itemId);
        verify(inventoryItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reduce stock when sufficient quantity available")
    void shouldReduceStockWhenSufficientQuantityAvailable() {
        // Given
        Long itemId = 1L;
        Integer quantityToReduce = 20;
        inventoryItem1.setStockCentral(50);
        
        when(inventoryItemRepository.findById(itemId)).thenReturn(Optional.of(inventoryItem1));
        when(inventoryItemRepository.save(any(InventoryItem.class))).thenReturn(inventoryItem1);

        // When
        inventoryService.reduceStock(itemId, quantityToReduce);

        // Then
        assertEquals(30, inventoryItem1.getStockCentral());
        verify(inventoryItemRepository).findById(itemId);
        verify(inventoryItemRepository).save(inventoryItem1);
    }

    @Test
    @DisplayName("Should throw exception when trying to reduce more stock than available")
    void shouldThrowExceptionWhenTryingToReduceMoreStockThanAvailable() {
        // Given
        Long itemId = 1L;
        Integer quantityToReduce = 60; // More than available (50)
        inventoryItem1.setStockCentral(50);
        
        when(inventoryItemRepository.findById(itemId)).thenReturn(Optional.of(inventoryItem1));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.reduceStock(itemId, quantityToReduce);
        });
        verify(inventoryItemRepository).findById(itemId);
        verify(inventoryItemRepository, never()).save(any());
    }
}
