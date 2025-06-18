package com.log430.tp3.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.log430.tp3.model.*;
import com.log430.tp3.repository.*;

@Service
public class RapportService {

    @Autowired
    private VenteRepository venteRepository;

    @Autowired
    private ProduitRepository produitRepository;

    //@Autowired
    //private MagasinRepository magasinRepository; // Pour futur utilisation

    /**
     * Regroupe les ventes par magasin avec leur montant total.
     * Exemple : {"Magasin A" -> 1200.50, "Magasin B" -> 850.75}
     * Utile pour le rapport consolidé et les décisions stratégiques.
     */
    public Map<String, Double> ventesParMagasin() {
        List<Vente> ventes = venteRepository.findAll();
        
        // On regroupe les ventes par nom de magasin, et on somme les montants de chaque groupe.
        return ventes.stream()
                .collect(Collectors.groupingBy(
                        v -> v.getMagasin().getNom(), // clé = nom du magasin
                        Collectors.summingDouble(Vente::getMontantTotal) // valeur = total des montants
                ));
    }

    /**
     * Même logique que ventesParMagasin(), mais limitée à un magasin spécifique.
     * Filtrage par ID avant de regrouper.
     */
    public Map<String, Double> ventesParMagasin(Integer magasinId) {
        return venteRepository.findAll().stream()
                .filter(v -> v.getMagasin() != null) // protection contre les données corrompues
                .collect(Collectors.groupingBy(
                        v -> v.getMagasin().getNom(),
                        Collectors.summingDouble(Vente::getMontantTotal)));
    }

    /**
     * Retourne la liste des produits les plus vendus (du plus au moins vendu).
     * Chaque entrée de la liste contient un produit et la quantité totale vendue.
     * Exemple : [ (Produit A, 30), (Produit B, 22), ... ]
     * Cela permet d’identifier les articles populaires pour le réapprovisionnement
     * ou les promotions.
     */
    public List<Map.Entry<Produit, Integer>> produitsLesPlusVendus() {
        List<Vente> ventes = venteRepository.findAll();
        Map<Produit, Integer> ventesParProduit = new HashMap<>();

        for (Vente vente : ventes) {
            for (VenteProduit vp : vente.getVenteProduits()) {
                // On accumule la quantité vendue pour chaque produit.
                ventesParProduit.merge(vp.getProduit(), vp.getQuantite(), Integer::sum);
            }
        }

        // On trie ensuite les produits du plus vendu au moins vendu.
        return ventesParProduit.entrySet()
                .stream()
                .sorted((e1, e2) -> e2.getValue() - e1.getValue()) // tri décroissant
                .collect(Collectors.toList());
    }

    /**
     * Variante de produitsLesPlusVendus() limitée à un seul magasin.
     * Filtrage préalable par ID du magasin avant l’agrégation.
     */
    public List<Map.Entry<Produit, Integer>> produitsLesPlusVendus(Integer magasinId) {
        Map<Produit, Integer> ventesParProduit = new HashMap<>();

        // On filtre les ventes du magasin sélectionné et on accumule les quantités
        venteRepository.findAll().stream()
                .filter(v -> v.getMagasin().getId() == magasinId)
                .flatMap(v -> v.getVenteProduits().stream())
                .forEach(vp -> ventesParProduit.merge(vp.getProduit(), vp.getQuantite(), Integer::sum));

        // Tri décroissant selon la quantité totale vendue
        return ventesParProduit.entrySet()
                .stream()
                .sorted((e1, e2) -> e2.getValue() - e1.getValue())
                .collect(Collectors.toList());
    }

    /**
     * Retourne la liste de tous les produits avec leur stock actuel (quantité
     * disponible).
     * Ce stock est lu directement depuis la base de données.
     * Permet de visualiser quels produits sont en rupture ou en surstock.
     */
    public List<Produit> stockActuel() {
        return produitRepository.findAll(); // Equivalent à : SELECT * FROM produits dans SQL
    }
}
