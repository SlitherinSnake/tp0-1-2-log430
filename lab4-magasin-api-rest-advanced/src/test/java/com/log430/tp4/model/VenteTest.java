package com.log430.tp4.model;

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
        // Création des données de test
        employe = new Employe("John Doe", "JD001");
        magasin = new Magasin();
        magasin.setNom("Magasin Test");
        magasin.setQuartier("Quartier Test");

        produit1 = new Produit("Laptop", "Électronique", 999.99f, 10);
        produit1.setId(1);
        produit2 = new Produit("Smartphone", "Électronique", 599.99f, 20);
        produit2.setId(2);

        // Création d’une nouvelle vente
        vente = new Vente();
        vente.setDateVente(LocalDate.now());
        vente.setEmploye(employe);
        vente.setMagasin(magasin);
    }

    @Test
    public void testAjouterProduit() {
        // Ajouter un produit à la vente
        vente.ajouterProduit(produit1, 2);

        // Vérifier que le produit a été ajouté
        List<VenteProduit> items = vente.getItems();
        assertEquals(1, items.size());
        assertEquals(produit1.getId(), items.get(0).getProduit().getId());
        assertEquals(2, items.get(0).getQuantite());

        // Ajouter à nouveau le même produit (devrait mettre à jour la quantité)
        vente.ajouterProduit(produit1, 3);

        // Vérifier que la quantité a été mise à jour
        items = vente.getItems();
        assertEquals(1, items.size());
        assertEquals(5, items.get(0).getQuantite());

        // Ajouter un autre produit
        vente.ajouterProduit(produit2, 1);

        // Vérifier que le second produit a été ajouté
        items = vente.getItems();
        assertEquals(2, items.size());
        assertEquals(1, items.get(1).getQuantite());
    }

    @Test
    public void testRemoveProduit() {
        // Ajouter des produits à la vente
        vente.ajouterProduit(produit1, 2);
        vente.ajouterProduit(produit2, 1);

        // Vérifier qu’ils ont bien été ajoutés
        assertEquals(2, vente.getItems().size());

        // Retirer un produit
        vente.removeProduit(produit1.getId());

        // Vérifier que le produit a été retiré
        List<VenteProduit> items = vente.getItems();
        assertEquals(1, items.size());
        assertEquals(produit2.getId(), items.get(0).getProduit().getId());
    }

    @Test
    public void testCalculerMontantTotal() {
        // Ajouter des produits à la vente
        vente.ajouterProduit(produit1, 2); // 2 * 999.99 = 1999.98
        vente.ajouterProduit(produit2, 3); // 3 * 599.99 = 1799.97

        // Calculer le montant total
        vente.calculerMontantTotal();

        // Total attendu : 1999.98 + 1799.97 = 3799.95
        // Utilisation d’un delta pour la comparaison des floats
        assertEquals(3799.95, vente.getMontantTotal(), 0.01);
    }

    @Test
    public void testSetDateVenteIfNull() {
        // Vente sans date (null)
        Vente venteWithNullDate = new Vente();
        assertNull(venteWithNullDate.getDateVente());

        // Fixer la date si elle est nulle
        venteWithNullDate.setDateVenteIfNull();

        // Vérifier que la date a été fixée à aujourd’hui
        assertNotNull(venteWithNullDate.getDateVente());
        assertEquals(LocalDate.now(), venteWithNullDate.getDateVente());

        // Vente avec une date spécifique
        LocalDate specificDate = LocalDate.of(2023, 1, 1);
        Vente venteWithDate = new Vente();
        venteWithDate.setDateVente(specificDate);

        // Tenter de fixer la date alors qu’elle existe déjà (ne doit rien changer)
        venteWithDate.setDateVenteIfNull();

        // Vérifier que la date n’a pas été modifiée
        assertEquals(specificDate, venteWithDate.getDateVente());
    }

    @Test
    public void testConstructorAndGetters() {
        // Créer une vente via le constructeur paramétré
        List<VenteProduit> venteProduits = new ArrayList<>();
        VenteProduit venteProduit = new VenteProduit(null, produit1, 2);
        venteProduits.add(venteProduit);

        LocalDate today = LocalDate.now();
        Double montantTotal = 1999.98;

        Vente venteWithParams = new Vente(today, montantTotal, employe, venteProduits);

        // Vérifier les attributs
        assertEquals(today, venteWithParams.getDateVente());
        assertEquals(montantTotal, venteWithParams.getMontantTotal());
        assertEquals(employe, venteWithParams.getEmploye());
        assertEquals(venteProduits, venteWithParams.getVenteProduits());
    }

    @Test
    public void testSetters() {
        // Tester les setters
        LocalDate date = LocalDate.of(2023, 1, 1);
        Double montant = 1000.0;
        List<VenteProduit> venteProduits = new ArrayList<>();

        vente.setId(1);
        vente.setDateVente(date);
        vente.setMontantTotal(montant);
        vente.setVenteProduits(venteProduits);

        // Vérifier les attributs
        assertEquals(1, vente.getId());
        assertEquals(date, vente.getDateVente());
        assertEquals(montant, vente.getMontantTotal());
        assertEquals(venteProduits, vente.getVenteProduits());
    }
}