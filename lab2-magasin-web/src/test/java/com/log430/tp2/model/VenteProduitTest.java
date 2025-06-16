package com.log430.tp2.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class VenteProduitTest {

    @Test
    public void testConstructorAndGetters() {
        // Create test objects
        Vente vente = new Vente();
        vente.setId(1);
        
        Produit produit = new Produit("Laptop", "Électronique", 999.99f, 10);
        produit.setId(1);
        
        int quantite = 2;
        
        // Create VenteProduit using constructor
        VenteProduit venteProduit = new VenteProduit(vente, produit, quantite);
        
        // Verify attributes
        assertEquals(vente, venteProduit.getVente());
        assertEquals(produit, venteProduit.getProduit());
        assertEquals(quantite, venteProduit.getQuantite());
    }
    
    @Test
    public void testSetters() {
        // Create test objects
        Vente vente = new Vente();
        vente.setId(1);
        
        Vente newVente = new Vente();
        newVente.setId(2);
        
        Produit produit = new Produit("Laptop", "Électronique", 999.99f, 10);
        produit.setId(1);
        
        Produit newProduit = new Produit("Smartphone", "Électronique", 599.99f, 20);
        newProduit.setId(2);
        
        // Create VenteProduit
        VenteProduit venteProduit = new VenteProduit();
        
        // Set attributes
        venteProduit.setVente(vente);
        venteProduit.setProduit(produit);
        venteProduit.setQuantite(2);
        
        // Verify attributes
        assertEquals(vente, venteProduit.getVente());
        assertEquals(produit, venteProduit.getProduit());
        assertEquals(2, venteProduit.getQuantite());
        
        // Update attributes
        venteProduit.setVente(newVente);
        venteProduit.setProduit(newProduit);
        venteProduit.setQuantite(3);
        
        // Verify updated attributes
        assertEquals(newVente, venteProduit.getVente());
        assertEquals(newProduit, venteProduit.getProduit());
        assertEquals(3, venteProduit.getQuantite());
    }
    
    @Test
    public void testGetSousTotal() {
        // Create test objects
        Produit produit = new Produit("Laptop", "Électronique", 999.99f, 10);
        
        // Create VenteProduit
        VenteProduit venteProduit = new VenteProduit(null, produit, 2);
        
        // Calculate expected sous-total: 2 * 999.99 = 1999.98
        double expectedSousTotal = 2 * 999.99;
        
        // Verify sous-total
        assertEquals(expectedSousTotal, venteProduit.getSousTotal(), 0.01);
        
        // Update quantity
        venteProduit.setQuantite(3);
        
        // Recalculate expected sous-total: 3 * 999.99 = 2999.97
        expectedSousTotal = 3 * 999.99;
        
        // Verify updated sous-total
        assertEquals(expectedSousTotal, venteProduit.getSousTotal(), 0.01);
    }
    
    @Test
    public void testToString() {
        // Create test objects
        Produit produit = new Produit("Laptop", "Électronique", 999.99f, 10);
        produit.setId(1);
        
        // Create VenteProduit
        VenteProduit venteProduit = new VenteProduit(null, produit, 2);
        
        // Expected toString result
        String expected = "VenteProduit{id=0, produit=Laptop, quantite=2}";
        
        // Verify toString
        assertEquals(expected, venteProduit.toString());
    }
} 