package com.log430.tp7.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.log430.tp7.application.service.InventoryService;
import com.log430.tp7.domain.inventory.InventoryItem;
import com.log430.tp7.presentation.api.InventoryController;
import com.log430.tp7.presentation.api.dto.InventoryItemDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private InventoryController inventoryController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private InventoryItem testItem;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(inventoryController).build();
        objectMapper = new ObjectMapper();
        
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
    void getAllItems_ShouldReturnAllActiveItems() throws Exception {
        // Given
        List<InventoryItem> items = Arrays.asList(testItem);
        when(inventoryService.getAllActiveItems()).thenReturn(items);

        // When & Then
        mockMvc.perform(get("/api/inventory"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nom").value("Test Product"));

        verify(inventoryService).getAllActiveItems();
    }

    @Test
    void getItemById_WhenItemExists_ShouldReturnItem() throws Exception {
        // Given
        when(inventoryService.getItemById(1L)).thenReturn(Optional.of(testItem));

        // When & Then
        mockMvc.perform(get("/api/inventory/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nom").value("Test Product"));

        verify(inventoryService).getItemById(1L);
    }

    @Test
    void getItemById_WhenItemNotExists_ShouldReturn404() throws Exception {
        // Given
        when(inventoryService.getItemById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/inventory/999"))
                .andExpect(status().isNotFound());

        verify(inventoryService).getItemById(999L);
    }

    @Test
    void createItem_WithValidData_ShouldCreateItem() throws Exception {
        // Given
        InventoryController.CreateItemRequest request = new InventoryController.CreateItemRequest(
                "New Product", "Electronics", 149.99, 50
        );
        when(inventoryService.createItem(anyString(), anyString(), anyDouble(), anyInt()))
                .thenReturn(testItem);

        // When & Then
        mockMvc.perform(post("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nom").value("Test Product"));

        verify(inventoryService).createItem("New Product", "Electronics", 149.99, 50);
    }

    @Test
    void updateItem_WithValidData_ShouldUpdateItem() throws Exception {
        // Given
        InventoryController.UpdateItemRequest request = new InventoryController.UpdateItemRequest(
                "Updated Product", "Electronics", 199.99, "Updated description"
        );
        when(inventoryService.updateItem(eq(1L), anyString(), anyString(), anyDouble(), anyString()))
                .thenReturn(testItem);

        // When & Then
        mockMvc.perform(put("/api/inventory/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Test Product"));

        verify(inventoryService).updateItem(1L, "Updated Product", "Electronics", 199.99, "Updated description");
    }

    @Test
    void updateStock_WithValidData_ShouldUpdateStock() throws Exception {
        // Given
        Map<String, Integer> request = Map.of("stock", 150);
        when(inventoryService.updateStock(1L, 150)).thenReturn(testItem);

        // When & Then
        mockMvc.perform(patch("/api/inventory/1/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Test Product"));

        verify(inventoryService).updateStock(1L, 150);
    }

    @Test
    void increaseStock_WithValidQuantity_ShouldIncreaseStock() throws Exception {
        // Given
        Map<String, Integer> request = Map.of("quantity", 50);
        when(inventoryService.increaseStock(1L, 50)).thenReturn(testItem);

        // When & Then
        mockMvc.perform(patch("/api/inventory/1/stock/increase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Test Product"));

        verify(inventoryService).increaseStock(1L, 50);
    }

    @Test
    void decreaseStock_WithValidQuantity_ShouldDecreaseStock() throws Exception {
        // Given
        Map<String, Integer> request = Map.of("quantity", 25);
        when(inventoryService.decreaseStock(1L, 25)).thenReturn(testItem);

        // When & Then
        mockMvc.perform(patch("/api/inventory/1/stock/decrease")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Test Product"));

        verify(inventoryService).decreaseStock(1L, 25);
    }

    @Test
    void getItemsNeedingRestock_ShouldReturnLowStockItems() throws Exception {
        // Given
        List<InventoryItem> lowStockItems = Arrays.asList(testItem);
        when(inventoryService.getItemsNeedingRestock()).thenReturn(lowStockItems);

        // When & Then
        mockMvc.perform(get("/api/inventory/restock-needed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(inventoryService).getItemsNeedingRestock();
    }

    @Test
    void getItemsByCategory_ShouldReturnItemsInCategory() throws Exception {
        // Given
        List<InventoryItem> categoryItems = Arrays.asList(testItem);
        when(inventoryService.getItemsByCategory("Electronics")).thenReturn(categoryItems);

        // When & Then
        mockMvc.perform(get("/api/inventory/category/Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].categorie").value("Electronics"));

        verify(inventoryService).getItemsByCategory("Electronics");
    }

    @Test
    void searchItems_ShouldReturnMatchingItems() throws Exception {
        // Given
        List<InventoryItem> searchResults = Arrays.asList(testItem);
        when(inventoryService.searchItemsByName("Test")).thenReturn(searchResults);

        // When & Then
        mockMvc.perform(get("/api/inventory/search?name=Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].nom").value("Test Product"));

        verify(inventoryService).searchItemsByName("Test");
    }

    @Test
    void getCategories_ShouldReturnDistinctCategories() throws Exception {
        // Given
        List<String> categories = Arrays.asList("Electronics", "Clothing", "Books");
        when(inventoryService.getDistinctCategories()).thenReturn(categories);

        // When & Then
        mockMvc.perform(get("/api/inventory/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").value(categories));

        verify(inventoryService).getDistinctCategories();
    }

    @Test
    void getTotalInventoryValue_ShouldReturnTotalValue() throws Exception {
        // Given
        when(inventoryService.calculateTotalInventoryValue()).thenReturn(9999.99);

        // When & Then
        mockMvc.perform(get("/api/inventory/total-value"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalValue").value(9999.99));

        verify(inventoryService).calculateTotalInventoryValue();
    }

    @Test
    void deactivateItem_WithValidId_ShouldDeactivateItem() throws Exception {
        // Given
        doNothing().when(inventoryService).deactivateItem(1L);

        // When & Then
        mockMvc.perform(delete("/api/inventory/1"))
                .andExpect(status().isNoContent());

        verify(inventoryService).deactivateItem(1L);
    }

    @Test
    void deactivateItem_WithInvalidId_ShouldReturn404() throws Exception {
        // Given
        doThrow(new IllegalArgumentException("Item not found"))
                .when(inventoryService).deactivateItem(999L);

        // When & Then
        mockMvc.perform(delete("/api/inventory/999"))
                .andExpect(status().isNotFound());

        verify(inventoryService).deactivateItem(999L);
    }

    @Test
    void testEndpoint_ShouldReturnOkStatus() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/inventory/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("Inventory API is working"));
    }
}