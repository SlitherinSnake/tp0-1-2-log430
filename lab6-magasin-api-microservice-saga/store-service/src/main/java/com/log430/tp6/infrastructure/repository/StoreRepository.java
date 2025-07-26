package com.log430.tp6.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.log430.tp6.domain.store.Store;

/**
 * Repository interface for Store entities.
 * Provides data access operations for store management.
 */
public interface StoreRepository extends JpaRepository<Store, Long> {

    /**
     * Find all active stores.
     */
    List<Store> findByIsActiveTrue();

    /**
     * Find store by name.
     */
    Optional<Store> findByNomAndIsActiveTrue(String nom);

    /**
     * Find stores by district/quartier.
     */
    List<Store> findByQuartierAndIsActiveTrue(String quartier);

    /**
     * Find stores by name containing the given text.
     */
    List<Store> findByNomContainingIgnoreCaseAndIsActiveTrue(String nom);
}
