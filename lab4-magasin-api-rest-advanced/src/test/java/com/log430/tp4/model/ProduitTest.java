package com.log430.tp4.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class ProduitTest {

    @Test
    public void testConstructorAndGetters() {
        // Tester le constructeur et les getters
        Produit produit = new Produit("Test Produit", "Test Catégorie", 123.45f, 10);
        
        assertEquals("Test Produit", produit.getNom());
        assertEquals("Test Catégorie", produit.getCategorie());
        assertEquals(123.45f, produit.getPrix());
        assertEquals(10, produit.getQuantite());
    }
    
    @Test
    public void testSetters() {
        // Tester les setters
        Produit produit = new Produit();
        
        produit.setNom("Test Produit");
        produit.setCategorie("Test Catégorie");
        produit.setPrix(123.45f);
        produit.setQuantite(10);
        produit.setId(1);
        
        assertEquals("Test Produit", produit.getNom());
        assertEquals("Test Catégorie", produit.getCategorie());
        assertEquals(123.45f, produit.getPrix());
        assertEquals(10, produit.getQuantite());
        assertEquals(1, produit.getId());
    }
    
    @Test
    public void testHasStock() {
        // Tester la méthode hasStock
        Produit produit = new Produit("Test Produit", "Test Catégorie", 123.45f, 10);
        
        // Cas de stock suffisant
        assertTrue(produit.hasStock(5));
        assertTrue(produit.hasStock(10));
        
        // Cas de stock insuffisant
        assertFalse(produit.hasStock(11));
        assertFalse(produit.hasStock(20));
    }
    
    @Test
    public void testDecreaseStock() {
        // Tester la méthode decreaseStock
        Produit produit = new Produit("Test Produit", "Test Catégorie", 123.45f, 10);
        
        // Diminution du stock avec une quantité valide
        produit.decreaseStock(5);
        assertEquals(5, produit.getQuantite());
        
        produit.decreaseStock(3);
        assertEquals(2, produit.getQuantite());
        
        // Diminution du stock avec la quantité restante exacte
        produit.decreaseStock(2);
        assertEquals(0, produit.getQuantite());
    }
    
    @Test
    public void testDecreaseStockWithInsufficientStock() {
        // Tester la méthode decreaseStock avec stock insuffisant
        Produit produit = new Produit("Test Produit", "Test Catégorie", 123.45f, 10);
        
        // Diminution du stock avec une quantité invalide
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            produit.decreaseStock(15);
        });
        
        assertEquals("Stock insuffisant", exception.getMessage());
        assertEquals(10, produit.getQuantite());  // Le stock doit rester inchangé
    }
    
    @Test
    public void testToString() {
        // Tester la méthode toString
        Produit produit = new Produit("Test Produit", "Test Catégorie", 123.45f, 10);
        produit.setId(1);
        
        String expected = "Produit{id=1, nom='Test Produit', categorie='Test Catégorie', prix=123.45, quantite=10}";
        assertEquals(expected, produit.toString());
    }
} 