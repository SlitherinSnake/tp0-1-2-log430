package com.log430.tp2.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class VenteTest {

    private Vente vente;
    private Employe employe;
    private Magasin magasin;
    private Produit produit1;
    private Produit produit2;

    @BeforeEach
    public void setUp() {
        // Create test data
        employe = new Employe("John Doe", "JD001");
        magasin = new Magasin();
        magasin.setNom("Magasin Test");
        magasin.setQuartier("Quartier Test");

        produit1 = new Produit("Laptop", "Électronique", 999.99f, 10);
        produit1.setId(1);
        produit2 = new Produit("Smartphone", "Électronique", 599.99f, 20);
        produit2.setId(2);

        // Create a new sale
        vente = new Vente();
        vente.setDateVente(LocalDate.now());
        vente.setEmploye(employe);
        vente.setMagasin(magasin);
    }

    @Test
    public void testAjouterProduit() {
        // Test adding a product to the sale
        vente.ajouterProduit(produit1, 2);
        
        // Verify product was added
        List<VenteProduit> items = vente.getItems();
        assertEquals(1, items.size());
        assertEquals(produit1.getId(), items.get(0).getProduit().getId());
        assertEquals(2, items.get(0).getQuantite());
        
        // Test adding the same product again (should update quantity)
        vente.ajouterProduit(produit1, 3);
        
        // Verify quantity was updated
        items = vente.getItems();
        assertEquals(1, items.size());
        assertEquals(5, items.get(0).getQuantite());
        
        // Test adding a different product
        vente.ajouterProduit(produit2, 1);
        
        // Verify second product was added
        items = vente.getItems();
        assertEquals(2, items.size());
        assertEquals(1, items.get(1).getQuantite());
    }

    @Test
    public void testRemoveProduit() {
        // Add products to the sale
        vente.ajouterProduit(produit1, 2);
        vente.ajouterProduit(produit2, 1);
        
        // Verify products were added
        assertEquals(2, vente.getItems().size());
        
        // Remove a product
        vente.removeProduit(produit1.getId());
        
        // Verify product was removed
        List<VenteProduit> items = vente.getItems();
        assertEquals(1, items.size());
        assertEquals(produit2.getId(), items.get(0).getProduit().getId());
    }

    @Test
    public void testCalculerMontantTotal() {
        // Add products to the sale
        vente.ajouterProduit(produit1, 2); // 2 * 999.99 = 1999.98
        vente.ajouterProduit(produit2, 3); // 3 * 599.99 = 1799.97
        
        // Calculate total amount
        vente.calculerMontantTotal();
        
        // Expected total: 1999.98 + 1799.97 = 3799.95
        // Using delta for float comparison
        assertEquals(3799.95, vente.getMontantTotal(), 0.01);
    }

    @Test
    public void testSetDateVenteIfNull() {
        // Create a sale with null date
        Vente venteWithNullDate = new Vente();
        assertNull(venteWithNullDate.getDateVente());
        
        // Set date if null
        venteWithNullDate.setDateVenteIfNull();
        
        // Verify date was set to today
        assertNotNull(venteWithNullDate.getDateVente());
        assertEquals(LocalDate.now(), venteWithNullDate.getDateVente());
        
        // Create a sale with a specific date
        LocalDate specificDate = LocalDate.of(2023, 1, 1);
        Vente venteWithDate = new Vente();
        venteWithDate.setDateVente(specificDate);
        
        // Set date if null (should not change)
        venteWithDate.setDateVenteIfNull();
        
        // Verify date was not changed
        assertEquals(specificDate, venteWithDate.getDateVente());
    }

    @Test
    public void testConstructorAndGetters() {
        // Create a sale using constructor with parameters
        List<VenteProduit> venteProduits = new ArrayList<>();
        VenteProduit venteProduit = new VenteProduit(null, produit1, 2);
        venteProduits.add(venteProduit);
        
        LocalDate today = LocalDate.now();
        Double montantTotal = 1999.98;
        
        Vente venteWithParams = new Vente(today, montantTotal, employe, venteProduits);
        
        // Verify attributes
        assertEquals(today, venteWithParams.getDateVente());
        assertEquals(montantTotal, venteWithParams.getMontantTotal());
        assertEquals(employe, venteWithParams.getEmploye());
        assertEquals(venteProduits, venteWithParams.getVenteProduits());
    }

    @Test
    public void testSetters() {
        // Test setters
        LocalDate date = LocalDate.of(2023, 1, 1);
        Double montant = 1000.0;
        List<VenteProduit> venteProduits = new ArrayList<>();
        
        vente.setId(1);
        vente.setDateVente(date);
        vente.setMontantTotal(montant);
        vente.setVenteProduits(venteProduits);
        
        // Verify attributes
        assertEquals(1, vente.getId());
        assertEquals(date, vente.getDateVente());
        assertEquals(montant, vente.getMontantTotal());
        assertEquals(venteProduits, vente.getVenteProduits());
    }
} 