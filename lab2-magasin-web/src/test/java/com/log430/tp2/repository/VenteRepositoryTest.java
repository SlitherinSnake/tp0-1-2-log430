package com.log430.tp2.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;

import com.log430.tp2.model.Employe;
import com.log430.tp2.model.Magasin;
import com.log430.tp2.model.Produit;
import com.log430.tp2.model.Vente;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class VenteRepositoryTest {

    @Autowired
    private VenteRepository venteRepository;
    
    @Autowired
    private EmployeRepository employeRepository;
    
    @Autowired
    private MagasinRepository magasinRepository;
    
    @Autowired
    private ProduitRepository produitRepository;
    
    private Employe employe;
    private Magasin magasin1;
    private Magasin magasin2;
    private Produit produit1;
    private Produit produit2;
    
    @BeforeEach
    public void setUp() {
        // Create and save test data
        employe = new Employe("John Doe", "JD001");
        employeRepository.save(employe);
        
        magasin1 = new Magasin();
        magasin1.setNom("Magasin A");
        magasin1.setQuartier("Quartier A");
        magasinRepository.save(magasin1);
        
        magasin2 = new Magasin();
        magasin2.setNom("Magasin B");
        magasin2.setQuartier("Quartier B");
        magasinRepository.save(magasin2);
        
        produit1 = new Produit("Laptop", "Électronique", 999.99f, 10);
        produitRepository.save(produit1);
        
        produit2 = new Produit("Smartphone", "Électronique", 599.99f, 20);
        produitRepository.save(produit2);
    }
    
    @Test
    public void testCreateVente() {
        // Create a new sale
        Vente vente = new Vente();
        vente.setDateVente(LocalDate.now());
        vente.setEmploye(employe);
        vente.setMagasin(magasin1);
        vente.setMontantTotal(1999.98);
        
        // Add products to the sale
        vente.ajouterProduit(produit1, 2);
        
        // Save the sale
        Vente savedVente = venteRepository.save(vente);
        
        // Verify sale was created with an ID
        assertNotNull(savedVente.getId());
        assertTrue(savedVente.getId() > 0);
    }
    
    @Test
    public void testReadVente() {
        // Create and save a sale
        Vente vente = new Vente();
        vente.setDateVente(LocalDate.now());
        vente.setEmploye(employe);
        vente.setMagasin(magasin1);
        vente.setMontantTotal(1999.98);
        vente.ajouterProduit(produit1, 2);
        
        Vente savedVente = venteRepository.save(vente);
        
        // Read the sale by ID
        Optional<Vente> foundVente = venteRepository.findById(savedVente.getId());
        
        // Verify sale was found and has correct attributes
        assertTrue(foundVente.isPresent());
        assertEquals(employe.getId(), foundVente.get().getEmploye().getId());
        assertEquals(magasin1.getId(), foundVente.get().getMagasin().getId());
        assertEquals(1999.98, foundVente.get().getMontantTotal(), 0.01);
        assertEquals(1, foundVente.get().getVenteProduits().size());
    }
    
    @Test
    public void testUpdateVente() {
        // Create and save a sale
        Vente vente = new Vente();
        vente.setDateVente(LocalDate.now());
        vente.setEmploye(employe);
        vente.setMagasin(magasin1);
        vente.setMontantTotal(1999.98);
        vente.ajouterProduit(produit1, 2);
        
        Vente savedVente = venteRepository.save(vente);
        
        // Update the sale
        savedVente.setMagasin(magasin2);
        savedVente.setMontantTotal(2599.97);
        savedVente.ajouterProduit(produit2, 1);
        
        Vente updatedVente = venteRepository.save(savedVente);
        
        // Verify sale was updated
        assertEquals(magasin2.getId(), updatedVente.getMagasin().getId());
        assertEquals(2599.97, updatedVente.getMontantTotal(), 0.01);
        assertEquals(2, updatedVente.getVenteProduits().size());
    }
    
    @Test
    public void testDeleteVente() {
        // Create and save a sale
        Vente vente = new Vente();
        vente.setDateVente(LocalDate.now());
        vente.setEmploye(employe);
        vente.setMagasin(magasin1);
        vente.setMontantTotal(1999.98);
        vente.ajouterProduit(produit1, 2);
        
        Vente savedVente = venteRepository.save(vente);
        
        // Delete the sale
        venteRepository.deleteById(savedVente.getId());
        
        // Verify sale was deleted
        Optional<Vente> deletedVente = venteRepository.findById(savedVente.getId());
        assertFalse(deletedVente.isPresent());
    }
    
    @Test
    public void testTotalVentesParMagasin() {
        // Create and save sales for different stores
        // Sale for Magasin A
        Vente vente1 = new Vente();
        vente1.setDateVente(LocalDate.now());
        vente1.setEmploye(employe);
        vente1.setMagasin(magasin1);
        vente1.setMontantTotal(1999.98);
        venteRepository.save(vente1);
        
        // Another sale for Magasin A
        Vente vente2 = new Vente();
        vente2.setDateVente(LocalDate.now());
        vente2.setEmploye(employe);
        vente2.setMagasin(magasin1);
        vente2.setMontantTotal(599.99);
        venteRepository.save(vente2);
        
        // Sale for Magasin B
        Vente vente3 = new Vente();
        vente3.setDateVente(LocalDate.now());
        vente3.setEmploye(employe);
        vente3.setMagasin(magasin2);
        vente3.setMontantTotal(999.99);
        venteRepository.save(vente3);
        
        // Get total sales by store
        List<Object[]> totalVentes = venteRepository.totalVentesParMagasin();
        
        // Verify results
        assertEquals(2, totalVentes.size());
        
        // Find Magasin A's total
        Double totalMagasinA = null;
        Double totalMagasinB = null;
        
        for (Object[] row : totalVentes) {
            String magasinNom = (String) row[0];
            Double total = (Double) row[1];
            
            if (magasinNom.equals("Magasin A")) {
                totalMagasinA = total;
            } else if (magasinNom.equals("Magasin B")) {
                totalMagasinB = total;
            }
        }
        
        // Expected totals: Magasin A = 1999.98 + 599.99 = 2599.97, Magasin B = 999.99
        assertNotNull(totalMagasinA);
        assertNotNull(totalMagasinB);
        assertEquals(2599.97, totalMagasinA, 0.01);
        assertEquals(999.99, totalMagasinB, 0.01);
    }
    
    @Test
    public void testFindByDateVenteAfterOrderByDateVenteAsc() {
        // Create sales with different dates
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate lastWeek = today.minusDays(7);
        
        // Sale from last week
        Vente vente1 = new Vente();
        vente1.setDateVente(lastWeek);
        vente1.setEmploye(employe);
        vente1.setMagasin(magasin1);
        vente1.setMontantTotal(1999.98);
        venteRepository.save(vente1);
        
        // Sale from yesterday
        Vente vente2 = new Vente();
        vente2.setDateVente(yesterday);
        vente2.setEmploye(employe);
        vente2.setMagasin(magasin1);
        vente2.setMontantTotal(599.99);
        venteRepository.save(vente2);
        
        // Sale from today
        Vente vente3 = new Vente();
        vente3.setDateVente(today);
        vente3.setEmploye(employe);
        vente3.setMagasin(magasin2);
        vente3.setMontantTotal(999.99);
        venteRepository.save(vente3);
        
        // Find sales after 3 days ago
        LocalDate threeDaysAgo = today.minusDays(3);
        List<Vente> recentSales = venteRepository.findByDateVenteAfterOrderByDateVenteAsc(threeDaysAgo);
        
        // Verify results
        assertEquals(2, recentSales.size());
        assertEquals(yesterday, recentSales.get(0).getDateVente());
        assertEquals(today, recentSales.get(1).getDateVente());
    }
} 