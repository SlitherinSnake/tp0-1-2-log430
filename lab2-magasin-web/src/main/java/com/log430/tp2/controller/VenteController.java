package com.log430.tp2.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

import com.log430.tp2.model.Magasin;
import com.log430.tp2.model.StockMagasin;
import com.log430.tp2.model.Produit;
import com.log430.tp2.model.Vente;
import com.log430.tp2.repository.EmployeRepository;
import com.log430.tp2.repository.MagasinRepository;
import com.log430.tp2.repository.ProduitRepository;
import com.log430.tp2.repository.StockMagasinRepository;
import com.log430.tp2.repository.VenteRepository;

// Indique que cette classe est un contrôleur Spring MVC, responsable de gérer les requêtes web.
// Les méthodes à l’intérieur renvoient généralement vers des vues Thymeleaf (HTML).
@Controller
// Indique que les objets "vente" et "selectedMagasinId" doivent être stockés dans la session HTTP.
// Cela permet de conserver la vente en cours (panier) et le magasin sélectionné entre plusieurs requêtes.
// Utile pour gérer un panier temporaire tout au long du processus d'achat.
@SessionAttributes({"vente", "selectedMagasinId"}) // Maintient l’objet Vente et le magasin sélectionné en session entre les requêtes
public class VenteController {

    // Injecte automatiquement une instance du composant (Repository, Service, etc.) correspondant.
    // Permet d’éviter d’écrire un constructeur ou un setter manuellement.
    @Autowired
    private ProduitRepository produitRepository; // Accès aux produits
    @Autowired
    private EmployeRepository employeRepository; // Accès aux employés
    @Autowired
    private VenteRepository venteRepository; // Accès aux ventes
    @Autowired
    private MagasinRepository magasinRepository; // Accès aux magasin
    @Autowired
    private StockMagasinRepository stockMagasinRepository; // Accès aux stock du magasin

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
        // Premier magasin ID par défaut (ID 1)
        return 1;
    }

    /**
     * Fournit la liste de tous les magasins pour l'affichage dans le menu déroulant.
     */
    @ModelAttribute("allMagasins")
    public List<Magasin> getAllMagasins() {
        return magasinRepository.findAll();
    }

    /**
     * Affiche la page d’accueil du système de vente, avec tous les produits et
     * employés.
     * L’objet vente est injecté dans le modèle automatiquement (grâce
     * à @SessionAttributes).
     */
    @GetMapping("/ventes")
    public String accueil(Model model, @ModelAttribute("vente") Vente vente, @ModelAttribute("selectedMagasinId") Integer selectedMagasinId) {
        // Obtient tous les produits
        List<Produit> allProducts = produitRepository.findAll();

        // Obtient le magasn choisie
        Magasin selectedMagasin = magasinRepository.findById(selectedMagasinId).orElse(null);
        model.addAttribute("selectedMagasin", selectedMagasin);

        // Récupère le stock du magasin choisie
        List<StockMagasin> stockItems = stockMagasinRepository.findByMagasinId(selectedMagasinId);

        // Crée une liste des produits avec leur propre quantité pour le magasin choisie
        List<Produit> productsWithStock = new ArrayList<>();
        for (Produit produit : allProducts) {
            Optional<StockMagasin> stockItem = stockItems.stream()
                .filter(s -> s.getProduit().getId() == produit.getId())
                .findFirst();
            
            if (stockItem.isPresent()) {
                // Crée une copie du produit avec sa quantité présente dans le stock
                Produit productWithStock = new Produit();
                productWithStock.setId(produit.getId());
                productWithStock.setNom(produit.getNom());
                productWithStock.setCategorie(produit.getCategorie());
                productWithStock.setPrix(produit.getPrix());
                productWithStock.setQuantite(stockItem.get().getQuantite());
                productsWithStock.add(productWithStock);
            }
        }

        model.addAttribute("produits", produitRepository.findAll());
        model.addAttribute("employes", employeRepository.findAll());
        model.addAttribute("vente", vente);
        model.addAttribute("produitForm", new Produit()); // formulaire vide
        model.addAttribute("editing", false);
        // Pour passer le nombre d’items si souhaiter :
        // model.addAttribute("nbPanier", vente.getItems().size());
        return "home";
    }

    /**
     * Affiche le contenu du panier (lignes de vente) avec le total actuel.
     */
    @GetMapping("/panier")
    public String showPanier(Model model, @ModelAttribute("vente") Vente vente, @ModelAttribute("selectedMagasinId") Integer selectedMagasinId) {
        model.addAttribute("items", vente.getItems());
        model.addAttribute("total", vente.getMontantTotal());

        // Injecte ici la liste des employés disponibles dans le formulaire
        model.addAttribute("employes", employeRepository.findAll());

        // Injecte ici la liste des magasins disponibles dans le formulaire
        model.addAttribute("magasins", magasinRepository.findAll());

        // Ajoute le magasin choisie au model
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

        // Retourne le nombre d’items pour mise à jour UI côté client
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
     * Vide complètement le panier en supprimant l’objet Vente de la session.
     */
    @PostMapping("/panier/clear")
    public String clearVente(SessionStatus status, @ModelAttribute("selectedMagasinId") Integer selectedMagasinId) {
        status.setComplete(); // Invalide l’objet vente dans la session
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
            @ModelAttribute("vente") Vente vente, @ModelAttribute("selectedMagasinId") Integer selectedMagasinId, SessionStatus status,
            Model model) {

        // Associer un employé à la vente
        employeRepository.findById(employeId).ifPresent(vente::setEmploye);

        // Associer le magasin
        magasinRepository.findById(magasinId).ifPresent(vente::setMagasin);

        // 1. Mise à jour des stocks : on diminue le stock de chaque produit vendu
        vente.getItems().forEach(ligne -> {
            Produit produit = ligne.getProduit();
            int qteVendue = ligne.getQuantite();
            produit.decreaseStock(qteVendue);
            produitRepository.save(produit); // sauvegarder les modifications
        });

        // 2. Recalculer le total de la vente
        vente.calculerMontantTotal();

        // 3. Ajouter la date si elle est absente
        vente.setDateVenteIfNull();

        // 4. Sauvegarder la vente complète (produits, employé, date, total)
        venteRepository.save(vente);

        // 5. Préparer la page de confirmation/facture
        model.addAttribute("venteConfirmee", vente);

        // Ajoute magasin choisie au model
        Magasin selectedMagasin = magasinRepository.findById(selectedMagasinId).orElse(null);
        model.addAttribute("selectedMagasin", selectedMagasin);

        // Nettoyer la session (vider le panier)
        status.setComplete();

        return "facture";
    }
}