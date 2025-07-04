package com.log430.tp4.repository;

import java.util.List;

// ProduitRepository.java
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.log430.tp4.model.Produit;

@Repository
public interface ProduitRepository extends JpaRepository<Produit, Integer> {

    // Recherche tp4 produits dont le nom contient une chaîne de caractères
    List<Produit> findByNomContainingIgnoreCase(String nom);

    // Recherche tp4 produits dont la catégorie contient une chaîne de caractères
    List<Produit> findByCategorieContainingIgnoreCase(String categorie);

    // Recherche tp4 produits dont le nom et la catégorie contiennent une chaîne de
    // caractères
    List<Produit> findByNomContainingIgnoreCaseAndCategorieContainingIgnoreCase(String nom, String categorie);

    /**
     * Requête personnalisée pour lister tous les produits triés par stock croissant.
     * Permet d’identifier rapidement les produits proches de la rupture de stock.
     * Exemple d’utilisation : affichage d’un tableau d’alerte ou d’un rapport critique.
     */
    @Query("SELECT p FROM Produit p ORDER BY p.quantite ASC")
    List<Produit> stockActuelOrdreCritique(); // Optionnel

}
