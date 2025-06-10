package com.log430.tp2.controller;

import com.log430.tp2.model.*;
import com.log430.tp2.service.StockService;
import com.log430.tp2.repository.MagasinRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Indique que cette classe est un contrôleur Spring MVC, responsable de gérer les requêtes web.
// Les méthodes à l’intérieur renvoient généralement vers des vues Thymeleaf (HTML).
@Controller
// Toutes les routes de ce contrôleur commencent par "/stock".
// Par exemple : "/stock", "/stock/demander"…
// Cela permet de regrouper logiquement les fonctionnalités liées au stock.
@RequestMapping("/stock")
public class StockController {

    // Injecte automatiquement une instance du composant (Repository, Service, etc.)
    // correspondant.
    // Permet d’éviter d’écrire un constructeur ou un setter manuellement.
    @Autowired
    private StockService stockService;
    @Autowired
    private MagasinRepository magasinRepository;

    /**
     * Affiche la page du stock central, avec tous les produits disponibles.
     * Permet à l’employé de faire une demande de réapprovisionnement.
     * Paramètre optionnel : magasinId (pour associer la demande à un magasin
     * précis).
     */
    @GetMapping
    public String afficherStockCentral(@RequestParam(required = false) Integer magasinId, Model model) {
        List<Produit> produits = stockService.getProduitsDisponibles();
        model.addAttribute("produits", produits);
        model.addAttribute("magasins", magasinRepository.findAll());
        model.addAttribute("magasinId", magasinId); // conserve le filtre sélectionné

        // Pour activer l’historique dans la vue
        if (magasinId != null) {
            model.addAttribute("demandes", stockService.getDemandesParMagasin(magasinId));
        }

        return "stock"; // → templates/stock.html
    }

    /**
     * Permet de soumettre une demande de réapprovisionnement depuis l’interface.
     * Protection contre les doublons dans la journée.
     */
    @PostMapping("/demander")
    public String demanderReapprovisionnement(
            @RequestParam int produitId,
            @RequestParam int magasinId,
            @RequestParam int quantite,
            Model model) {
        boolean success = stockService.demanderReapprovisionnement(produitId, magasinId, quantite);

        // Redirige avec un paramètre de succès ou d’échec
        return "redirect:/stock?magasinId=" + magasinId + (success ? "&success" : "&duplicate");
    }
}
