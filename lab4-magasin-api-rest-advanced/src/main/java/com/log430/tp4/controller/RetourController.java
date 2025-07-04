package com.log430.tp4.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.log430.tp4.model.*;
import com.log430.tp4.repository.*;

// Indique que cette classe est un contrôleur Spring MVC, responsable de gérer les requêtes web.
// Les méthodes à l’intérieur renvoient généralement vers des vues Thymeleaf (HTML).
@Controller
// Définit le préfixe commun "/retour" pour toutes les URL gérées par ce
// contrôleur.
// Par exemple, /retour/nouveau, /retour/valider…
// Permet d’organiser les routes associées aux retours produits.
@RequestMapping("/retour")
public class RetourController {

    // Injecte automatiquement une instance du composant (Repository, Service, etc.)
    // correspondant.
    // Permet d’éviter d’écrire un constructeur ou un setter manuellement.
    @Autowired
    private RetourRepository retourRepository; // Accès aux retours
    @Autowired
    private VenteRepository venteRepository; // Accès aux ventes
    @Autowired
    private ProduitRepository produitRepository; // Accès aux produits
    @Autowired
    private StockMagasinRepository stockMagasinRepository;

    /**
     * Redirige automatiquement vers le formulaire de retour.
     * Appelé si l'utilisateur accède directement à /retour
     */
    @GetMapping
    public String redirectionVersFormulaire() {
        return "redirect:/retour/nouveau";
    }

    /**
     * Affiche le formulaire de retour pour choisir une vente.
     * Si une vente est sélectionnée, on affiche les produits encore retournables.
     */
    @GetMapping("/nouveau")
    public String formulaireRetour(@RequestParam(required = false) Integer venteId, Model model) {

        List<Vente> ventes = venteRepository.findAll();

        // On filtre pour ne garder que les ventes où il reste des produits retournables
        List<Vente> ventesFiltrees = ventes.stream()
                .filter(v -> {
                    // Quantités déjà retournées pour cette vente
                    List<Object[]> retournees = retourRepository.findQuantitesRetourneesPourVente(v.getId());
                    Map<Integer, Integer> quantitesRetournees = new HashMap<>();
                    for (Object[] obj : retournees) {
                        Integer produitId = (Integer) obj[0];
                        Long qte = (Long) obj[1];
                        quantitesRetournees.put(produitId, qte.intValue());
                    }

                    // On garde la vente si au moins un produit n'a pas été entièrement retourné
                    return v.getVenteProduits().stream()
                            .anyMatch(vp -> {
                                int dejaRetourne = quantitesRetournees.getOrDefault(vp.getProduit().getId(), 0);
                                return vp.getQuantite() > dejaRetourne;
                            });
                })
                .toList();

        model.addAttribute("ventes", ventesFiltrees);

        // Si une vente spécifique est sélectionnée, on l'affiche avec les quantités
        // restantes
        if (venteId != null) {
            Vente vente = venteRepository.findById(venteId).orElse(null);
            if (vente != null) {
                // Récupère les quantités déjà retournées pour chaque produit de la vente
                List<Object[]> retournees = retourRepository.findQuantitesRetourneesPourVente(venteId);
                Map<Integer, Integer> quantitesRetournees = new HashMap<>();
                for (Object[] obj : retournees) {
                    Integer produitId = (Integer) obj[0];
                    Long qte = (Long) obj[1];
                    quantitesRetournees.put(produitId, qte.intValue());
                }

                // Met à jour les quantités restantes à retourner pour chaque produit
                for (VenteProduit vp : vente.getVenteProduits()) {
                    int dejaRetourne = quantitesRetournees.getOrDefault(vp.getProduit().getId(), 0);
                    vp.setQuantite(vp.getQuantite() - dejaRetourne);
                }

                // On garde uniquement les produits ayant encore une quantité > 0 à retourner
                List<VenteProduit> restants = vente.getVenteProduits().stream()
                        .filter(vp -> vp.getQuantite() > 0)
                        .toList();

                vente.setVenteProduits(restants);
                model.addAttribute("vente", vente);
            }
        }

        return "retour-form";
    }

    /**
     * Valide le formulaire de retour soumis par l'utilisateur.
     * Met à jour le stock, enregistre les retours, puis redirige vers la page
     * d'accueil.
     */
    @PostMapping("/valider")
    public String validerRetour(@RequestParam int venteId,
            @RequestParam List<Integer> produitIds,
            @RequestParam List<Integer> quantites,
            Model model) {

        // Vérifie s'il y a au moins un produit avec une quantité > 0
        boolean hasValidQuantite = quantites.stream().anyMatch(qte -> qte != null && qte > 0);

        if (!hasValidQuantite) {
            // Si aucune quantité n’est valide, on recharge le formulaire avec un message
            // d’erreur
            model.addAttribute("ventes", venteRepository.findAll());
            model.addAttribute("vente", venteRepository.findById(venteId).orElse(null));
            model.addAttribute("retourErreur", true);
            return "retour-form";
        }

        // Traitement normal du retour
        Vente vente = venteRepository.findById(venteId).orElseThrow();
        Employe employe = vente.getEmploye(); // On réutilise l'employé associé à la vente

        Retour retour = new Retour();
        retour.setDateRetour(LocalDate.now()); // La Date actuelle du retour
        retour.setVente(vente);
        retour.setEmploye(employe);

        // Création des lignes de retour
        List<RetourProduit> retourProduits = new ArrayList<>();
        for (int i = 0; i < produitIds.size(); i++) {
            int prodId = produitIds.get(i);
            int qte = quantites.get(i);

            if (qte <= 0)
                continue; // On ignore les quantités nulles ou négatives

            // On récupère l'objet Produit pour l'identifiant donné
            Produit produit = produitRepository.findById(prodId).orElseThrow();

            // On récupère l'entrée de stock local correspondant au produit et au magasin lié à la vente
            StockMagasin stock = stockMagasinRepository
                    .findByProduitAndMagasin(produit.getId(), vente.getMagasin().getId())
                    .orElseThrow(() -> new RuntimeException("Stock introuvable"));

            // Mise à jour du stock local : on réintègre la quantité retournée
            stock.setQuantite(stock.getQuantite() + qte);
            stockMagasinRepository.save(stock); //Sauvegarde dans la base

            /*
             * Pour Stock Cenntral
             * produit.setQuantite(produit.getQuantite() + qte); // Réintègre la quantité
             * retournée
             * produitRepository.save(produit);
             */

            // On crée et associe une ligne de retour pour ce produit
            RetourProduit rp = new RetourProduit();
            rp.setProduit(produit);
            rp.setQuantite(qte);
            rp.setRetour(retour);
            retourProduits.add(rp);
        }

        // Association des produits au retour principal
        retour.setProduitsRetournes(retourProduits);
        retourRepository.save(retour);// Sauvegarde complète en cascade

        return "redirect:/"; // retour acceuil
    }

}
