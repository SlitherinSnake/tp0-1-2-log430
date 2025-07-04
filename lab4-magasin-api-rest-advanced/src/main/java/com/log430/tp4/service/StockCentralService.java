package com.log430.tp4.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.log430.tp4.model.*;
import com.log430.tp4.repository.*;

@Service
public class StockCentralService {

    @Autowired
    private StockCentralRepository stockRepository;
    @Autowired
    private ProduitRepository produitRepository;
    @Autowired
    private MagasinRepository magasinRepository;

    /**
     * Retourne la liste complète des produits du stock central.
     * Utilisé par les employés pour consulter les produits disponibles.
     * Permet de planifier un réapprovisionnement si besoin.
     */
    public List<Produit> getProduitsDisponibles() {
        return produitRepository.findAll(); // SELECT * FROM produits
    }

    /**
     * Enregistre une demande de réapprovisionnement pour un produit spécifique par
     * un magasin donné, avec une quantité précise.
     * Protection : bloque les doublons si une demande pour ce produit et ce magasin
     * a déjà été faite aujourd’hui.
     * 
     * @return true si la demande est bien enregistrée, false si elle est ignorée
     *         (déjà faite).
     */
    public boolean demanderReapprovisionnement(int produitId, int magasinId, int quantite) {
        // Vérifie si une demande identique a déjà été faite aujourd’hui
        boolean existeDeja = !stockRepository
                .findDemandesDuJour(produitId, magasinId)
                .isEmpty();

        if (existeDeja) {
            return false; // Demande ignorée
        }

        // Récupération des entités associées
        Produit produit = produitRepository.findById(produitId).orElse(null);
        Magasin magasin = magasinRepository.findById(magasinId).orElse(null);

        if (produit == null || magasin == null) {
            return false; // Données invalides
        }

        // Création et sauvegarde de la demande
        StockCentral demande = new StockCentral(produit, magasin, quantite, LocalDate.now());
        stockRepository.save(demande);
        return true;
    }

    /**
     * Récupère l’historique des demandes de stock faites par un magasin donné.
     * Les plus récentes sont affichées en premier.
     */
    public List<StockCentral> getDemandesParMagasin(int magasinId) {
        return stockRepository.findByMagasin(magasinId);
    }

    /**
     * Récupère l’historique des demandes effectuées pour un produit spécifique.
     * Utile pour voir quels magasins en ont demandé récemment.
     */
    public List<StockCentral> getDemandesParProduit(int produitId) {
        return stockRepository.findByProduit(produitId);
    }
}
