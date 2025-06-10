package com.log430.tp2.controller;

import com.log430.tp2.model.Produit;
import com.log430.tp2.model.StockMagasin;
import com.log430.tp2.model.Vente;
import com.log430.tp2.repository.ProduitRepository;
import com.log430.tp2.repository.StockMagasinRepository;
import com.log430.tp2.repository.VenteRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

// Indique que cette classe est un contrôleur Spring MVC, responsable de gérer les requêtes web.
// Les méthodes à l’intérieur renvoient généralement vers des vues Thymeleaf (HTML).
@Controller
public class DashboardController {

    // Injecte automatiquement une instance du composant (Repository, Service, etc.)
    // correspondant.
    // Permet d’éviter d’écrire un constructeur ou un setter manuellement.
    @Autowired
    private VenteRepository venteRepository; // Accès aux ventes
    @Autowired
    private StockMagasinRepository stockMagasinRepository; // Accès au stock des magasins
    @Autowired
    private ProduitRepository produitRepository;

    // Route pour afficher la page du tableau de bord
    @GetMapping("/dashboard")
    public String afficherDashboard(Model model) {

        // Stock Central
        List<Produit> stockCentral = produitRepository.findAll();
        model.addAttribute("stockCentral", stockCentral);

        // 1. Chiffre d'affaires par magasin
        // Requête personnalisée (native ou JPQL) retournant : [nom_magasin,
        // montant_total]
        List<Object[]> caParMagasin = venteRepository.chiffreAffaireTousMagasins();
        model.addAttribute("chiffresAffaires", caParMagasin);

        // 2. Produits en rupture de stock (quantité == 0)
        List<StockMagasin> ruptures = stockMagasinRepository.findByQuantite(0);
        model.addAttribute("ruptures", ruptures);

        // 3. Produits en surstock (quantité > 100)
        List<StockMagasin> surstocks = stockMagasinRepository.findByQuantiteGreaterThan(100);
        model.addAttribute("surstocks", surstocks);

        // 4. Tendances des ventes – total par jour sur 7 derniers jours
        LocalDate ilY7Jours = LocalDate.now().minusDays(6); // Inclut aujourd’hui (J-6 à J)
        List<Vente> ventes = venteRepository.findByDateVenteAfterOrderByDateVenteAsc(ilY7Jours);

        // Regroupement par date → somme des montants
        Map<LocalDate, Double> ventesParJour = ventes.stream()
                .collect(Collectors.groupingBy(
                        Vente::getDateVente,
                        TreeMap::new, // Pour garder les dates triées
                        Collectors.summingDouble(Vente::getMontantTotal)));

        // Conversion en format exploitable côté Thymeleaf (liste de maps {date, total})
        List<Map<String, Object>> ventesSemaine = new ArrayList<>();
        for (Map.Entry<LocalDate, Double> entry : ventesParJour.entrySet()) {
            Map<String, Object> ligne = new HashMap<>();
            ligne.put("date", entry.getKey());
            ligne.put("total", entry.getValue());
            ventesSemaine.add(ligne);
        }

        model.addAttribute("ventesSemaine", ventesSemaine); // Passé à la vue

        // Affiche la vue Thymeleaf "dashboard.html"
        return "dashboard";
    }
}
