package com.log430.tp2.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.log430.tp2.model.Produit;
import com.log430.tp2.repository.ProduitRepository;

@Controller
public class ProduitController {

    @Autowired
    private ProduitRepository produitRepository;
    
     /**
     * Méthode utilitaire privée pour afficher la page principale avec la liste des produits.
     * Elle permet aussi de pré-remplir le formulaire d'édition si nécessaire.
     *
     * @param model         l’objet Model pour transmettre les données à la vue Thymeleaf
     * @param produits      la liste des produits à afficher
     * @param editing       vrai si l’utilisateur est en train de modifier un produit
     * @param produitForm   l’objet Produit prérempli dans le formulaire (édition)
     * @return              le nom du template Thymeleaf à afficher ("home")
     */
    private String renderHome(Model model, List<Produit> produits, boolean editing, Produit produitForm) {
        model.addAttribute("produits", produits);
        model.addAttribute("produitForm", produitForm != null ? produitForm : new Produit());
        model.addAttribute("editing", editing);
        return "home";
    }

    /**
     * Affiche tous les produits sur la page d'accueil.
     */
    @GetMapping("/")
    public String listProduits(Model model) {
        return renderHome(model, produitRepository.findAll(), false, new Produit());
    }

    /**
     * Effectue une recherche de produits selon l’ID, le nom ou la catégorie.
     * La priorité est donnée à l'ID si fourni.
     * Sinon, le filtre combine nom et catégorie si les deux sont fournis.
     * 
     * Résultat affiché sur la même page "home", avec indication si aucun résultat n’est trouvé.
     */
    @GetMapping("/produit/search")
    public String rechercherProduit(@RequestParam(required = false) Integer id,
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String categorie,
            Model model) {
        List<Produit> resultats;

        if (id != null) {
            // Si un ID est fourni, on fait une recherche directe
            Produit p = produitRepository.findById(id).orElse(null);
            resultats = (p != null) ? List.of(p) : List.of();
        } else if (nom != null && !nom.trim().isEmpty() && categorie != null && !categorie.trim().isEmpty()) {
            // Recherche combinée nom + catégorie
            resultats = produitRepository.findByNomContainingIgnoreCaseAndCategorieContainingIgnoreCase(nom, categorie);
        } else if (nom != null && !nom.trim().isEmpty()) {
            // Recherche uniquement par nom
            resultats = produitRepository.findByNomContainingIgnoreCase(nom);
        } else if (categorie != null && !categorie.trim().isEmpty()) {
            // Recherche uniquement par catégorie
            resultats = produitRepository.findByCategorieContainingIgnoreCase(categorie);
        } else {
            // Aucun filtre → on retourne tous les produits
            resultats = produitRepository.findAll();
        }

        // Ajout d’un indicateur dans le modèle si la recherche n’a retourné aucun résulta
        model.addAttribute("aucunResultat", resultats.isEmpty());

        // Utilise renderHome comme d’habitude
        return renderHome(model, resultats, false, new Produit());
    }

    /**
     * Enregistre un nouveau produit soumis via le formulaire (POST).
     * Redirige vers l'accueil après l'ajout.
     */
    @PostMapping("/produit/add")
    public String addProduit(@ModelAttribute("produitForm") Produit produit) {
        produitRepository.save(produit);
        return "redirect:/";
    }

    /**
     * Affiche le formulaire d’édition pour un produit donné.
     * Le formulaire est pré-rempli avec les données du produit existant.
     */
    @GetMapping("/produit/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model) {
        Produit produit = produitRepository.findById(id).orElse(null);
        return renderHome(model, produitRepository.findAll(), true, produit);
    }

    /**
     * Met à jour un produit existant après soumission du formulaire d’édition.
     * L’enregistrement est effectué via la méthode save de JpaRepository.
     */
    @PostMapping("/produit/edit")
    public String editProduit(@ModelAttribute("produitForm") Produit produit) {
        produitRepository.save(produit);
        return "redirect:/";
    }

    /**
     * Supprime un produit en fonction de son ID.
     * Redirige ensuite vers la page principale pour rafraîchir la liste.
     */
    @GetMapping("/produit/delete/{id}")
    public String deleteProduit(@PathVariable int id) {
        produitRepository.deleteById(id);
        return "redirect:/";
    }
}
