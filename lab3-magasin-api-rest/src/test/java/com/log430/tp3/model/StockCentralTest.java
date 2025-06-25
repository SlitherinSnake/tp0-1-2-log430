package com.log430.tp3.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

class StockCentralTest {

    private Produit produit;
    private Magasin magasin;

    @BeforeEach
    void setUp() {
        produit = new Produit("Laptop", "Électronique", 999.99f, 10);
        produit.setId(1);
        
        magasin = new Magasin();
        magasin.setId(1);
        magasin.setNom("Magasin Test");
        magasin.setQuartier("Centre-Ville");
    }

    @Test
    void testDefaultConstructor() {
        StockCentral stock = new StockCentral();
        
        assertEquals(0, stock.getId());
        assertNull(stock.getProduit());
        assertNull(stock.getMagasin());
        assertEquals(0, stock.getQuantiteDemandee());
        assertNull(stock.getDateDemande());
    }

    @Test
    void testParameterizedConstructor() {
        int quantiteDemandee = 5;
        LocalDate dateDemande = LocalDate.now();
        
        StockCentral stock = new StockCentral(produit, magasin, quantiteDemandee, dateDemande);
        
        assertEquals(0, stock.getId()); // ID not set in constructor
        assertEquals(produit, stock.getProduit());
        assertEquals(magasin, stock.getMagasin());
        assertEquals(quantiteDemandee, stock.getQuantiteDemandee());
        assertEquals(dateDemande, stock.getDateDemande());
    }

    @Test
    void testSettersAndGetters() {
        StockCentral stock = new StockCentral();
        int quantiteDemandee = 10;
        LocalDate dateDemande = LocalDate.of(2023, 1, 15);

        stock.setProduit(produit);
        stock.setMagasin(magasin);
        stock.setQuantiteDemandee(quantiteDemandee);
        stock.setDateDemande(dateDemande);

        assertEquals(produit, stock.getProduit());
        assertEquals(magasin, stock.getMagasin());
        assertEquals(quantiteDemandee, stock.getQuantiteDemandee());
        assertEquals(dateDemande, stock.getDateDemande());
    }

    @Test
    void testToStringWithValidData() {
        StockCentral stock = new StockCentral(produit, magasin, 15, LocalDate.now());
        
        String result = stock.toString();
        assertTrue(result.contains("Stock{id=0"));
        assertTrue(result.contains("produit=Laptop"));
        assertTrue(result.contains("magasin=Magasin Test"));
        assertTrue(result.contains("quantiteDemandee=15"));
    }

    @Test
    void testToStringWithNullValues() {
        StockCentral stock = new StockCentral();
        
        String result = stock.toString();
        assertTrue(result.contains("Stock{id=0"));
        assertTrue(result.contains("produit=null"));
        assertTrue(result.contains("magasin=null"));
        assertTrue(result.contains("quantiteDemandee=0"));
    }

    @Test
    void testSetNullValues() {
        StockCentral stock = new StockCentral(produit, magasin, 5, LocalDate.now());
        
        stock.setProduit(null);
        stock.setMagasin(null);
        stock.setDateDemande(null);

        assertNull(stock.getProduit());
        assertNull(stock.getMagasin());
        assertNull(stock.getDateDemande());
    }

    @Test
    void testQuantiteDemandeeEdgeCases() {
        StockCentral stock = new StockCentral();
        
        // Test zero quantity
        stock.setQuantiteDemandee(0);
        assertEquals(0, stock.getQuantiteDemandee());
        
        // Test negative quantity (might be allowed depending on business rules)
        stock.setQuantiteDemandee(-5);
        assertEquals(-5, stock.getQuantiteDemandee());
        
        // Test large quantity
        stock.setQuantiteDemandee(1000);
        assertEquals(1000, stock.getQuantiteDemandee());
    }

    @Test
    void testDateDemandeDifferentDates() {
        StockCentral stock = new StockCentral();
        
        // Test past date
        LocalDate pastDate = LocalDate.of(2020, 1, 1);
        stock.setDateDemande(pastDate);
        assertEquals(pastDate, stock.getDateDemande());
        
        // Test future date
        LocalDate futureDate = LocalDate.of(2025, 12, 31);
        stock.setDateDemande(futureDate);
        assertEquals(futureDate, stock.getDateDemande());
        
        // Test today
        LocalDate today = LocalDate.now();
        stock.setDateDemande(today);
        assertEquals(today, stock.getDateDemande());
    }
}
