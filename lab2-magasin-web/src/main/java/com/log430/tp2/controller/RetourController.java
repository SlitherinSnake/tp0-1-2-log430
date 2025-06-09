package com.log430.tp2.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.log430.tp2.model.*;
import com.log430.tp2.repository.*;

@Controller
@RequestMapping("/retour")
public class RetourController {

    @Autowired
    private RetourRepository retourRepository;
    @Autowired
    private VenteRepository venteRepository;
    @Autowired
    private ProduitRepository produitRepository;

    @GetMapping
    public String redirectionVersFormulaire() {
        return "redirect:/retour/nouveau";
    }

    // Affiche la page de sélection de la vente à retourner
    @GetMapping("/nouveau")
    public String formulaireRetour(@RequestParam(required = false) Integer venteId, Model model) {
        List<Vente> ventes = venteRepository.findAll();
        // Supprimer les ventes où tous les produits ont été retournés
        List<Vente> ventesFiltrees = ventes.stream()
                .filter(v -> {
                    List<Object[]> retournees = retourRepository.findQuantitesRetourneesPourVente(v.getId());
                    Map<Integer, Integer> quantitesRetournees = new HashMap<>();
                    for (Object[] obj : retournees) {
                        Integer produitId = (Integer) obj[0];
                        Long qte = (Long) obj[1];
                        quantitesRetournees.put(produitId, qte.intValue());
                    }

                    return v.getVenteProduits().stream()
                            .anyMatch(vp -> {
                                int dejaRetourne = quantitesRetournees.getOrDefault(vp.getProduit().getId(), 0);
                                return vp.getQuantite() > dejaRetourne;
                            });
                })
                .toList();

        model.addAttribute("ventes", ventesFiltrees);

        if (venteId != null) {
            Vente vente = venteRepository.findById(venteId).orElse(null);
            if (vente != null) {
                // Quantités déjà retournées pour chaque produit
                List<Object[]> retournees = retourRepository.findQuantitesRetourneesPourVente(venteId);
                Map<Integer, Integer> quantitesRetournees = new HashMap<>();
                for (Object[] obj : retournees) {
                    Integer produitId = (Integer) obj[0];
                    Long qte = (Long) obj[1];
                    quantitesRetournees.put(produitId, qte.intValue());
                }

                // Calculer les quantités restantes
                for (VenteProduit vp : vente.getVenteProduits()) {
                    int dejaRetourne = quantitesRetournees.getOrDefault(vp.getProduit().getId(), 0);
                    vp.setQuantite(vp.getQuantite() - dejaRetourne);
                }

                // Garder seulement ceux avec quantité > 0
                List<VenteProduit> restants = vente.getVenteProduits().stream()
                        .filter(vp -> vp.getQuantite() > 0)
                        .toList();

                vente.setVenteProduits(restants);
                model.addAttribute("vente", vente);
            }
        }

        return "retour-form";
    }

    // Traite le retour soumis
    @PostMapping("/valider")
    public String validerRetour(@RequestParam int venteId,
            @RequestParam List<Integer> produitIds,
            @RequestParam List<Integer> quantites,
            Model model) {
        // Vérifie s’il y a au moins un produit avec une quantité > 0
        boolean hasValidQuantite = quantites.stream().anyMatch(qte -> qte != null && qte > 0);

        if (!hasValidQuantite) {
            // Recharge la page avec message d'erreur
            model.addAttribute("ventes", venteRepository.findAll());
            model.addAttribute("vente", venteRepository.findById(venteId).orElse(null));
            model.addAttribute("retourErreur", true);
            return "retour-form";
        }

        // Traitement normal
        Vente vente = venteRepository.findById(venteId).orElseThrow();
        Employe employe = vente.getEmploye();

        Retour retour = new Retour();
        retour.setDateRetour(LocalDate.now());
        retour.setVente(vente);
        retour.setEmploye(employe);

        List<RetourProduit> retourProduits = new ArrayList<>();
        for (int i = 0; i < produitIds.size(); i++) {
            int prodId = produitIds.get(i);
            int qte = quantites.get(i);

            if (qte <= 0)
                continue;

            Produit produit = produitRepository.findById(prodId).orElseThrow();
            produit.setQuantite(produit.getQuantite() + qte); // mise à jour du stock
            produitRepository.save(produit);

            RetourProduit rp = new RetourProduit();
            rp.setProduit(produit);
            rp.setQuantite(qte);
            rp.setRetour(retour);
            retourProduits.add(rp);
        }

        retour.setProduitsRetournes(retourProduits);
        retourRepository.save(retour);
        return "redirect:/"; // retour acceuil
    }

}
