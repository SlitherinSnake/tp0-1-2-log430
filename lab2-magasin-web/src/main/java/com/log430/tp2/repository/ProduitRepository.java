package com.log430.tp2.repository;

import java.util.List;

// ProduitRepository.java
import org.springframework.data.jpa.repository.JpaRepository;

import com.log430.tp2.model.Produit;

public interface ProduitRepository extends JpaRepository<Produit, Integer> {
    List<Produit> findByNomContainingIgnoreCase(String nom);

    List<Produit> findByCategorieContainingIgnoreCase(String categorie);
}
