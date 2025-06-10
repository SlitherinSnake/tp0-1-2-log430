package com.log430.tp2.controller;

import com.log430.tp2.service.RapportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RapportController {

    @Autowired
    private RapportService rapportService;

    /**
     * Point d'accès pour l'administrateur : affiche un rapport consolidé.
     * URL : /rapport
     * Vue associée : templates/rapport.html (à créer plus tard)
     */
    @GetMapping("/rapport")
    public String afficherRapport(Model model) {
        // Injecte dans le modèle les ventes par magasin
        model.addAttribute("ventesParMagasin", rapportService.ventesParMagasin());

        // Injecte la liste des produits les plus vendus
        model.addAttribute("produitsPopulaires", rapportService.produitsLesPlusVendus());

        // Injecte l’état actuel du stock
        model.addAttribute("stockActuel", rapportService.stockActuel());

        return "rapport"; // Correspond à resources/templates/rapport.html
    }
}
