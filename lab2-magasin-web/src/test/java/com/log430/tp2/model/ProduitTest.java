package com.log430.tp2.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class ProduitTest {

    @Test
    public void testConstructorAndGetters() {
        // Test constructor and getters
        Produit produit = new Produit("Test Produit", "Test Catégorie", 123.45f, 10);
        
        assertEquals("Test Produit", produit.getNom());
        assertEquals("Test Catégorie", produit.getCategorie());
        assertEquals(123.45f, produit.getPrix());
        assertEquals(10, produit.getQuantite());
    }
    
    @Test
    public void testSetters() {
        // Test setters
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
        // Test hasStock method
        Produit produit = new Produit("Test Produit", "Test Catégorie", 123.45f, 10);
        
        // Test with sufficient stock
        assertTrue(produit.hasStock(5));
        assertTrue(produit.hasStock(10));
        
        // Test with insufficient stock
        assertFalse(produit.hasStock(11));
        assertFalse(produit.hasStock(20));
    }
    
    @Test
    public void testDecreaseStock() {
        // Test decreaseStock method
        Produit produit = new Produit("Test Produit", "Test Catégorie", 123.45f, 10);
        
        // Test decreasing stock with valid quantity
        produit.decreaseStock(5);
        assertEquals(5, produit.getQuantite());
        
        produit.decreaseStock(3);
        assertEquals(2, produit.getQuantite());
        
        // Test decreasing stock with exact remaining quantity
        produit.decreaseStock(2);
        assertEquals(0, produit.getQuantite());
    }
    
    @Test
    public void testDecreaseStockWithInsufficientStock() {
        // Test decreaseStock method with insufficient stock
        Produit produit = new Produit("Test Produit", "Test Catégorie", 123.45f, 10);
        
        // Test decreasing stock with invalid quantity
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            produit.decreaseStock(15);
        });
        
        assertEquals("Stock insuffisant", exception.getMessage());
        assertEquals(10, produit.getQuantite()); // Stock should remain unchanged
    }
    
    @Test
    public void testToString() {
        // Test toString method
        Produit produit = new Produit("Test Produit", "Test Catégorie", 123.45f, 10);
        produit.setId(1);
        
        String expected = "Produit{id=1, nom='Test Produit', categorie='Test Catégorie', prix=123.45, quantite=10}";
        assertEquals(expected, produit.toString());
    }
} 