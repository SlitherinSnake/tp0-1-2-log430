package com.log430.tp2.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.log430.tp2.model.*;
import com.log430.tp2.repository.*;

@Controller
@RequestMapping("/retour")
public class RetourController {

    @Autowired private RetourRepository retourRepository;
    @Autowired private VenteRepository venteRepository;
    @Autowired private ProduitRepository produitRepository;

    @GetMapping
    public String redirectionVersFormulaire() {
        return "redirect:/retour/nouveau";
    }

    // Affiche la page de sélection de la vente à retourner
    @GetMapping("/nouveau")
    public String formulaireRetour(@RequestParam(required = false) Integer venteId, Model model) {
        if (venteId != null) {
            Vente vente = venteRepository.findById(venteId).orElse(null);
            model.addAttribute("vente", vente);
        }
        model.addAttribute("ventes", venteRepository.findAll());
        return "retour-form";
    }

    // Traite le retour soumis
    @PostMapping("/valider")
    public String validerRetour(@RequestParam int venteId,
                                 @RequestParam List<Integer> produitIds,
                                 @RequestParam List<Integer> quantites) {
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
        return "redirect:/"; // Redirige à l'accueil
    }
}
