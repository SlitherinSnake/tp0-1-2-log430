package com.log430.tp4.presentation.api.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.log430.tp4.domain.inventory.InventoryItem;

@DisplayName("InventoryItemDto Tests")
class InventoryItemDtoTest {

    private InventoryItem inventoryItem;

    @BeforeEach
    void setUp() {
        inventoryItem = new InventoryItem();
        inventoryItem.setId(1L);
        inventoryItem.setNom("Test Product");
        inventoryItem.setDescription("Test description");
        inventoryItem.setCategorie("Electronics");
        inventoryItem.setPrix(99.99);
        inventoryItem.setStockCentral(50);
        inventoryItem.setActive(true);
    }

    @Test
    @DisplayName("Should create DTO from entity correctly")
    void shouldCreateDtoFromEntityCorrectly() {
        // When
        InventoryItemDto dto = new InventoryItemDto(inventoryItem);

        // Then
        assertEquals(inventoryItem.getId(), dto.getId());
        assertEquals(inventoryItem.getNom(), dto.getName());
        assertEquals(inventoryItem.getDescription(), dto.getDescription());
        assertEquals(inventoryItem.getCategorie(), dto.getCategory());
        assertEquals(inventoryItem.getPrix(), dto.getPrice());
        assertEquals(inventoryItem.getStockCentral(), dto.getStock());
        assertEquals("active", dto.getStatus());
        assertEquals(inventoryItem.getId(), dto.getCreated()); // ID used as proxy for creation order
    }

    @Test
    @DisplayName("Should create DTO using static factory method")
    void shouldCreateDtoUsingStaticFactoryMethod() {
        // When
        InventoryItemDto dto = InventoryItemDto.fromEntity(inventoryItem);

        // Then
        assertEquals(inventoryItem.getId(), dto.getId());
        assertEquals(inventoryItem.getNom(), dto.getName());
        assertEquals(inventoryItem.getCategorie(), dto.getCategory());
    }

    @Test
    @DisplayName("Should handle inactive inventory item")
    void shouldHandleInactiveInventoryItem() {
        // Given
        inventoryItem.setActive(false);

        // When
        InventoryItemDto dto = new InventoryItemDto(inventoryItem);

        // Then
        assertEquals("inactive", dto.getStatus());
    }

    @Test
    @DisplayName("Should throw exception when inventory item is null")
    void shouldThrowExceptionWhenInventoryItemIsNull() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            new InventoryItemDto(null);
        });
    }

    @Test
    @DisplayName("Should use default constructor correctly")
    void shouldUseDefaultConstructorCorrectly() {
        // When
        InventoryItemDto dto = new InventoryItemDto();

        // Then
        assertNull(dto.getId());
        assertNull(dto.getName());
        assertNull(dto.getDescription());
        assertNull(dto.getCategory());
        assertNull(dto.getBrand()); // Default null brand
        assertNull(dto.getPrice());
        assertNull(dto.getStock());
        assertNull(dto.getStatus());
        assertNull(dto.getCreated());
    }

    @Test
    @DisplayName("Should set and get all properties correctly")
    void shouldSetAndGetAllPropertiesCorrectly() {
        // Given
        InventoryItemDto dto = new InventoryItemDto();

        // When
        dto.setId(2L);
        dto.setName("New Product");
        dto.setDescription("New description");
        dto.setCategory("Books");
        dto.setBrand("Test Brand");
        dto.setPrice(25.50);
        dto.setStock(30);
        dto.setStatus("active");
        dto.setCreated(System.currentTimeMillis());

        // Then
        assertEquals(2L, dto.getId());
        assertEquals("New Product", dto.getName());
        assertEquals("New description", dto.getDescription());
        assertEquals("Books", dto.getCategory());
        assertEquals("Test Brand", dto.getBrand());
        assertEquals(25.50, dto.getPrice());
        assertEquals(30, dto.getStock());
        assertEquals("active", dto.getStatus());
        assertNotNull(dto.getCreated());
    }

    @Test
    @DisplayName("Should handle null values in entity")
    void shouldHandleNullValuesInEntity() {
        // Given
        InventoryItem itemWithNulls = new InventoryItem();
        itemWithNulls.setId(1L);
        itemWithNulls.setNom("Product");
        itemWithNulls.setCategorie("Category");
        itemWithNulls.setPrix(10.0);
        itemWithNulls.setStockCentral(5);
        itemWithNulls.setActive(true);
        // Description is null

        // When
        InventoryItemDto dto = new InventoryItemDto(itemWithNulls);

        // Then
        assertEquals(1L, dto.getId());
        assertEquals("Product", dto.getName());
        assertNull(dto.getDescription());
        assertEquals("Category", dto.getCategory());
        assertEquals(10.0, dto.getPrice());
        assertEquals(5, dto.getStock());
        assertEquals("active", dto.getStatus());
    }

    @Test
    @DisplayName("Should maintain data integrity after DTO conversion")
    void shouldMaintainDataIntegrityAfterDtoConversion() {
        // Given
        Double expectedPrice = 123.45;
        Integer expectedStock = 42;
        String expectedName = "Specific Product Name";

        inventoryItem.setPrix(expectedPrice);
        inventoryItem.setStockCentral(expectedStock);
        inventoryItem.setNom(expectedName);

        // When
        InventoryItemDto dto = InventoryItemDto.fromEntity(inventoryItem);

        // Then
        assertEquals(expectedPrice, dto.getPrice());
        assertEquals(expectedStock, dto.getStock());
        assertEquals(expectedName, dto.getName());
    }
}
