package com.log430.tp3.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class VenteProduitTest {

    @Test
    public void testConstructorAndGetters() {
        // Créer des objets de test
        Vente vente = new Vente();
        vente.setId(1);
        
        Produit produit = new Produit("Laptop", "Électronique", 999.99f, 10);
        produit.setId(1);
        
        int quantite = 2;
        
        // Créer un VenteProduit via le constructeur
        VenteProduit venteProduit = new VenteProduit(vente, produit, quantite);
        
        // Vérifier les attributs
        assertEquals(vente, venteProduit.getVente());
        assertEquals(produit, venteProduit.getProduit());
        assertEquals(quantite, venteProduit.getQuantite());
    }
    
    @Test
    public void testSetters() {
        // Créer des objets de test
        Vente vente = new Vente();
        vente.setId(1);
        
        Vente nouvelleVente = new Vente();
        nouvelleVente.setId(2);
        
        Produit produit = new Produit("Laptop", "Électronique", 999.99f, 10);
        produit.setId(1);
        
        Produit nouveauProduit = new Produit("Smartphone", "Électronique", 599.99f, 20);
        nouveauProduit.setId(2);
        
        // Créer un VenteProduit vide
        VenteProduit venteProduit = new VenteProduit();
        
        // Définir les attributs
        venteProduit.setVente(vente);
        venteProduit.setProduit(produit);
        venteProduit.setQuantite(2);
        
        // Vérifier les attributs
        assertEquals(vente, venteProduit.getVente());
        assertEquals(produit, venteProduit.getProduit());
        assertEquals(2, venteProduit.getQuantite());
        
        // Mettre à jour les attributs
        venteProduit.setVente(nouvelleVente);
        venteProduit.setProduit(nouveauProduit);
        venteProduit.setQuantite(3);
        
        // Vérifier les attributs mis à jour
        assertEquals(nouvelleVente, venteProduit.getVente());
        assertEquals(nouveauProduit, venteProduit.getProduit());
        assertEquals(3, venteProduit.getQuantite());
    }
    
    @Test
    public void testGetSousTotal() {
        // Créer des objets de test
        Produit produit = new Produit("Laptop", "Électronique", 999.99f, 10);
        
        // Créer un VenteProduit
        VenteProduit venteProduit = new VenteProduit(null, produit, 2);
        
        // Calculer le sous-total attendu : 2 * 999.99 = 1999.98
        double sousTotalAttendu = 2 * 999.99;
        
        // Vérifier le sous-total
        assertEquals(sousTotalAttendu, venteProduit.getSousTotal(), 0.01);
        
        // Mettre à jour la quantité
        venteProduit.setQuantite(3);
        
        // Recalculer le sous-total attendu : 3 * 999.99 = 2999.97
        sousTotalAttendu = 3 * 999.99;
        
        // Vérifier le sous-total mis à jour
        assertEquals(sousTotalAttendu, venteProduit.getSousTotal(), 0.01);
    }
    
    @Test
    public void testToString() {
        // Créer des objets de test
        Produit produit = new Produit("Laptop", "Électronique", 999.99f, 10);
        produit.setId(1);
        
        // Créer un VenteProduit
        VenteProduit venteProduit = new VenteProduit(null, produit, 2);
        
        // Résultat attendu de toString
        String attendu = "VenteProduit{id=0, produit=Laptop, quantite=2}";
        
        // Vérifier toString
        assertEquals(attendu, venteProduit.toString());
    }
}