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

    @GetMapping("/")
    public String listProduits(Model model) {
        if (!model.containsAttribute("produitForm")) {
            model.addAttribute("produitForm", new Produit());
        }
        if (!model.containsAttribute("editing")) {
            model.addAttribute("editing", false);
        }
        model.addAttribute("produits", produitRepository.findAll());
        return "home";
    }

    @GetMapping("/produit/search")
    public String rechercherProduit(@RequestParam(required = false) Integer id,
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String categorie,
            Model model) {
        List<Produit> resultats;

        if (id != null) {
            // Recherche par ID (exact)
            Produit produit = produitRepository.findById(id).orElse(null);
            resultats = (produit != null) ? List.of(produit) : List.of();
        } else if (nom != null && !nom.isEmpty()) {
            // Recherche par nom (partiel)
            resultats = produitRepository.findByNomContainingIgnoreCase(nom);
        } else if (categorie != null && !categorie.isEmpty()) {
            // Recherche par catégorie (partiel)
            resultats = produitRepository.findByCategorieContainingIgnoreCase(categorie);
        } else {
            resultats = produitRepository.findAll();
        }

        model.addAttribute("produits", resultats);
        model.addAttribute("produitForm", new Produit());
        model.addAttribute("editing", false);
        return "home";
    }

    @PostMapping("/produit/add")
    public String addProduit(@ModelAttribute("produitForm") Produit produit) {
        produitRepository.save(produit);
        return "redirect:/";
    }

    @GetMapping("/produit/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model) {
        Produit produit = produitRepository.findById(id).orElse(null);
        model.addAttribute("produitForm", produit);
        model.addAttribute("editing", true);
        model.addAttribute("produits", produitRepository.findAll());
        return "home";
    }

    @PostMapping("/produit/edit")
    public String editProduit(@ModelAttribute("produitForm") Produit produit) {
        produitRepository.save(produit);
        return "redirect:/";
    }

    @GetMapping("/produit/delete/{id}")
    public String deleteProduit(@PathVariable int id) {
        produitRepository.deleteById(id);
        return "redirect:/";
    }
}
