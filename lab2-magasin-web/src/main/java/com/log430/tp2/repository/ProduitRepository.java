package com.log430.tp2.repository;

import java.util.List;

// ProduitRepository.java
import org.springframework.data.jpa.repository.JpaRepository;

import com.log430.tp2.model.Produit;

public interface ProduitRepository extends JpaRepository<Produit, Integer> {

    // Recherche des produits dont le nom contient une chaîne de caractères
    List<Produit> findByNomContainingIgnoreCase(String nom);

    // Recherche des produits dont la catégorie contient une chaîne de caractères
    List<Produit> findByCategorieContainingIgnoreCase(String categorie);

    // Recherche des produits dont le nom et la catégorie contiennent une chaîne de caractères
    List<Produit> findByNomContainingIgnoreCaseAndCategorieContainingIgnoreCase(String nom, String categorie);
}
