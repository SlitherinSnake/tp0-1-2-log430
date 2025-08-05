package com.log430.tp6.store.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.log430.tp6.application.service.StoreService;
import com.log430.tp6.domain.store.Store;
import com.log430.tp6.presentation.api.StoreController;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class StoreControllerTest {

    @Mock
    private StoreService storeService;

    @InjectMocks
    private StoreController storeController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Store testStore;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(storeController).build();
        objectMapper = new ObjectMapper();
        
        testStore = new Store();
        testStore.setId(1L);
        testStore.setNom("Test Store");
        testStore.setQuartier("Downtown");
        testStore.setAdresse("123 Test Street");
        testStore.setTelephone("555-0123");
        testStore.setActif(true);
    }

    @Test
    void getAllActiveStores_ShouldReturnAllActiveStores() throws Exception {
        // Given
        List<Store> stores = Arrays.asList(testStore);
        when(storeService.getAllActiveStores()).thenReturn(stores);

        // When & Then
        mockMvc.perform(get("/api/stores"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nom").value("Test Store"));

        verify(storeService).getAllActiveStores();
    }

    @Test
    void getStoreById_WhenStoreExists_ShouldReturnStore() throws Exception {
        // Given
        when(storeService.getStoreById(1L)).thenReturn(Optional.of(testStore));

        // When & Then
        mockMvc.perform(get("/api/stores/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nom").value("Test Store"));

        verify(storeService).getStoreById(1L);
    }

    @Test
    void getStoreById_WhenStoreNotExists_ShouldReturn404() throws Exception {
        // Given
        when(storeService.getStoreById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/stores/999"))
                .andExpect(status().isNotFound());

        verify(storeService).getStoreById(999L);
    }

    @Test
    void createStore_WithValidData_ShouldCreateStore() throws Exception {
        // Given
        StoreController.CreateStoreRequest request = new StoreController.CreateStoreRequest(
                "New Store", "Uptown", "456 New Street", "555-0456"
        );
        when(storeService.createStore("New Store", "Uptown", "456 New Street", "555-0456"))
                .thenReturn(testStore);

        // When & Then
        mockMvc.perform(post("/api/stores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nom").value("Test Store"));

        verify(storeService).createStore("New Store", "Uptown", "456 New Street", "555-0456");
    }

    @Test
    void createStore_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given
        StoreController.CreateStoreRequest request = new StoreController.CreateStoreRequest(
                "", "Uptown", "456 New Street", "555-0456"
        );
        when(storeService.createStore("", "Uptown", "456 New Street", "555-0456"))
                .thenThrow(new IllegalArgumentException("Invalid store name"));

        // When & Then
        mockMvc.perform(post("/api/stores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(storeService).createStore("", "Uptown", "456 New Street", "555-0456");
    }

    @Test
    void updateStore_WithValidData_ShouldUpdateStore() throws Exception {
        // Given
        StoreController.UpdateStoreRequest request = new StoreController.UpdateStoreRequest(
                "Updated Store", "Downtown", "123 Updated Street", "555-9999"
        );
        when(storeService.updateStore(1L, "Updated Store", "Downtown", "123 Updated Street", "555-9999"))
                .thenReturn(testStore);

        // When & Then
        mockMvc.perform(put("/api/stores/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Test Store"));

        verify(storeService).updateStore(1L, "Updated Store", "Downtown", "123 Updated Street", "555-9999");
    }

    @Test
    void updateStore_WithInvalidId_ShouldReturn404() throws Exception {
        // Given
        StoreController.UpdateStoreRequest request = new StoreController.UpdateStoreRequest(
                "Updated Store", "Downtown", "123 Updated Street", "555-9999"
        );
        when(storeService.updateStore(999L, "Updated Store", "Downtown", "123 Updated Street", "555-9999"))
                .thenThrow(new IllegalArgumentException("Store not found"));

        // When & Then
        mockMvc.perform(put("/api/stores/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(storeService).updateStore(999L, "Updated Store", "Downtown", "123 Updated Street", "555-9999");
    }

    @Test
    void findStoreByName_WhenStoreExists_ShouldReturnStore() throws Exception {
        // Given
        when(storeService.findStoreByName("Test Store")).thenReturn(Optional.of(testStore));

        // When & Then
        mockMvc.perform(get("/api/stores/by-name?nom=Test Store"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Test Store"));

        verify(storeService).findStoreByName("Test Store");
    }

    @Test
    void findStoreByName_WhenStoreNotExists_ShouldReturn404() throws Exception {
        // Given
        when(storeService.findStoreByName("Nonexistent Store")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/stores/by-name?nom=Nonexistent Store"))
                .andExpect(status().isNotFound());

        verify(storeService).findStoreByName("Nonexistent Store");
    }

    @Test
    void findStoresByQuartier_ShouldReturnStoresInQuartier() throws Exception {
        // Given
        List<Store> stores = Arrays.asList(testStore);
        when(storeService.findStoresByQuartier("Downtown")).thenReturn(stores);

        // When & Then
        mockMvc.perform(get("/api/stores/by-quartier?quartier=Downtown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].quartier").value("Downtown"));

        verify(storeService).findStoresByQuartier("Downtown");
    }

    @Test
    void searchStoresByName_ShouldReturnMatchingStores() throws Exception {
        // Given
        List<Store> stores = Arrays.asList(testStore);
        when(storeService.searchStoresByName("Test")).thenReturn(stores);

        // When & Then
        mockMvc.perform(get("/api/stores/search?nom=Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].nom").value("Test Store"));

        verify(storeService).searchStoresByName("Test");
    }

    @Test
    void deactivateStore_WithValidId_ShouldDeactivateStore() throws Exception {
        // Given
        doNothing().when(storeService).deactivateStore(1L);

        // When & Then
        mockMvc.perform(delete("/api/stores/1"))
                .andExpect(status().isNoContent());

        verify(storeService).deactivateStore(1L);
    }

    @Test
    void deactivateStore_WithInvalidId_ShouldReturn404() throws Exception {
        // Given
        doThrow(new IllegalArgumentException("Store not found"))
                .when(storeService).deactivateStore(999L);

        // When & Then
        mockMvc.perform(delete("/api/stores/999"))
                .andExpect(status().isNotFound());

        verify(storeService).deactivateStore(999L);
    }

    @Test
    void activateStore_WithValidId_ShouldActivateStore() throws Exception {
        // Given
        doNothing().when(storeService).activateStore(1L);

        // When & Then
        mockMvc.perform(patch("/api/stores/1/activate"))
                .andExpect(status().isNoContent());

        verify(storeService).activateStore(1L);
    }

    @Test
    void getAllStores_ShouldReturnAllStores() throws Exception {
        // Given
        List<Store> stores = Arrays.asList(testStore);
        when(storeService.getAllStores()).thenReturn(stores);

        // When & Then
        mockMvc.perform(get("/api/stores/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].nom").value("Test Store"));

        verify(storeService).getAllStores();
    }

    @Test
    void isStoreActive_ShouldReturnActiveStatus() throws Exception {
        // Given
        when(storeService.isStoreActive(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/stores/1/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        verify(storeService).isStoreActive(1L);
    }

    @Test
    void getStoreStats_ShouldReturnStatistics() throws Exception {
        // Given
        when(storeService.getStoreCount()).thenReturn(5L);
        when(storeService.getActiveStoreCount()).thenReturn(4L);

        // When & Then
        mockMvc.perform(get("/api/stores/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStores").value(5))
                .andExpect(jsonPath("$.activeStores").value(4))
                .andExpect(jsonPath("$.inactiveStores").value(1));

        verify(storeService).getStoreCount();
        verify(storeService).getActiveStoreCount();
    }

    @Test
    void testEndpoint_ShouldReturnOkStatus() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/stores/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("Store API is working"));
    }
}