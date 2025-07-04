package com.log430.tp4.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.log430.tp4.model.Magasin;
import com.log430.tp4.model.Produit;
import com.log430.tp4.model.StockMagasin;
import com.log430.tp4.repository.MagasinRepository;
import com.log430.tp4.repository.ProduitRepository;
import com.log430.tp4.repository.StockMagasinRepository;

// Indique que cette classe est un contrôleur Spring MVC, responsable de gérer les requêtes web.
// Les méthodes à l’intérieur renvoient généralement vers des vues Thymeleaf (HTML).
@Controller
// Garde "selectedMagasinId" en session pour le réutiliser entre les requêtes (ex : lors des ventes ou retours).
@SessionAttributes("selectedMagasinId")
public class ProduitController {

    // Injecte automatiquement une instance du composant (Repository, Service, etc.) correspondant.
    // Permet d’éviter d’écrire un constructeur ou un setter manuellement.
    @Autowired
    private ProduitRepository produitRepository; // Accès aux produits
    @Autowired
    private MagasinRepository magasinRepository; // Accès aux magasin
    @Autowired
    private StockMagasinRepository stockMagasinRepository; // Accès au stock du magasin

    // Initialise "selectedMagasinId" dans la session avec la valeur par défaut (1)
    @ModelAttribute("selectedMagasinId")
    public Integer initSelectedMagasinId() {
        // ID defaut du premier magasin
        return 1;
    }
    
    // Charge la liste complète des magasins pour les rendre disponibles dans toutes les vues
    @ModelAttribute("allMagasins")
    public List<Magasin> getAllMagasins() {
        return magasinRepository.findAll();
    }
    
    // Met à jour le magasin sélectionné dans la session et redirige vers l’accueil
    @GetMapping("/selectMagasin/{id}")
    public String selectMagasin(@PathVariable int id, @ModelAttribute("selectedMagasinId") Integer selectedMagasinId, Model model) {
        // Met à jour le magasin sélectionné dans la session
        model.addAttribute("selectedMagasinId", id);
        // Retour à la page d'acceuil
        return "redirect:/";
    }

     /**
     * Méthode utilitaire privée pour afficher la page principale avec la liste des produits.
     * Elle permet aussi de pré-remplir le formulaire d'édition si nécessaire.
     *
     * @param model         l’objet Model pour transmettre les données à la vue Thymeleaf
     * @param produits      la liste des produits à afficher
     * @param editing       vrai si l’utilisateur est en train de modifier un produit
     * @param produitForm   l’objet Produit prérempli dans le formulaire (édition)
     * @param magasinId     l'ID du magasin sélectionné
     * @return              le nom du template Thymeleaf à afficher ("home")
     */
    private String renderHome(Model model, List<Produit> produits, boolean editing, Produit produitForm, Integer magasinId) {
        // Récupérer le magasin sélectionné à partir de son ID (magasinId)
        Magasin selectedMagasin = magasinRepository.findById(magasinId).orElse(null);
        
        // Récupérer les stocks associés à ce magasin
        List<StockMagasin> stockItems = stockMagasinRepository.findByMagasinId(magasinId);
        
        // Préparer une nouvelle liste de produits, chacun avec la quantité en stock du magasin sélectionné
        List<Produit> productsWithStock = new ArrayList<>();
        for (Produit produit : produits) {
            Optional<StockMagasin> stockItem = stockItems.stream()
                .filter(s -> s.getProduit().getId() == produit.getId())
                .findFirst();
            
            if (stockItem.isPresent()) {
                // Créer une copie du produit avec sa quantité disponible en stock
                Produit productWithStock = new Produit();
                productWithStock.setId(produit.getId());
                productWithStock.setNom(produit.getNom());
                productWithStock.setCategorie(produit.getCategorie());
                productWithStock.setPrix(produit.getPrix());
                productWithStock.setQuantite(stockItem.get().getQuantite());

                // Ajouter ce produit enrichi à la liste
                productsWithStock.add(productWithStock);
            }
        }
        
        // Ajouter les données au modèle pour affichage dans la vue
        model.addAttribute("produits", productsWithStock);                     // Liste des produits avec stock magasin
        model.addAttribute("produitForm", produitForm != null ? produitForm : new Produit()); // Formulaire produit vide ou en édition
        model.addAttribute("editing", editing);                                // Indique si on est en mode édition
        model.addAttribute("selectedMagasin", selectedMagasin);                // Magasin actuellement sélectionné

        // Afficher la page d’accueil (home.html)
        return "home";
    }

    /**
     * Affiche tous les produits sur la page d'accueil.
     */
    @GetMapping("/")
    public String listProduits(@ModelAttribute("selectedMagasinId") Integer selectedMagasinId, Model model) {
        return renderHome(model, produitRepository.findAll(), false, new Produit(), selectedMagasinId);
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
            @ModelAttribute("selectedMagasinId") Integer selectedMagasinId,
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
        return renderHome(model, resultats, false, new Produit(), selectedMagasinId);
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
    public String showEditForm(@PathVariable int id, @ModelAttribute("selectedMagasinId") Integer selectedMagasinId, Model model) {
        Produit produit = produitRepository.findById(id).orElse(null);
        return renderHome(model, produitRepository.findAll(), true, produit, selectedMagasinId);
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
