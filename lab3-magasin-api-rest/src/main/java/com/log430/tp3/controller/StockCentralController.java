package com.log430.tp3.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.log430.tp3.model.*;
import com.log430.tp3.repository.MagasinRepository;
import com.log430.tp3.repository.StockMagasinRepository;
import com.log430.tp3.service.StockCentralService;

// Indique que cette classe est un contrôleur Spring MVC, responsable de gérer les requêtes web.
// Les méthodes à l’intérieur renvoient généralement vers des vues Thymeleaf (HTML).
@Controller
// Toutes les routes de ce contrôleur commencent par "/stock".
// Par exemple : "/stock", "/stock/demander"…
// Cela permet de regrouper logiquement les fonctionnalités liées au stock.
@RequestMapping("/stock")
// Garde "selectedMagasinId" en session pour le réutiliser entre les requêtes 
@SessionAttributes("selectedMagasinId")
public class StockCentralController {

    // Injecte automatiquement une instance du composant (Repository, Service, etc.)
    // correspondant.
    // Permet d’éviter d’écrire un constructeur ou un setter manuellement.
    @Autowired
    private StockCentralService stockService; // Accès aux stock
    @Autowired
    private MagasinRepository magasinRepository; // Accès aux magasin
    @Autowired
    private StockMagasinRepository stockMagasinRepository; // Accès au stock des magasins

    // Rend la liste complète des magasins disponible dans le modèle pour toutes les vues.
    // Cela permet d’afficher dynamiquement les magasins dans un menu déroulant ou une sélection.
    @ModelAttribute("allMagasins")
    public List<Magasin> getAllMagasins() {
        return magasinRepository.findAll();
    }

    /**
     * Affiche la page du stock central, avec tous les produits disponibles.
     * Permet à l’employé de faire une demande de réapprovisionnement.
     * Paramètre optionnel : magasinId (pour associer la demande à un magasin
     * précis).
     */
    @GetMapping
    public String afficherStockCentral(@RequestParam(required = false) Integer magasinId, @ModelAttribute("selectedMagasinId") Integer selectedMagasinId, Model model) {
        List<Produit> produits = stockService.getProduitsDisponibles();
        model.addAttribute("produits", produits);
        model.addAttribute("magasins", magasinRepository.findAll());
        
        // If magasinId is not provided, use the selectedMagasinId from session
        if (magasinId == null) {
            magasinId = selectedMagasinId;
        }
        
        model.addAttribute("magasinId", magasinId); // conserve le filtre sélectionné

        // Pour activer l’historique dans la vue
        if (magasinId != null) {
            model.addAttribute("demandes", stockService.getDemandesParMagasin(magasinId));

            // Prépare le nom du magasin
            magasinRepository.findById(magasinId).ifPresent(m -> model.addAttribute("nomMagasin", m.getNom()));

            // Ajouter le stock local
            List<StockMagasin> stockLocal = stockMagasinRepository.findByMagasinId(magasinId);
            model.addAttribute("stockLocal", stockLocal);
        }

        // Ajoute le magasin choisi
        Magasin selectedMagasin = magasinRepository.findById(selectedMagasinId).orElse(null);
        model.addAttribute("selectedMagasin", selectedMagasin);

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
