package com.log430.tp2.controller;

import com.log430.tp2.repository.MagasinRepository;
import com.log430.tp2.service.RapportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Indique que cette classe est un contrôleur Spring MVC, responsable de gérer les requêtes web.
// Les méthodes à l’intérieur renvoient généralement vers des vues Thymeleaf (HTML).
@Controller
public class RapportController {

    // Injecte automatiquement une instance du composant (Repository, Service, etc.) correspondant.
    // Permet d’éviter d’écrire un constructeur ou un setter manuellement.
    @Autowired
    private RapportService rapportService;
    @Autowired
    private MagasinRepository magasinRepository;

    /**
     * Point d'accès pour l'administrateur : affiche un rapport consolidé.
     * URL : /rapport
     * Vue associée : templates/rapport.html
     * Permet de filtrer les rapports par magasin via un paramètre GET (?magasinId=).
     */
    @GetMapping("/rapport")
    public String afficherRapport(@RequestParam(required = false) Integer magasinId, Model model) {
        // Injecte la liste des magasins pour permettre le filtrage dans le <select>
        model.addAttribute("magasins", magasinRepository.findAll());

        if (magasinId != null) {
            // Filtre les données du rapport pour un magasin spécifique
            model.addAttribute("ventesParMagasin", rapportService.ventesParMagasin(magasinId));
            model.addAttribute("produitsPopulaires", rapportService.produitsLesPlusVendus(magasinId));
        } else {
            // Données globales si aucun filtre sélectionné
            model.addAttribute("ventesParMagasin", rapportService.ventesParMagasin());
            model.addAttribute("produitsPopulaires", rapportService.produitsLesPlusVendus());
        }

        // Liste complète des produits et leur stock
        model.addAttribute("stockActuel", rapportService.stockActuel());
        
        return "rapport"; // Correspond à templates/rapport.html
    }

}
