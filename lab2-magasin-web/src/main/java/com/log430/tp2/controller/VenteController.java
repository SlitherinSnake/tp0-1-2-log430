package com.log430.tp2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import com.log430.tp2.model.Produit;
import com.log430.tp2.model.Vente;
import com.log430.tp2.repository.EmployeRepository;
import com.log430.tp2.repository.ProduitRepository;
import com.log430.tp2.repository.VenteRepository;

@Controller
@SessionAttributes("vente")
public class VenteController {

    @Autowired
    private ProduitRepository produitRepository;

    @Autowired
    private EmployeRepository employeRepository;

    @Autowired
    private VenteRepository venteRepository;

    public VenteController(ProduitRepository produitRepository) {
        this.produitRepository = produitRepository;
    }

    // Ceci garantit que la session a toujours un objet Vente dispo
    @ModelAttribute("vente")
    public Vente vente() {
        return new Vente();
    }

    // Page d’accueil avec vente injecté dans le modèle
    @GetMapping("/ventes")
    public String accueil(Model model, @ModelAttribute("vente") Vente vente) {
        model.addAttribute("produits", produitRepository.findAll());
        model.addAttribute("employes", employeRepository.findAll());
        model.addAttribute("vente", vente);
        // Pour passer le nombre d’items si souhaiter :
        // model.addAttribute("nbPanier", vente.getItems().size());
        return "home";
    }

    // Page du panier
    @GetMapping("/panier")
    public String showPanier(Model model, @ModelAttribute("vente") Vente vente) {
        model.addAttribute("items", vente.getItems());
        model.addAttribute("total", vente.getMontantTotal());
        model.addAttribute("employes", employeRepository.findAll());
        return "panier";
    }

    // AJAX : Ajout d’un produit (pas de redirection)
    @PostMapping("/panier/add")
    public ResponseEntity<?> addToVente(@RequestParam int produitId, @RequestParam(defaultValue = "1") int quantite,
            @ModelAttribute("vente") Vente vente) {
        produitRepository.findById(produitId).ifPresent(produit -> {
            vente.ajouterProduit(produit, quantite);
        });
        // On retourne par exemple le nombre d’articles
        return ResponseEntity.ok(vente.getItems().size());
    }

    // AJAX : Suppression d’un produit
    @PostMapping("/panier/remove")
    public ResponseEntity<?> removeFromVente(@RequestParam int produitId, @ModelAttribute("vente") Vente vente) {
        vente.removeProduit(produitId);
        return ResponseEntity.ok(vente.getItems().size());
    }

    @PostMapping("/panier/clear")
    public String clearVente(SessionStatus status) {
        status.setComplete();
        return "redirect:/panier";
    }

    @PostMapping("/panier/valider")
    public String validerAchat(@RequestParam int employeId, @ModelAttribute("vente") Vente vente, SessionStatus status,
            Model model) {
        // Associer l'employé
        employeRepository.findById(employeId).ifPresent(vente::setEmploye);

        // 1. Gérer le stock
        vente.getItems().forEach(ligne -> {
            Produit produit = ligne.getProduit();
            int qteVendue = ligne.getQuantite();
            produit.decreaseStock(qteVendue);
            produitRepository.save(produit); // Persist the updated stock!
        });

        // Recalculer le montant total
        vente.calculerMontantTotal();

        // Date du jour
        vente.setDateVenteIfNull();

        // 2. Sauvegarder la vente (avec ses lignes déjà associées)
        venteRepository.save(vente);

        // 3. Préparer la facture/confirmation
        model.addAttribute("venteConfirmee", vente);
        status.setComplete(); // Vide le panier/session

        return "facture";
    }
}
