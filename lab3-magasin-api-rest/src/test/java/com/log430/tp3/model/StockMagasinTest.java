package com.log430.tp3.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StockMagasinTest {

    private Produit produit;
    private Magasin magasin;

    @BeforeEach
    void setUp() {
        produit = new Produit("Smartphone", "Électronique", 599.99f, 15);
        produit.setId(2);
        
        magasin = new Magasin();
        magasin.setId(2);
        magasin.setNom("Magasin Banlieue");
        magasin.setQuartier("Banlieue");
    }

    @Test
    void testDefaultConstructor() {
        StockMagasin stock = new StockMagasin();
        
        assertNull(stock.getId());
        assertNull(stock.getProduit());
        assertNull(stock.getMagasin());
        assertEquals(0, stock.getQuantite());
    }

    @Test
    void testParameterizedConstructor() {
        int quantite = 25;
        
        StockMagasin stock = new StockMagasin(produit, magasin, quantite);
        
        assertNull(stock.getId()); // ID not set in constructor
        assertEquals(produit, stock.getProduit());
        assertEquals(magasin, stock.getMagasin());
        assertEquals(quantite, stock.getQuantite());
    }

    @Test
    void testSettersAndGetters() {
        StockMagasin stock = new StockMagasin();
        int quantite = 50;

        stock.setProduit(produit);
        stock.setMagasin(magasin);
        stock.setQuantite(quantite);

        assertEquals(produit, stock.getProduit());
        assertEquals(magasin, stock.getMagasin());
        assertEquals(quantite, stock.getQuantite());
    }

    @Test
    void testToStringWithValidData() {
        StockMagasin stock = new StockMagasin(produit, magasin, 30);
        
        String result = stock.toString();
        assertTrue(result.contains("StockMagasin{id=null"));
        assertTrue(result.contains("produit=Smartphone"));
        assertTrue(result.contains("magasin=Magasin Banlieue"));
        assertTrue(result.contains("quantite=30"));
    }

    @Test
    void testToStringWithNullValues() {
        StockMagasin stock = new StockMagasin();
        
        String result = stock.toString();
        assertTrue(result.contains("StockMagasin{id=null"));
        assertTrue(result.contains("produit=null"));
        assertTrue(result.contains("magasin=null"));
        assertTrue(result.contains("quantite=0"));
    }

    @Test
    void testSetNullValues() {
        StockMagasin stock = new StockMagasin(produit, magasin, 20);
        
        stock.setProduit(null);
        stock.setMagasin(null);

        assertNull(stock.getProduit());
        assertNull(stock.getMagasin());
        assertEquals(20, stock.getQuantite()); // Quantity remains unchanged
    }

    @Test
    void testQuantiteEdgeCases() {
        StockMagasin stock = new StockMagasin();
        
        // Test zero quantity
        stock.setQuantite(0);
        assertEquals(0, stock.getQuantite());
        
        // Test negative quantity (might be allowed depending on business rules)
        stock.setQuantite(-10);
        assertEquals(-10, stock.getQuantite());
        
        // Test large quantity
        stock.setQuantite(9999);
        assertEquals(9999, stock.getQuantite());
    }

    @Test
    void testCompleteObjectCreation() {
        StockMagasin stock = new StockMagasin(produit, magasin, 75);
        
        // Verify all fields are set correctly
        assertEquals(produit, stock.getProduit());
        assertEquals("Smartphone", stock.getProduit().getNom());
        assertEquals(magasin, stock.getMagasin());
        assertEquals("Magasin Banlieue", stock.getMagasin().getNom());
        assertEquals(75, stock.getQuantite());
    }
}
