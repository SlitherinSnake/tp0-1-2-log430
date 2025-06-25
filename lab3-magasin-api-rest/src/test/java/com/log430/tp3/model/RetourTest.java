package com.log430.tp3.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

class RetourTest {

    private Retour retour;
    private Vente vente;
    private Employe employe;
    private Produit produit1;
    private Produit produit2;

    @BeforeEach
    void setUp() {
        employe = new Employe("Marie Dubois", "MD001");
        
        vente = new Vente();
        vente.setId(1);
        vente.setDateVente(LocalDate.now().minusDays(5));
        
        produit1 = new Produit("Tablet", "Électronique", 299.99f, 8);
        produit1.setId(3);
        produit2 = new Produit("Casque", "Électronique", 79.99f, 15);
        produit2.setId(4);
        
        retour = new Retour();
    }

    @Test
    void testDefaultConstructor() {
        Retour newRetour = new Retour();
        
        assertEquals(0, newRetour.getId());
        assertNull(newRetour.getDateRetour());
        assertNull(newRetour.getVente());
        assertNull(newRetour.getEmploye());
        assertNotNull(newRetour.getProduitsRetournes());
        assertTrue(newRetour.getProduitsRetournes().isEmpty());
    }

    @Test
    void testParameterizedConstructor() {
        LocalDate dateRetour = LocalDate.now();
        List<RetourProduit> retourProduits = new ArrayList<>();
        
        Retour newRetour = new Retour(dateRetour, vente, employe, retourProduits);
        
        assertEquals(0, newRetour.getId()); // ID not set in constructor
        assertEquals(dateRetour, newRetour.getDateRetour());
        assertEquals(vente, newRetour.getVente());
        assertEquals(employe, newRetour.getEmploye());
        assertEquals(retourProduits, newRetour.getProduitsRetournes());
    }

    @Test
    void testSettersAndGetters() {
        LocalDate dateRetour = LocalDate.of(2023, 3, 15);
        List<RetourProduit> retourProduits = new ArrayList<>();

        retour.setDateRetour(dateRetour);
        retour.setVente(vente);
        retour.setEmploye(employe);
        retour.setProduitsRetournes(retourProduits);

        assertEquals(dateRetour, retour.getDateRetour());
        assertEquals(vente, retour.getVente());
        assertEquals(employe, retour.getEmploye());
        assertEquals(retourProduits, retour.getProduitsRetournes());
    }

    @Test
    void testAjouterProduit() {
        // Ajouter un produit au retour
        retour.ajouterProduit(produit1, 2);

        // Vérifier que le produit a été ajouté
        List<RetourProduit> items = retour.getProduitsRetournes();
        assertEquals(1, items.size());
        assertEquals(produit1, items.get(0).getProduit());
        assertEquals(2, items.get(0).getQuantite());
        assertEquals(retour, items.get(0).getRetour());

        // Ajouter un autre produit
        retour.ajouterProduit(produit2, 1);

        // Vérifier que le second produit a été ajouté
        items = retour.getProduitsRetournes();
        assertEquals(2, items.size());
        assertEquals(produit2, items.get(1).getProduit());
        assertEquals(1, items.get(1).getQuantite());
    }

    @Test
    void testAjouterProduitMultipleQuantities() {
        retour.ajouterProduit(produit1, 3);
        retour.ajouterProduit(produit1, 2); // Note: This adds a new entry, doesn't update existing

        List<RetourProduit> items = retour.getProduitsRetournes();
        assertEquals(2, items.size()); // Two separate entries for the same product
        assertEquals(3, items.get(0).getQuantite());
        assertEquals(2, items.get(1).getQuantite());
    }

    @Test
    void testToString() {
        retour.setDateRetour(LocalDate.now());
        retour.setVente(vente);
        retour.setEmploye(employe);
        retour.ajouterProduit(produit1, 1);

        String result = retour.toString();
        assertTrue(result.contains("Retour{id=0"));
        assertTrue(result.contains("dateRetour="));
        assertTrue(result.contains("vente="));
        assertTrue(result.contains("employe="));
        assertTrue(result.contains("produitsRetournes="));
    }

    @Test
    void testSetNullValues() {
        retour.setDateRetour(null);
        retour.setVente(null);
        retour.setEmploye(null);
        retour.setProduitsRetournes(null);

        assertNull(retour.getDateRetour());
        assertNull(retour.getVente());
        assertNull(retour.getEmploye());
        assertNull(retour.getProduitsRetournes());
    }

    @Test
    void testCompleteRetourWorkflow() {
        // Setup a complete return scenario
        LocalDate dateRetour = LocalDate.now();
        retour.setDateRetour(dateRetour);
        retour.setVente(vente);
        retour.setEmploye(employe);

        // Add multiple products to return
        retour.ajouterProduit(produit1, 1);
        retour.ajouterProduit(produit2, 2);

        // Verify the complete setup
        assertEquals(dateRetour, retour.getDateRetour());
        assertEquals(vente, retour.getVente());
        assertEquals(employe, retour.getEmploye());
        assertEquals(2, retour.getProduitsRetournes().size());

        // Verify bidirectional relationships
        for (RetourProduit rp : retour.getProduitsRetournes()) {
            assertEquals(retour, rp.getRetour());
        }
    }
}
