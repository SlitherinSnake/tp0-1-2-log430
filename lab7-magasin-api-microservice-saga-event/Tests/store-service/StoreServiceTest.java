package com.log430.tp7.store.service;

import com.log430.tp7.application.service.StoreService;
import com.log430.tp7.domain.store.Store;
import com.log430.tp7.infrastructure.repository.StoreRepository;
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
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private StoreService storeService;

    private Store testStore;

    @BeforeEach
    void setUp() {
        testStore = new Store();
        testStore.setId(1L);
        testStore.setNom("Test Store");
        testStore.setQuartier("Downtown");
        testStore.setAdresse("123 Test Street");
        testStore.setTelephone("555-0123");
        testStore.setActif(true);
    }

    @Test
    void getAllActiveStores_ShouldReturnOnlyActiveStores() {
        // Given
        List<Store> activeStores = Arrays.asList(testStore);
        when(storeRepository.findByActifTrue()).thenReturn(activeStores);

        // When
        List<Store> result = storeService.getAllActiveStores();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNom()).isEqualTo("Test Store");
        assertThat(result.get(0).getActif()).isTrue();
        verify(storeRepository).findByActifTrue();
    }

    @Test
    void getStoreById_WhenStoreExists_ShouldReturnStore() {
        // Given
        when(storeRepository.findById(1L)).thenReturn(Optional.of(testStore));

        // When
        Optional<Store> result = storeService.getStoreById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getNom()).isEqualTo("Test Store");
        verify(storeRepository).findById(1L);
    }

    @Test
    void getStoreById_WhenStoreNotExists_ShouldReturnEmpty() {
        // Given
        when(storeRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Store> result = storeService.getStoreById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(storeRepository).findById(999L);
    }

    @Test
    void createStore_WithValidData_ShouldCreateAndReturnStore() {
        // Given
        when(storeRepository.existsByNom("New Store")).thenReturn(false);
        when(storeRepository.save(any(Store.class))).thenReturn(testStore);

        // When
        Store result = storeService.createStore("New Store", "Uptown", "456 New Street", "555-0456");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNom()).isEqualTo("Test Store");
        verify(storeRepository).existsByNom("New Store");
        verify(storeRepository).save(any(Store.class));
    }

    @Test
    void createStore_WithNullName_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> storeService.createStore(null, "Uptown", "456 New Street", "555-0456"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nom");
    }

    @Test
    void createStore_WithEmptyName_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> storeService.createStore("", "Uptown", "456 New Street", "555-0456"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nom");
    }

    @Test
    void createStore_WithDuplicateName_ShouldThrowException() {
        // Given
        when(storeRepository.existsByNom("Test Store")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> storeService.createStore("Test Store", "Uptown", "456 New Street", "555-0456"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void createStore_WithNullQuartier_ShouldThrowException() {
        // Given
        when(storeRepository.existsByNom("New Store")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> storeService.createStore("New Store", null, "456 New Street", "555-0456"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("quartier");
    }

    @Test
    void updateStore_WhenStoreExists_ShouldUpdateAndReturnStore() {
        // Given
        when(storeRepository.findById(1L)).thenReturn(Optional.of(testStore));
        when(storeRepository.save(any(Store.class))).thenReturn(testStore);

        // When
        Store result = storeService.updateStore(1L, "Updated Store", "Downtown", "123 Updated Street", "555-9999");

        // Then
        assertThat(result).isNotNull();
        verify(storeRepository).findById(1L);
        verify(storeRepository).save(testStore);
    }

    @Test
    void updateStore_WhenStoreNotExists_ShouldThrowException() {
        // Given
        when(storeRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> storeService.updateStore(999L, "Updated Store", "Downtown", "123 Updated Street", "555-9999"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void findStoreByName_WhenStoreExists_ShouldReturnStore() {
        // Given
        when(storeRepository.findByNom("Test Store")).thenReturn(Optional.of(testStore));

        // When
        Optional<Store> result = storeService.findStoreByName("Test Store");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getNom()).isEqualTo("Test Store");
        verify(storeRepository).findByNom("Test Store");
    }

    @Test
    void findStoresByQuartier_ShouldReturnStoresInQuartier() {
        // Given
        List<Store> stores = Arrays.asList(testStore);
        when(storeRepository.findByQuartierAndActifTrue("Downtown")).thenReturn(stores);

        // When
        List<Store> result = storeService.findStoresByQuartier("Downtown");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getQuartier()).isEqualTo("Downtown");
        verify(storeRepository).findByQuartierAndActifTrue("Downtown");
    }

    @Test
    void searchStoresByName_ShouldReturnMatchingStores() {
        // Given
        List<Store> stores = Arrays.asList(testStore);
        when(storeRepository.findByNomContainingIgnoreCaseAndActifTrue("Test")).thenReturn(stores);

        // When
        List<Store> result = storeService.searchStoresByName("Test");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNom()).containsIgnoringCase("Test");
        verify(storeRepository).findByNomContainingIgnoreCaseAndActifTrue("Test");
    }

    @Test
    void deactivateStore_WhenStoreExists_ShouldDeactivateStore() {
        // Given
        when(storeRepository.findById(1L)).thenReturn(Optional.of(testStore));
        when(storeRepository.save(any(Store.class))).thenReturn(testStore);

        // When
        storeService.deactivateStore(1L);

        // Then
        verify(storeRepository).findById(1L);
        verify(storeRepository).save(testStore);
    }

    @Test
    void deactivateStore_WhenStoreNotExists_ShouldThrowException() {
        // Given
        when(storeRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> storeService.deactivateStore(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void activateStore_WhenStoreExists_ShouldActivateStore() {
        // Given
        testStore.setActif(false);
        when(storeRepository.findById(1L)).thenReturn(Optional.of(testStore));
        when(storeRepository.save(any(Store.class))).thenReturn(testStore);

        // When
        storeService.activateStore(1L);

        // Then
        verify(storeRepository).findById(1L);
        verify(storeRepository).save(testStore);
    }

    @Test
    void getAllStores_ShouldReturnAllStores() {
        // Given
        Store inactiveStore = new Store();
        inactiveStore.setActif(false);
        List<Store> allStores = Arrays.asList(testStore, inactiveStore);
        when(storeRepository.findAll()).thenReturn(allStores);

        // When
        List<Store> result = storeService.getAllStores();

        // Then
        assertThat(result).hasSize(2);
        verify(storeRepository).findAll();
    }

    @Test
    void isStoreActive_WhenStoreExists_ShouldReturnActiveStatus() {
        // Given
        when(storeRepository.findById(1L)).thenReturn(Optional.of(testStore));

        // When
        boolean result = storeService.isStoreActive(1L);

        // Then
        assertThat(result).isTrue();
        verify(storeRepository).findById(1L);
    }

    @Test
    void isStoreActive_WhenStoreNotExists_ShouldReturnFalse() {
        // Given
        when(storeRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        boolean result = storeService.isStoreActive(999L);

        // Then
        assertThat(result).isFalse();
        verify(storeRepository).findById(999L);
    }

    @Test
    void getStoreCount_ShouldReturnTotalCount() {
        // Given
        when(storeRepository.count()).thenReturn(5L);

        // When
        long result = storeService.getStoreCount();

        // Then
        assertThat(result).isEqualTo(5L);
        verify(storeRepository).count();
    }

    @Test
    void getActiveStoreCount_ShouldReturnActiveCount() {
        // Given
        when(storeRepository.countByActifTrue()).thenReturn(4L);

        // When
        long result = storeService.getActiveStoreCount();

        // Then
        assertThat(result).isEqualTo(4L);
        verify(storeRepository).countByActifTrue();
    }

    @Test
    void getStoresByQuartierCount_ShouldReturnQuartierSpecificCount() {
        // Given
        List<Store> downtownStores = Arrays.asList(testStore);
        when(storeRepository.findByQuartierAndActifTrue("Downtown")).thenReturn(downtownStores);

        // When
        List<Store> result = storeService.findStoresByQuartier("Downtown");

        // Then
        assertThat(result).hasSize(1);
        verify(storeRepository).findByQuartierAndActifTrue("Downtown");
    }
}