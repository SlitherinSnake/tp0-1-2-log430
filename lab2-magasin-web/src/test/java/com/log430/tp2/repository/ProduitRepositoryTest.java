package com.log430.tp2.repository;

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

import com.log430.tp2.model.Produit;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ProduitRepositoryTest {

    @Autowired
    private ProduitRepository produitRepository;

    @Test
    public void testCreateProduit() {
        // Test creating a new product
        Produit produit = new Produit("Laptop", "Électronique", 999.99f, 10);
        Produit savedProduit = produitRepository.save(produit);
        
        // Verify product was created with an ID
        assertNotNull(savedProduit.getId());
        assertTrue(savedProduit.getId() > 0);
        assertEquals("Laptop", savedProduit.getNom());
    }

    @Test
    public void testReadProduit() {
        // Test reading a product
        // First create a product
        Produit produit = new Produit("Smartphone", "Électronique", 599.99f, 20);
        produitRepository.save(produit);
        
        // Read the product by ID
        Optional<Produit> foundProduit = produitRepository.findById(produit.getId());
        
        // Verify product was found and has correct attributes
        assertTrue(foundProduit.isPresent());
        assertEquals("Smartphone", foundProduit.get().getNom());
        assertEquals("Électronique", foundProduit.get().getCategorie());
        assertEquals(599.99f, foundProduit.get().getPrix());
        assertEquals(20, foundProduit.get().getQuantite());
    }

    @Test
    public void testUpdateProduit() {
        // Test updating a product
        // First create a product
        Produit produit = new Produit("Tablette", "Électronique", 299.99f, 15);
        produitRepository.save(produit);
        
        // Update the product
        produit.setNom("Tablette Pro");
        produit.setPrix(399.99f);
        produit.setQuantite(25);
        produitRepository.save(produit);
        
        // Read the updated product
        Optional<Produit> updatedProduit = produitRepository.findById(produit.getId());
        
        // Verify product was updated correctly
        assertTrue(updatedProduit.isPresent());
        assertEquals("Tablette Pro", updatedProduit.get().getNom());
        assertEquals(399.99f, updatedProduit.get().getPrix());
        assertEquals(25, updatedProduit.get().getQuantite());
    }

    @Test
    public void testDeleteProduit() {
        // Test deleting a product
        // First create a product
        Produit produit = new Produit("Écouteurs", "Audio", 99.99f, 30);
        produitRepository.save(produit);
        
        // Delete the product
        produitRepository.deleteById(produit.getId());
        
        // Verify product was deleted
        Optional<Produit> deletedProduit = produitRepository.findById(produit.getId());
        assertFalse(deletedProduit.isPresent());
    }

    @Test
    public void testFindByNomContainingIgnoreCase() {
        // Test searching products by name
        produitRepository.save(new Produit("Laptop HP", "Électronique", 899.99f, 5));
        produitRepository.save(new Produit("Laptop Dell", "Électronique", 999.99f, 8));
        produitRepository.save(new Produit("Smartphone Samsung", "Électronique", 699.99f, 12));
        
        // Search for laptops
        List<Produit> laptops = produitRepository.findByNomContainingIgnoreCase("laptop");
        
        // Verify correct products were found
        assertEquals(2, laptops.size());
        assertTrue(laptops.stream().anyMatch(p -> p.getNom().equals("Laptop HP")));
        assertTrue(laptops.stream().anyMatch(p -> p.getNom().equals("Laptop Dell")));
    }

    @Test
    public void testFindByCategorieContainingIgnoreCase() {
        // Test searching products by category
        produitRepository.save(new Produit("Laptop HP", "Électronique", 899.99f, 5));
        produitRepository.save(new Produit("Casque Audio", "Audio", 149.99f, 15));
        produitRepository.save(new Produit("Livre Java", "Livres", 39.99f, 25));
        
        // Search for electronics
        List<Produit> electronics = produitRepository.findByCategorieContainingIgnoreCase("électro");
        
        // Verify correct products were found
        assertEquals(1, electronics.size());
        assertEquals("Laptop HP", electronics.get(0).getNom());
    }

    @Test
    public void testStockActuelOrdreCritique() {
        // Test sorting products by stock level
        produitRepository.save(new Produit("Produit A", "Catégorie A", 10.0f, 5));
        produitRepository.save(new Produit("Produit B", "Catégorie B", 20.0f, 2));
        produitRepository.save(new Produit("Produit C", "Catégorie C", 30.0f, 10));
        
        // Get products sorted by stock level
        List<Produit> produits = produitRepository.stockActuelOrdreCritique();
        
        // Verify products are sorted by stock level (ascending)
        assertEquals(3, produits.size());
        assertEquals("Produit B", produits.get(0).getNom());
        assertEquals("Produit A", produits.get(1).getNom());
        assertEquals("Produit C", produits.get(2).getNom());
    }
} 