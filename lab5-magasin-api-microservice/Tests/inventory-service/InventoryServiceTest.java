package com.log430.tp5.inventory.service;

import com.log430.tp5.application.service.InventoryService;
import com.log430.tp5.domain.inventory.InventoryItem;
import com.log430.tp5.infrastructure.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private InventoryItem testItem;

    @BeforeEach
    void setUp() {
        testItem = new InventoryItem();
        testItem.setId(1L);
        testItem.setNom("Test Product");
        testItem.setCategorie("Electronics");
        testItem.setPrix(99.99);
        testItem.setStockCentral(100);
        testItem.setDescription("Test description");
        testItem.setActif(true);
    }

    @Test
    void getAllActiveItems_ShouldReturnOnlyActiveItems() {
        // Given
        List<InventoryItem> activeItems = Arrays.asList(testItem);
        when(inventoryRepository.findByActifTrue()).thenReturn(activeItems);

        // When
        List<InventoryItem> result = inventoryService.getAllActiveItems();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNom()).isEqualTo("Test Product");
        assertThat(result.get(0).getActif()).isTrue();
        verify(inventoryRepository).findByActifTrue();
    }

    @Test
    void getItemById_WhenItemExists_ShouldReturnItem() {
        // Given
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testItem));

        // When
        Optional<InventoryItem> result = inventoryService.getItemById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getNom()).isEqualTo("Test Product");
        verify(inventoryRepository).findById(1L);
    }

    @Test
    void getItemById_WhenItemNotExists_ShouldReturnEmpty() {
        // Given
        when(inventoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<InventoryItem> result = inventoryService.getItemById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(inventoryRepository).findById(999L);
    }

    @Test
    void createItem_WithValidData_ShouldCreateAndReturnItem() {
        // Given
        when(inventoryRepository.save(any(InventoryItem.class))).thenReturn(testItem);

        // When
        InventoryItem result = inventoryService.createItem("Test Product", "Electronics", 99.99, 100);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNom()).isEqualTo("Test Product");
        verify(inventoryRepository).save(any(InventoryItem.class));
    }

    @Test
    void createItem_WithNullName_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> inventoryService.createItem(null, "Electronics", 99.99, 100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nom");
    }

    @Test
    void createItem_WithEmptyName_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> inventoryService.createItem("", "Electronics", 99.99, 100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nom");
    }

    @Test
    void createItem_WithNegativePrice_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> inventoryService.createItem("Test", "Electronics", -10.0, 100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("prix");
    }

    @Test
    void createItem_WithNegativeStock_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> inventoryService.createItem("Test", "Electronics", 99.99, -10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("stock");
    }

    @Test
    void updateItem_WhenItemExists_ShouldUpdateAndReturnItem() {
        // Given
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(inventoryRepository.save(any(InventoryItem.class))).thenReturn(testItem);

        // When
        InventoryItem result = inventoryService.updateItem(1L, "Updated Product", "Electronics", 149.99, "Updated description");

        // Then
        assertThat(result).isNotNull();
        verify(inventoryRepository).findById(1L);
        verify(inventoryRepository).save(testItem);
    }

    @Test
    void updateItem_WhenItemNotExists_ShouldThrowException() {
        // Given
        when(inventoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> inventoryService.updateItem(999L, "Updated", "Electronics", 149.99, "Updated"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void updateStock_WhenItemExists_ShouldUpdateStock() {
        // Given
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(inventoryRepository.save(any(InventoryItem.class))).thenReturn(testItem);

        // When
        InventoryItem result = inventoryService.updateStock(1L, 150);

        // Then
        assertThat(result).isNotNull();
        verify(inventoryRepository).findById(1L);
        verify(inventoryRepository).save(testItem);
    }

    @Test
    void updateStock_WithNegativeStock_ShouldThrowException() {
        // Given
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testItem));

        // When & Then
        assertThatThrownBy(() -> inventoryService.updateStock(1L, -10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("stock");
    }

    @Test
    void increaseStock_WhenItemExists_ShouldIncreaseStock() {
        // Given
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(inventoryRepository.save(any(InventoryItem.class))).thenReturn(testItem);

        // When
        InventoryItem result = inventoryService.increaseStock(1L, 50);

        // Then
        assertThat(result).isNotNull();
        verify(inventoryRepository).findById(1L);
        verify(inventoryRepository).save(testItem);
    }

    @Test
    void decreaseStock_WhenSufficientStock_ShouldDecreaseStock() {
        // Given
        testItem.setStockCentral(100);
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(inventoryRepository.save(any(InventoryItem.class))).thenReturn(testItem);

        // When
        InventoryItem result = inventoryService.decreaseStock(1L, 25);

        // Then
        assertThat(result).isNotNull();
        verify(inventoryRepository).findById(1L);
        verify(inventoryRepository).save(testItem);
    }

    @Test
    void decreaseStock_WhenInsufficientStock_ShouldThrowException() {
        // Given
        testItem.setStockCentral(10);
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testItem));

        // When & Then
        assertThatThrownBy(() -> inventoryService.decreaseStock(1L, 50))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("stock");
    }

    @Test
    void getItemsNeedingRestock_ShouldReturnLowStockItems() {
        // Given
        InventoryItem lowStockItem = new InventoryItem();
        lowStockItem.setStockCentral(5);
        List<InventoryItem> lowStockItems = Arrays.asList(lowStockItem);
        when(inventoryRepository.findByStockCentralLessThanAndActifTrue(anyInt())).thenReturn(lowStockItems);

        // When
        List<InventoryItem> result = inventoryService.getItemsNeedingRestock();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStockCentral()).isLessThan(10);
        verify(inventoryRepository).findByStockCentralLessThanAndActifTrue(anyInt());
    }

    @Test
    void getItemsByCategory_ShouldReturnItemsInCategory() {
        // Given
        List<InventoryItem> categoryItems = Arrays.asList(testItem);
        when(inventoryRepository.findByCategorieAndActifTrue("Electronics")).thenReturn(categoryItems);

        // When
        List<InventoryItem> result = inventoryService.getItemsByCategory("Electronics");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategorie()).isEqualTo("Electronics");
        verify(inventoryRepository).findByCategorieAndActifTrue("Electronics");
    }

    @Test
    void searchItemsByName_ShouldReturnMatchingItems() {
        // Given
        List<InventoryItem> searchResults = Arrays.asList(testItem);
        when(inventoryRepository.findByNomContainingIgnoreCaseAndActifTrue("Test")).thenReturn(searchResults);

        // When
        List<InventoryItem> result = inventoryService.searchItemsByName("Test");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNom()).containsIgnoringCase("Test");
        verify(inventoryRepository).findByNomContainingIgnoreCaseAndActifTrue("Test");
    }

    @Test
    void getDistinctCategories_ShouldReturnUniqueCategories() {
        // Given
        List<String> categories = Arrays.asList("Electronics", "Clothing", "Books");
        when(inventoryRepository.findDistinctCategories()).thenReturn(categories);

        // When
        List<String> result = inventoryService.getDistinctCategories();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).contains("Electronics", "Clothing", "Books");
        verify(inventoryRepository).findDistinctCategories();
    }

    @Test
    void calculateTotalInventoryValue_ShouldReturnCorrectValue() {
        // Given
        List<InventoryItem> items = Arrays.asList(testItem);
        when(inventoryRepository.findByActifTrue()).thenReturn(items);

        // When
        Double result = inventoryService.calculateTotalInventoryValue();

        // Then
        assertThat(result).isEqualTo(9999.0); // 99.99 * 100
        verify(inventoryRepository).findByActifTrue();
    }

    @Test
    void deactivateItem_WhenItemExists_ShouldDeactivateItem() {
        // Given
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(inventoryRepository.save(any(InventoryItem.class))).thenReturn(testItem);

        // When
        inventoryService.deactivateItem(1L);

        // Then
        verify(inventoryRepository).findById(1L);
        verify(inventoryRepository).save(testItem);
    }

    @Test
    void deactivateItem_WhenItemNotExists_ShouldThrowException() {
        // Given
        when(inventoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> inventoryService.deactivateItem(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }
}