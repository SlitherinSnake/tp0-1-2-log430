package com.log430.tp4.presentation.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import com.log430.tp4.application.service.InventoryService;
import com.log430.tp4.domain.inventory.InventoryItem;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = InventoryController.class)
@ContextConfiguration(classes = {InventoryController.class})
@DisplayName("InventoryController Tests")
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("removal")
    @MockBean
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
        inventoryItem1.setActive(true);

        inventoryItem2 = new InventoryItem();
        inventoryItem2.setId(2L);
        inventoryItem2.setNom("Product 2");
        inventoryItem2.setCategorie("Books");
        inventoryItem2.setPrix(19.99);
        inventoryItem2.setStockCentral(25);
        inventoryItem2.setActive(true);
    }

    @Test
    @DisplayName("GET /api/inventory should return all active items")
    void shouldReturnAllActiveItems() throws Exception {
        // Given
        List<InventoryItem> items = Arrays.asList(inventoryItem1, inventoryItem2);
        when(inventoryService.getAllActiveItems()).thenReturn(items);

        // When & Then
        mockMvc.perform(get("/api/inventory"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Product 1"))
                .andExpect(jsonPath("$[0].category").value("Electronics"))
                .andExpect(jsonPath("$[0].price").value(99.99))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Product 2"));

        verify(inventoryService).getAllActiveItems();
    }

    @Test
    @DisplayName("GET /api/inventory/{id} should return item when exists")
    void shouldReturnItemWhenExists() throws Exception {
        // Given
        Long itemId = 1L;
        when(inventoryService.getItemById(itemId)).thenReturn(Optional.of(inventoryItem1));

        // When & Then
        mockMvc.perform(get("/api/inventory/{id}", itemId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Product 1"))
                .andExpect(jsonPath("$.category").value("Electronics"));

        verify(inventoryService).getItemById(itemId);
    }

    @Test
    @DisplayName("GET /api/inventory/{id} should return 404 when item not found")
    void shouldReturn404WhenItemNotFound() throws Exception {
        // Given
        Long itemId = 999L;
        when(inventoryService.getItemById(itemId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/inventory/{id}", itemId))
                .andExpect(status().isNotFound());

        verify(inventoryService).getItemById(itemId);
    }

    @Test
    @DisplayName("POST /api/inventory should create new item")
    void shouldCreateNewItem() throws Exception {
        // Given
        InventoryItem savedItem = new InventoryItem();
        savedItem.setId(3L);
        savedItem.setNom("New Product");
        savedItem.setCategorie("Tools");
        savedItem.setPrix(45.00);
        savedItem.setStockCentral(25);

        when(inventoryService.createItem("New Product", "Tools", 45.00, 25)).thenReturn(savedItem);

        String requestBody = """
            {
                "nom": "New Product",
                "categorie": "Tools",
                "prix": 45.00,
                "stockCentral": 25
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("New Product"))
                .andExpect(jsonPath("$.category").value("Tools"));

        verify(inventoryService).createItem("New Product", "Tools", 45.00, 25);
    }

    @Test
    @DisplayName("PATCH /api/inventory/{id}/stock should update stock")
    void shouldUpdateStock() throws Exception {
        // Given
        Long itemId = 1L;
        Integer newStock = 75;
        
        inventoryItem1.setStockCentral(newStock);
        when(inventoryService.updateStock(itemId, newStock)).thenReturn(inventoryItem1);

        String requestBody = """
            {
                "stock": 75
            }
            """;

        // When & Then
        mockMvc.perform(patch("/api/inventory/{id}/stock", itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.stock").value(75));

        verify(inventoryService).updateStock(itemId, newStock);
    }

    @Test
    @DisplayName("GET /api/inventory/category/{category} should return items by category")
    void shouldReturnItemsByCategory() throws Exception {
        // Given
        String category = "Electronics";
        List<InventoryItem> categoryItems = Arrays.asList(inventoryItem1);
        when(inventoryService.getItemsByCategory(category)).thenReturn(categoryItems);

        // When & Then
        mockMvc.perform(get("/api/inventory/category/{category}", category))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].category").value("Electronics"));

        verify(inventoryService).getItemsByCategory(category);
    }

    @Test
    @DisplayName("GET /api/inventory/search should return search results")
    void shouldReturnSearchResults() throws Exception {
        // Given
        String searchTerm = "Product";
        List<InventoryItem> searchResults = Arrays.asList(inventoryItem1, inventoryItem2);
        when(inventoryService.searchItemsByName(searchTerm)).thenReturn(searchResults);

        // When & Then
        mockMvc.perform(get("/api/inventory/search")
                .param("name", searchTerm))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2));

        verify(inventoryService).searchItemsByName(searchTerm);
    }

    @Test
    @DisplayName("GET /api/inventory/categories should return distinct categories")
    void shouldReturnDistinctCategories() throws Exception {
        // Given
        List<String> categories = Arrays.asList("Electronics", "Books", "Tools");
        when(inventoryService.getDistinctCategories()).thenReturn(categories);

        // When & Then
        mockMvc.perform(get("/api/inventory/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0]").value("Electronics"))
                .andExpect(jsonPath("$[1]").value("Books"))
                .andExpect(jsonPath("$[2]").value("Tools"));

        verify(inventoryService).getDistinctCategories();
    }

    @Test
    @DisplayName("GET /api/inventory/total-value should return total inventory value")
    void shouldReturnTotalInventoryValue() throws Exception {
        // Given
        Double totalValue = 1234.56;
        when(inventoryService.calculateTotalInventoryValue()).thenReturn(totalValue);

        // When & Then
        mockMvc.perform(get("/api/inventory/total-value"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalValue").value(1234.56));

        verify(inventoryService).calculateTotalInventoryValue();
    }

    @Test
    @DisplayName("DELETE /api/inventory/{id} should deactivate item")
    void shouldDeactivateItem() throws Exception {
        // Given
        Long itemId = 1L;
        doNothing().when(inventoryService).deactivateItem(itemId);

        // When & Then
        mockMvc.perform(delete("/api/inventory/{id}", itemId))
                .andExpect(status().isNoContent());

        verify(inventoryService).deactivateItem(itemId);
    }

    @Test
    @DisplayName("GET /api/inventory/test should return test response")
    void shouldReturnTestResponse() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/inventory/test"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("API is working"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("GET /api/inventory/count should return item count")
    void shouldReturnItemCount() throws Exception {
        // Given
        List<InventoryItem> items = Arrays.asList(inventoryItem1, inventoryItem2);
        when(inventoryService.getAllActiveItems()).thenReturn(items);

        // When & Then
        mockMvc.perform(get("/api/inventory/count"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(2));

        verify(inventoryService).getAllActiveItems();
    }

    @Test
    @DisplayName("Should handle service exceptions gracefully")
    void shouldHandleServiceExceptionsGracefully() throws Exception {
        // Given
        Long itemId = 1L;
        when(inventoryService.getItemById(itemId)).thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(get("/api/inventory/{id}", itemId))
                .andExpect(status().isInternalServerError());

        verify(inventoryService).getItemById(itemId);
    }
}
