package com.log430.tp2.repository;

// VenteRepository.java
import org.springframework.data.jpa.repository.JpaRepository;

import com.log430.tp2.model.Vente;

public interface VenteRepository extends JpaRepository<Vente, Integer> {
    // For query
}
