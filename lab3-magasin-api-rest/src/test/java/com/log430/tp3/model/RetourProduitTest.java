package com.log430.tp3.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

class RetourProduitTest {

    private Retour retour;
    private Produit produit;

    @BeforeEach
    void setUp() {
        retour = new Retour();
        retour.setDateRetour(LocalDate.now());
        
        produit = new Produit("Souris", "Électronique", 29.99f, 25);
        produit.setId(5);
    }

    @Test
    void testDefaultConstructor() {
        RetourProduit retourProduit = new RetourProduit();
        
        assertEquals(0, retourProduit.getId());
        assertNull(retourProduit.getRetour());
        assertNull(retourProduit.getProduit());
        assertEquals(0, retourProduit.getQuantite());
    }

    @Test
    void testParameterizedConstructor() {
        int quantite = 3;
        
        RetourProduit retourProduit = new RetourProduit(retour, produit, quantite);
        
        assertEquals(0, retourProduit.getId()); // ID not set in constructor
        assertEquals(retour, retourProduit.getRetour());
        assertEquals(produit, retourProduit.getProduit());
        assertEquals(quantite, retourProduit.getQuantite());
    }

    @Test
    void testSettersAndGetters() {
        RetourProduit retourProduit = new RetourProduit();
        int quantite = 5;

        retourProduit.setRetour(retour);
        retourProduit.setProduit(produit);
        retourProduit.setQuantite(quantite);

        assertEquals(retour, retourProduit.getRetour());
        assertEquals(produit, retourProduit.getProduit());
        assertEquals(quantite, retourProduit.getQuantite());
    }

    @Test
    void testToStringWithValidData() {
        RetourProduit retourProduit = new RetourProduit(retour, produit, 4);
        
        String result = retourProduit.toString();
        assertTrue(result.contains("RetourProduit{id=0"));
        assertTrue(result.contains("produit=Souris"));
        assertTrue(result.contains("quantite=4"));
    }

    @Test
    void testToStringWithNullProduit() {
        RetourProduit retourProduit = new RetourProduit();
        retourProduit.setRetour(retour);
        retourProduit.setQuantite(2);
        
        String result = retourProduit.toString();
        assertTrue(result.contains("RetourProduit{id=0"));
        assertTrue(result.contains("produit=null"));
        assertTrue(result.contains("quantite=2"));
    }

    @Test
    void testSetNullValues() {
        RetourProduit retourProduit = new RetourProduit(retour, produit, 3);
        
        retourProduit.setRetour(null);
        retourProduit.setProduit(null);

        assertNull(retourProduit.getRetour());
        assertNull(retourProduit.getProduit());
        assertEquals(3, retourProduit.getQuantite()); // Quantity remains unchanged
    }

    @Test
    void testQuantiteEdgeCases() {
        RetourProduit retourProduit = new RetourProduit();
        
        // Test zero quantity
        retourProduit.setQuantite(0);
        assertEquals(0, retourProduit.getQuantite());
        
        // Test negative quantity (might be allowed depending on business rules)
        retourProduit.setQuantite(-1);
        assertEquals(-1, retourProduit.getQuantite());
        
        // Test large quantity
        retourProduit.setQuantite(999);
        assertEquals(999, retourProduit.getQuantite());
    }

    @Test
    void testBidirectionalRelationship() {
        // Create a RetourProduit and add it to the retour
        RetourProduit retourProduit = new RetourProduit(retour, produit, 2);
        
        // Manually add to retour's list (simulating what ajouterProduit does)
        retour.getProduitsRetournes().add(retourProduit);
        
        // Verify bidirectional relationship
        assertEquals(retour, retourProduit.getRetour());
        assertTrue(retour.getProduitsRetournes().contains(retourProduit));
        assertEquals(1, retour.getProduitsRetournes().size());
    }

    @Test
    void testChangeRetourReference() {
        // Create two different retours
        Retour autreRetour = new Retour();
        autreRetour.setDateRetour(LocalDate.now().minusDays(1));
        
        RetourProduit retourProduit = new RetourProduit(retour, produit, 1);
        
        // Change the retour reference
        retourProduit.setRetour(autreRetour);
        
        assertEquals(autreRetour, retourProduit.getRetour());
        assertNotEquals(retour, retourProduit.getRetour());
    }

    @Test
    void testChangeProduitReference() {
        Produit autreProduit = new Produit("Clavier", "Électronique", 49.99f, 12);
        autreProduit.setId(6);
        
        RetourProduit retourProduit = new RetourProduit(retour, produit, 1);
        
        // Change the produit reference
        retourProduit.setProduit(autreProduit);
        
        assertEquals(autreProduit, retourProduit.getProduit());
        assertEquals("Clavier", retourProduit.getProduit().getNom());
        assertNotEquals(produit, retourProduit.getProduit());
    }
}
