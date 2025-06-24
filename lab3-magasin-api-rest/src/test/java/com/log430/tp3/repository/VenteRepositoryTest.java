package com.log430.tp3.repository;

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

import com.log430.tp3.model.Employe;
import com.log430.tp3.model.Magasin;
import com.log430.tp3.model.Produit;
import com.log430.tp3.model.Vente;

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
        // Création / sauvegarde des données de test
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
        // Créer une nouvelle vente
        Vente vente = new Vente();
        vente.setDateVente(LocalDate.now());
        vente.setEmploye(employe);
        vente.setMagasin(magasin1);
        vente.setMontantTotal(1999.98);
        
        // Ajouter des produits à la vente
        vente.ajouterProduit(produit1, 2);
        
        // Sauvegarder la vente
        Vente savedVente = venteRepository.save(vente);
        
        // Vérifier que la vente a bien un ID
        assertNotNull(savedVente.getId());
        assertTrue(savedVente.getId() > 0);
    }
    
    @Test
    public void testReadVente() {
        // Créer puis sauvegarder une vente
        Vente vente = new Vente();
        vente.setDateVente(LocalDate.now());
        vente.setEmploye(employe);
        vente.setMagasin(magasin1);
        vente.setMontantTotal(1999.98);
        vente.ajouterProduit(produit1, 2);
        
        Vente savedVente = venteRepository.save(vente);
        
        // Lire la vente par ID
        Optional<Vente> foundVente = venteRepository.findById(savedVente.getId());
        
        // Vérifier les attributs
        assertTrue(foundVente.isPresent());
        assertEquals(employe.getId(), foundVente.get().getEmploye().getId());
        assertEquals(magasin1.getId(), foundVente.get().getMagasin().getId());
        assertEquals(1999.98, foundVente.get().getMontantTotal(), 0.01);
        assertEquals(1, foundVente.get().getVenteProduits().size());
    }
    
    @Test
    public void testUpdateVente() {
        // Créer puis sauvegarder une vente
        Vente vente = new Vente();
        vente.setDateVente(LocalDate.now());
        vente.setEmploye(employe);
        vente.setMagasin(magasin1);
        vente.setMontantTotal(1999.98);
        vente.ajouterProduit(produit1, 2);
        
        Vente savedVente = venteRepository.save(vente);
        
        // Modifier la vente
        savedVente.setMagasin(magasin2);
        savedVente.setMontantTotal(2599.97);
        savedVente.ajouterProduit(produit2, 1);
        
        Vente updatedVente = venteRepository.save(savedVente);
        
        // Vérifier la mise à jour
        assertEquals(magasin2.getId(), updatedVente.getMagasin().getId());
        assertEquals(2599.97, updatedVente.getMontantTotal(), 0.01);
        assertEquals(2, updatedVente.getVenteProduits().size());
    }
    
    @Test
    public void testDeleteVente() {
        // Créer puis sauvegarder une vente
        Vente vente = new Vente();
        vente.setDateVente(LocalDate.now());
        vente.setEmploye(employe);
        vente.setMagasin(magasin1);
        vente.setMontantTotal(1999.98);
        vente.ajouterProduit(produit1, 2);
        
        Vente savedVente = venteRepository.save(vente);
        
        // Supprimer la vente
        venteRepository.deleteById(savedVente.getId());
        
        // Vérifier que la vente n'existe plus
        Optional<Vente> deletedVente = venteRepository.findById(savedVente.getId());
        assertFalse(deletedVente.isPresent());
    }
    
    @Test
    public void testTotalVentesParMagasin() {
        // Créer des ventes pour différents magasins
        // Vente pour Magasin A
        Vente vente1 = new Vente();
        vente1.setDateVente(LocalDate.now());
        vente1.setEmploye(employe);
        vente1.setMagasin(magasin1);
        vente1.setMontantTotal(1999.98);
        venteRepository.save(vente1);
        
        // Deuxième vente pour Magasin A
        Vente vente2 = new Vente();
        vente2.setDateVente(LocalDate.now());
        vente2.setEmploye(employe);
        vente2.setMagasin(magasin1);
        vente2.setMontantTotal(599.99);
        venteRepository.save(vente2);
        
        // Vente pour Magasin B
        Vente vente3 = new Vente();
        vente3.setDateVente(LocalDate.now());
        vente3.setEmploye(employe);
        vente3.setMagasin(magasin2);
        vente3.setMontantTotal(999.99);
        venteRepository.save(vente3);
        
        // Récupérer le total des ventes par magasin
        List<Object[]> totalVentes = venteRepository.totalVentesParMagasin();
        
        // Vérifier les résultats
        assertEquals(2, totalVentes.size());
        
        // Totaux attendus
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
        
        // Magasin A : 1999.98 + 599.99 = 2599.97 ; Magasin B : 999.99
        assertNotNull(totalMagasinA);
        assertNotNull(totalMagasinB);
        assertEquals(2599.97, totalMagasinA, 0.01);
        assertEquals(999.99, totalMagasinB, 0.01);
    }
    
    @Test
    public void testFindByDateVenteAfterOrderByDateVenteAsc() {
        // Créer des ventes à des dates différentes
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate lastWeek = today.minusDays(7);
        
        // Vente de la semaine dernière
        Vente vente1 = new Vente();
        vente1.setDateVente(lastWeek);
        vente1.setEmploye(employe);
        vente1.setMagasin(magasin1);
        vente1.setMontantTotal(1999.98);
        venteRepository.save(vente1);
        
        // Vente d’hier
        Vente vente2 = new Vente();
        vente2.setDateVente(yesterday);
        vente2.setEmploye(employe);
        vente2.setMagasin(magasin1);
        vente2.setMontantTotal(599.99);
        venteRepository.save(vente2);
        
        // Vente d’aujourd’hui
        Vente vente3 = new Vente();
        vente3.setDateVente(today);
        vente3.setEmploye(employe);
        vente3.setMagasin(magasin2);
        vente3.setMontantTotal(999.99);
        venteRepository.save(vente3);
        
        // Rechercher les ventes à partir d’il y a 3 jours
        LocalDate threeDaysAgo = today.minusDays(3);
        List<Vente> recentSales = venteRepository.findByDateVenteAfterOrderByDateVenteAsc(threeDaysAgo);
        
        // Vérifier l’ordre chronologique (ascendant)
        assertEquals(2, recentSales.size());
        assertEquals(yesterday, recentSales.get(0).getDateVente());
        assertEquals(today, recentSales.get(1).getDateVente());
    }
}