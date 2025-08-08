package com.log430.tp7.application.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.log430.tp7.domain.store.Store;
import com.log430.tp7.infrastructure.repository.StoreRepository;

/**
 * Application service for store management operations.
 * Coordinates domain logic and data access for stores.
 */
@Service
@Transactional
public class StoreService {
    private static final Logger log = LoggerFactory.getLogger(StoreService.class);

    private static final String STORE_NOT_FOUND_MSG = "Store not found with id: ";

    private final StoreRepository storeRepository;

    public StoreService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    /**
     * Get all active stores.
     */
    @Transactional(readOnly = true)
    @Cacheable("activeStores")
    public List<Store> getAllActiveStores() {
        log.info("Fetching all active stores");
        List<Store> stores = storeRepository.findByIsActiveTrue();
        log.info("Found {} active stores", stores.size());
        return stores;
    }

    /**
     * Get store by ID.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "storeById", key = "#id")
    public Optional<Store> getStoreById(Long id) {
        log.info("Fetching store by id: {}", id);
        return storeRepository.findById(id);
    }

    /**
     * Create new store.
     */
    @Caching(evict = {
        @CacheEvict(value = "activeStores", allEntries = true),
        @CacheEvict(value = "storesByQuartier", allEntries = true)
    })
    public Store createStore(String nom, String quartier, String adresse, String telephone) {
        log.info("Creating new store: {}", nom);
        Store store = new Store(nom, quartier, adresse, telephone);
        Store savedStore = storeRepository.save(store);
        log.info("Created store with id: {}", savedStore.getId());
        return savedStore;
    }

    /**
     * Update store information.
     */
    @Caching(evict = {
        @CacheEvict(value = "activeStores", allEntries = true),
        @CacheEvict(value = "storeById", key = "#id"),
        @CacheEvict(value = "storesByQuartier", allEntries = true),
        @CacheEvict(value = "storesByName", allEntries = true)
    })
    public Store updateStore(Long id, String nom, String quartier, String adresse, String telephone) {
        log.info("Updating store with id: {}", id);
        return storeRepository.findById(id)
                .map(store -> {
                    store.setNom(nom);
                    store.setQuartier(quartier);
                    store.setAdresse(adresse);
                    store.setTelephone(telephone);
                    Store updatedStore = storeRepository.save(store);
                    log.info("Updated store with id: {}", id);
                    return updatedStore;
                })
                .orElseThrow(() -> new IllegalArgumentException(STORE_NOT_FOUND_MSG + id));
    }

    /**
     * Find store by name.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "storeByName", key = "#nom")
    public Optional<Store> findStoreByName(String nom) {
        log.info("Searching for store by name: {}", nom);
        return storeRepository.findByNomAndIsActiveTrue(nom);
    }

    /**
     * Find stores by quartier.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "storesByQuartier", key = "#quartier")
    public List<Store> findStoresByQuartier(String quartier) {
        log.info("Searching stores by quartier: {}", quartier);
        return storeRepository.findByQuartierAndIsActiveTrue(quartier);
    }

    /**
     * Search stores by name containing text.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "storesByName", key = "#nom")
    public List<Store> searchStoresByName(String nom) {
        log.info("Searching stores by name containing: {}", nom);
        return storeRepository.findByNomContainingIgnoreCaseAndIsActiveTrue(nom);
    }

    /**
     * Deactivate store.
     */
    @Caching(evict = {
        @CacheEvict(value = "activeStores", allEntries = true),
        @CacheEvict(value = "storeById", key = "#id"),
        @CacheEvict(value = "storesByQuartier", allEntries = true),
        @CacheEvict(value = "storesByName", allEntries = true)
    })
    public void deactivateStore(Long id) {
        log.warn("Deactivating store with id: {}", id);
        storeRepository.findById(id)
                .ifPresentOrElse(
                        store -> {
                            store.setActive(false);
                            storeRepository.save(store);
                            log.info("Store {} deactivated successfully", id);
                        },
                        () -> {
                            log.error("Store not found for deactivation: {}", id);
                            throw new IllegalArgumentException(STORE_NOT_FOUND_MSG + id);
                        }
                );
    }

    /**
     * Activate store.
     */
    @Caching(evict = {
        @CacheEvict(value = "activeStores", allEntries = true),
        @CacheEvict(value = "storeById", key = "#id"),
        @CacheEvict(value = "storesByQuartier", allEntries = true),
        @CacheEvict(value = "storesByName", allEntries = true)
    })
    public void activateStore(Long id) {
        log.info("Activating store with id: {}", id);
        storeRepository.findById(id)
                .ifPresentOrElse(
                        store -> {
                            store.setActive(true);
                            storeRepository.save(store);
                            log.info("Store {} activated successfully", id);
                        },
                        () -> {
                            log.error("Store not found for activation: {}", id);
                            throw new IllegalArgumentException(STORE_NOT_FOUND_MSG + id);
                        }
                );
    }

    /**
     * Get all stores (including inactive ones).
     */
    @Transactional(readOnly = true)
    public List<Store> getAllStores() {
        log.info("Fetching all stores");
        return storeRepository.findAll();
    }

    /**
     * Check if store exists and is active.
     */
    @Transactional(readOnly = true)
    public boolean isStoreActive(Long id) {
        log.info("Checking if store {} is active", id);
        return storeRepository.findById(id)
                .map(Store::isActive)
                .orElse(false);
    }

    /**
     * Get store count.
     */
    @Transactional(readOnly = true)
    public long getStoreCount() {
        log.info("Counting total stores");
        return storeRepository.count();
    }

    /**
     * Get active store count.
     */
    @Transactional(readOnly = true)
    public long getActiveStoreCount() {
        log.info("Counting active stores");
        return storeRepository.findByIsActiveTrue().size();
    }
}
