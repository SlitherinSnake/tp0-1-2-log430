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

    private String renderHome(Model model, List<Produit> produits, boolean editing, Produit produitForm) {
        model.addAttribute("produits", produits);
        model.addAttribute("produitForm", produitForm != null ? produitForm : new Produit());
        model.addAttribute("editing", editing);
        return "home";
    }

    @GetMapping("/")
    public String listProduits(Model model) {
        return renderHome(model, produitRepository.findAll(), false, new Produit());
    }

    @GetMapping("/produit/search")
    public String rechercherProduit(@RequestParam(required = false) Integer id,
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String categorie,
            Model model) {
        List<Produit> resultats;

        if (id != null) {
            Produit p = produitRepository.findById(id).orElse(null);
            resultats = (p != null) ? List.of(p) : List.of();
        } else if (nom != null && !nom.isEmpty()) {
            resultats = produitRepository.findByNomContainingIgnoreCase(nom);
        } else if (categorie != null && !categorie.isEmpty()) {
            resultats = produitRepository.findByCategorieContainingIgnoreCase(categorie);
        } else {
            resultats = produitRepository.findAll();
        }

        return renderHome(model, resultats, false, new Produit());
    }

    @PostMapping("/produit/add")
    public String addProduit(@ModelAttribute("produitForm") Produit produit) {
        produitRepository.save(produit);
        return "redirect:/";
    }

    @GetMapping("/produit/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model) {
        Produit produit = produitRepository.findById(id).orElse(null);
        return renderHome(model, produitRepository.findAll(), true, produit);
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
