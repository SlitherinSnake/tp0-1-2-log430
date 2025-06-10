package com.log430.tp2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

//import com.log430.tp2.model.Magasin;
import com.log430.tp2.model.Produit;
import com.log430.tp2.model.Vente;
import com.log430.tp2.repository.EmployeRepository;
import com.log430.tp2.repository.MagasinRepository;
import com.log430.tp2.repository.ProduitRepository;
import com.log430.tp2.repository.VenteRepository;

@Controller
@SessionAttributes("vente") // Maintient l’objet Vente en session entre les requêtes
public class VenteController {

    @Autowired
    private ProduitRepository produitRepository;
    @Autowired
    private EmployeRepository employeRepository;
    @Autowired
    private VenteRepository venteRepository;
    @Autowired
    private MagasinRepository magasinRepository;

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
     * Affiche la page d’accueil du système de vente, avec tous les produits et
     * employés.
     * L’objet vente est injecté dans le modèle automatiquement (grâce
     * à @SessionAttributes).
     */
    @GetMapping("/ventes")
    public String accueil(Model model, @ModelAttribute("vente") Vente vente) {
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
    public String showPanier(Model model, @ModelAttribute("vente") Vente vente) {
        model.addAttribute("items", vente.getItems());
        model.addAttribute("total", vente.getMontantTotal());

        // Injecte ici la liste des employés disponibles dans le formulaire
        model.addAttribute("employes", employeRepository.findAll());

        // Injecte ici la liste des magasins disponibles dans le formulaire
        model.addAttribute("magasins", magasinRepository.findAll());
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
    public String clearVente(SessionStatus status) {
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
    public String validerAchat(@RequestParam int employeId, @RequestParam int magasinId, @ModelAttribute("vente") Vente vente, SessionStatus status,
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

        // Nettoyer la session (vider le panier)
        status.setComplete();

        return "facture";
    }
}