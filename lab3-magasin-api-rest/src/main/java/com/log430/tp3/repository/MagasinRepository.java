package com.log430.tp3.repository;

// MagasinRepository.java
import org.springframework.data.jpa.repository.JpaRepository;
import com.log430.tp3.model.Magasin;

public interface MagasinRepository extends JpaRepository<Magasin, Integer> {
    // For query
}
