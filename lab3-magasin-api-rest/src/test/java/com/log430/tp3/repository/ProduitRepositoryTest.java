package com.log430.tp3.repository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;

import com.log430.tp3.model.Produit;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ProduitRepositoryTest {

    @Autowired
    private ProduitRepository produitRepository;

    @Test
    public void testCreateProduit() {
        // Vérifier la création d’un nouveau produit
        Produit produit = new Produit("Laptop", "Électronique", 999.99f, 10);
        Produit savedProduit = produitRepository.save(produit);
        
        // Le produit doit avoir un ID attribué
        assertNotNull(savedProduit.getId());
        assertTrue(savedProduit.getId() > 0);
        assertEquals("Laptop", savedProduit.getNom());
    }

    @Test
    public void testReadProduit() {
        // Vérifier la lecture d’un produit
        // 1) Créer d’abord un produit
        Produit produit = new Produit("Smartphone", "Électronique", 599.99f, 20);
        produitRepository.save(produit);
        
        // 2) Lire le produit via son ID
        Optional<Produit> foundProduit = produitRepository.findById(produit.getId());
        
        // 3) Vérifier que le produit existe et que ses attributs sont corrects
        assertTrue(foundProduit.isPresent());
        assertEquals("Smartphone", foundProduit.get().getNom());
        assertEquals("Électronique", foundProduit.get().getCategorie());
        assertEquals(599.99f, foundProduit.get().getPrix());
        assertEquals(20, foundProduit.get().getQuantite());
    }

    @Test
    public void testUpdateProduit() {
        // Vérifier la mise à jour d’un produit
        // 1) Créer un produit
        Produit produit = new Produit("Tablette", "Électronique", 299.99f, 15);
        produitRepository.save(produit);
        
        // 2) Modifier le produit
        produit.setNom("Tablette Pro");
        produit.setPrix(399.99f);
        produit.setQuantite(25);
        produitRepository.save(produit);
        
        // 3) Lire le produit mis à jour
        Optional<Produit> updatedProduit = produitRepository.findById(produit.getId());
        
        // 4) Vérifier les nouvelles valeurs
        assertTrue(updatedProduit.isPresent());
        assertEquals("Tablette Pro", updatedProduit.get().getNom());
        assertEquals(399.99f, updatedProduit.get().getPrix());
        assertEquals(25, updatedProduit.get().getQuantite());
    }

    @Test
    public void testDeleteProduit() {
        // Vérifier la suppression d’un produit
        // 1) Créer un produit
        Produit produit = new Produit("Écouteurs", "Audio", 99.99f, 30);
        produitRepository.save(produit);
        
        // 2) Supprimer le produit
        produitRepository.deleteById(produit.getId());
        
        // 3) Vérifier qu’il n’existe plus
        Optional<Produit> deletedProduit = produitRepository.findById(produit.getId());
        assertFalse(deletedProduit.isPresent());
    }

    @Test
    public void testFindByNomContainingIgnoreCase() {
        // Vérifier la recherche par nom (insensible à la casse)
        produitRepository.save(new Produit("Laptop HP", "Électronique", 899.99f, 5));
        produitRepository.save(new Produit("Laptop Dell", "Électronique", 999.99f, 8));
        produitRepository.save(new Produit("Smartphone Samsung", "Électronique", 699.99f, 12));
        
        // Rechercher les laptops
        List<Produit> laptops = produitRepository.findByNomContainingIgnoreCase("laptop");
        
        // Vérifier que les bons produits sont retournés
        assertEquals(2, laptops.size());
        assertTrue(laptops.stream().anyMatch(p -> p.getNom().equals("Laptop HP")));
        assertTrue(laptops.stream().anyMatch(p -> p.getNom().equals("Laptop Dell")));
    }

    @Test
    public void testFindByCategorieContainingIgnoreCase() {
        // Vérifier la recherche par catégorie
        produitRepository.save(new Produit("Laptop HP", "Électronique", 899.99f, 5));
        produitRepository.save(new Produit("Casque Audio", "Audio", 149.99f, 15));
        produitRepository.save(new Produit("Livre Java", "Livres", 39.99f, 25));
        
        // Rechercher la catégorie « Électro »
        List<Produit> electronics = produitRepository.findByCategorieContainingIgnoreCase("électro");
        
        // Vérifier la correspondance
        assertEquals(1, electronics.size());
        assertEquals("Laptop HP", electronics.get(0).getNom());
    }

    @Test
    public void testStockActuelOrdreCritique() {
        // Vérifier le tri des produits par niveau de stock
        produitRepository.save(new Produit("Produit A", "Catégorie A", 10.0f, 5));
        produitRepository.save(new Produit("Produit B", "Catégorie B", 20.0f, 2));
        produitRepository.save(new Produit("Produit C", "Catégorie C", 30.0f, 10));
        
        // Obtenir la liste triée par stock croissant
        List<Produit> produits = produitRepository.stockActuelOrdreCritique();
        
        // Vérifier l’ordre : B (2) < A (5) < C (10)
        assertEquals(3, produits.size());
        assertEquals("Produit B", produits.get(0).getNom());
        assertEquals("Produit A", produits.get(1).getNom());
        assertEquals("Produit C", produits.get(2).getNom());
    }
}