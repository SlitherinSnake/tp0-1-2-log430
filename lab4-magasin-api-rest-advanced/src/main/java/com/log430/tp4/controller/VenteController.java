package com.log430.tp4.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

import com.log430.tp4.model.Magasin;
import com.log430.tp4.model.Produit;
import com.log430.tp4.model.StockMagasin;
import com.log430.tp4.model.Vente;
import com.log430.tp4.repository.EmployeRepository;
import com.log430.tp4.repository.MagasinRepository;
import com.log430.tp4.repository.ProduitRepository;
import com.log430.tp4.repository.StockMagasinRepository;
import com.log430.tp4.repository.VenteRepository;

// Indique que cette classe est un contrôleur Spring MVC, responsable de gérer les requêtes web.
// Les méthodes à l'intérieur renvoient généralement vers des vues Thymeleaf (HTML).
@Controller
// Indique que l'objet nommé "vente" doit être stocké dans la session HTTP.
// Cela permet de le conserver entre plusieurs requêtes (ex : ajout au panier,
// validation…)
// Très utile pour simuler un panier temporaire ou une vente en cours.
@SessionAttributes({ "vente", "selectedMagasinId" }) // Maintient l'objet Vente et le magasin sélectionné en session
public class VenteController {

    // Injecte automatiquement une instance du composant (Repository, Service, etc.)
    // correspondant.
    // Permet d'éviter d'écrire un constructeur ou un setter manuellement.
    @Autowired
    private ProduitRepository produitRepository;
    @Autowired
    private EmployeRepository employeRepository;
    @Autowired
    private VenteRepository venteRepository;
    @Autowired
    private MagasinRepository magasinRepository;
    @Autowired
    private StockMagasinRepository stockMagasinRepository;

    // Constructeur avec injection de dépendance explicite pour ProduitRepository
    public VenteController(ProduitRepository produitRepository) {
        this.produitRepository = produitRepository;
    }

    /**
     * Initialise un objet Vente dans la session si inexistant.
     * Ceci permet de simuler un panier persistent dans la session utilisateur.
     */
    @ModelAttribute("vente")
    public Vente vente() {
        return new Vente();
    }

    /**
     * Initialise l'ID du magasin sélectionné dans la session si inexistant.
     */
    @ModelAttribute("selectedMagasinId")
    public Integer initSelectedMagasinId() {
        // Default to first store (ID 1)
        return 1;
    }

    /**
     * Fournit la liste de tous les magasins pour l'affichage dans le menu
     * déroulant.
     */
    @ModelAttribute("allMagasins")
    public List<Magasin> getAllMagasins() {
        return magasinRepository.findAll();
    }

    /**
     * Affiche la page d'accueil du système de vente, avec tous les produits et
     * employés.
     * L'objet vente est injecté dans le modèle automatiquement (grâce
     * à @SessionAttributes).
     */
    @GetMapping("/ventes")
    public String accueil(Model model, @ModelAttribute("vente") Vente vente,
            @ModelAttribute("selectedMagasinId") Integer selectedMagasinId) {
        // Get all products
        List<Produit> allProducts = produitRepository.findAll();

        // Get the selected store
        Magasin selectedMagasin = magasinRepository.findById(selectedMagasinId).orElse(null);
        model.addAttribute("selectedMagasin", selectedMagasin);

        // Get stock for the selected store
        List<StockMagasin> stockItems = stockMagasinRepository.findByMagasinId(selectedMagasinId);

        // Create a list of products with their stock quantities for the selected store
        List<Produit> productsWithStock = new ArrayList<>();
        for (Produit produit : allProducts) {
            Optional<StockMagasin> stockItem = stockItems.stream()
                    .filter(s -> s.getProduit().getId() == produit.getId())
                    .findFirst();

            if (stockItem.isPresent()) {
                // Create a copy of the product with the stock quantity
                Produit productWithStock = new Produit();
                productWithStock.setId(produit.getId());
                productWithStock.setNom(produit.getNom());
                productWithStock.setCategorie(produit.getCategorie());
                productWithStock.setPrix(produit.getPrix());
                productWithStock.setQuantite(stockItem.get().getQuantite());
                productsWithStock.add(productWithStock);
            }
        }

        model.addAttribute("produits", productsWithStock);
        model.addAttribute("employes", employeRepository.findAll());
        model.addAttribute("vente", vente);
        model.addAttribute("produitForm", new Produit()); // formulaire vide
        model.addAttribute("editing", false);
        // Pour passer le nombre d'items si souhaiter :
        // model.addAttribute("nbPanier", vente.getItems().size());
        return "home";
    }

    /**
     * Affiche le contenu du panier (lignes de vente) avec le total actuel.
     */
    @GetMapping("/panier")
    public String showPanier(Model model,
            @ModelAttribute("vente") Vente vente,
            @ModelAttribute("selectedMagasinId") Integer selectedMagasinId) {
        model.addAttribute("items", vente.getItems());
        model.addAttribute("total", vente.getMontantTotal());

        // Injecte ici la liste des employés disponibles dans le formulaire
        model.addAttribute("employes", employeRepository.findAll());

        // Injecte ici la liste des magasins disponibles dans le formulaire
        model.addAttribute("magasins", magasinRepository.findAll());

        // Add selected store to model
        Magasin selectedMagasin = magasinRepository.findById(selectedMagasinId).orElse(null);
        model.addAttribute("selectedMagasin", selectedMagasin);

        return "panier";
    }

    /**
     * Ajoute un produit au panier via un appel AJAX.
     * Ne provoque pas de redirection, retourne simplement le nombre d'articles dans
     * le panier.
     */
    @PostMapping("/panier/add")
    public ResponseEntity<?> addToVente(@RequestParam int produitId, @RequestParam(defaultValue = "1") int quantite,
            @ModelAttribute("vente") Vente vente) {

        // Recherche du produit puis ajout à la vente si présent
        produitRepository.findById(produitId).ifPresent(produit -> {
            vente.ajouterProduit(produit, quantite);
            vente.calculerMontantTotal();
        });

        // Retourne le nombre d'items pour mise à jour UI côté client
        return ResponseEntity.ok(vente.getItems().size());
    }

    /**
     * Supprime un produit du panier via appel AJAX.
     */
    @PostMapping("/panier/remove")
    public ResponseEntity<?> removeFromVente(@RequestParam int produitId, @ModelAttribute("vente") Vente vente) {
        vente.removeProduit(produitId);
        return ResponseEntity.ok(vente.getItems().size());
    }

    /**
     * Vide complètement le panier en supprimant l'objet Vente de la session.
     */
    @PostMapping("/panier/clear")
    public String clearVente(SessionStatus status,
            @ModelAttribute("selectedMagasinId") Integer selectedMagasinId) {
        status.setComplete(); // Invalide l'objet vente dans la session
        return "redirect:/panier";
    }

    /**
     * Retourne le nombre d'items dans le panier.
     * Utilisé par JavaScript (AJAX) pour afficher le compteur en haut de page.
     */
    @GetMapping("/panier/count")
    @ResponseBody
    public String getPanierCount(@ModelAttribute("vente") Vente vente) {
        return String.valueOf(vente.getItems().size());
    }

    /**
     * Valide la vente : met à jour le stock, assigne un employé, sauvegarde en BD.
     * Affiche ensuite la facture.
     */
    @PostMapping("/panier/valider")
    public String validerAchat(@RequestParam int employeId, @RequestParam int magasinId,
            @ModelAttribute("vente") Vente vente,
            @ModelAttribute("selectedMagasinId") Integer selectedMagasinId,
            SessionStatus status,
            Model model) {

        // Associer un employé à la vente
        employeRepository.findById(employeId).ifPresent(vente::setEmploye);

        // Associer le magasin
        magasinRepository.findById(magasinId).ifPresent(vente::setMagasin);

        // 1. Mise à jour des stocks : on diminue le stock de chaque produit vendu
        vente.getItems().forEach(ligne -> {
            int qteVendue = ligne.getQuantite();

            // On récupère le stock du produit dans le magasin où la vente a lieu
            StockMagasin stock = stockMagasinRepository
                    .findByProduitAndMagasin(ligne.getProduit().getId(), vente.getMagasin().getId())
                    .orElseThrow(() -> new RuntimeException("Stock introuvable"));

            int stockActuel = stock.getQuantite();

            // Vérifie qu'on a suffisamment de stock pour effectuer la vente
            if (stockActuel < qteVendue) {
                throw new RuntimeException("Stock insuffisant pour " + ligne.getProduit().getNom());
            }

            // Mise à jour du stock local : on retire la quantité vendue
            stock.setQuantite(stockActuel - qteVendue);
            stockMagasinRepository.save(stock); // Sauvegarde en base du nouveau stock
            /*
             * Pour stock central
             * Produit produit = ligne.getProduit();
             * produit.decreaseStock(qteVendue);
             * produitRepository.save(produit); // sauvegarder les modifications
             */
        });

        // 2. Recalculer le total de la vente
        vente.calculerMontantTotal();

        // 3. Ajouter la date si elle est absente
        vente.setDateVenteIfNull();

        // 4. Sauvegarder la vente complète (produits, employé, date, total)
        venteRepository.save(vente);

        // 5. Préparer la page de confirmation/facture
        model.addAttribute("venteConfirmee", vente);

        // Add selected store to model
        Magasin selectedMagasin = magasinRepository.findById(selectedMagasinId).orElse(null);
        model.addAttribute("selectedMagasin", selectedMagasin);

        // Nettoyer la session (vider le panier)
        status.setComplete();

        return "facture";
    }
}