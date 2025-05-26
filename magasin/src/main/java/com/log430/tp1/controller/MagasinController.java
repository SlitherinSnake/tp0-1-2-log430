package com.log430.tp1.controller;

import java.util.List;
import java.util.Scanner;

import com.log430.tp1.model.Produit;
import com.log430.tp1.model.dao.EmployeDAO;
import com.log430.tp1.model.dao.ProduitDAO;
import com.log430.tp1.model.dao.RetourDAO;
import com.log430.tp1.model.dao.VenteDAO;

public class MagasinController {

    private final EmployeDAO employeDAO;
    private final ProduitDAO produitDAO;
    private final RetourDAO retourDAO;
    private final VenteDAO venteDAO;

    public MagasinController() {
        this.employeDAO = new EmployeDAO();
        this.produitDAO = new ProduitDAO();
        this.retourDAO = new RetourDAO();
        this.venteDAO = new VenteDAO();
    }

    // Méthode pour rechercher un produit selon différents critères (ID, nom ou
    // catégorie)
    public void rechercherProduit(Scanner scanner) {
        System.out.println("Rechercher par : 1. ID  2. Nom  3. Catégorie");
        System.out.print("Choix : ");
        int choix = scanner.nextInt();
        scanner.nextLine();

        switch (choix) {
            case 1 -> {
                // Recherche par id unique
                System.out.print("ID du produit : ");
                int id = scanner.nextInt();
                scanner.nextLine();

                // Appel DAO pour chercher le produit par ID
                Produit produit = produitDAO.rechercherParId(id);

                // Basique if, montre trouver, sinon erreur 
                if (produit != null) {
                    System.out.println(produit);
                } else {
                    System.out.println("Aucun produit trouvé avec cet ID.");
                }
            }
            case 2 -> {
                // Recherche par nom (mot-clé partiel ou complet)
                System.out.print("Nom du produit : ");
                String nom = scanner.nextLine();

                // Appel DAO pour trouver tous les produits contenant ce nom
                List<Produit> produits = produitDAO.rechercherParNom(nom);

                // Affiche produits trouver
                afficherListeProduits(produits);
            }
            case 3 -> {
                // Recherche par catégorie (fruit, boisson, etc.)
                System.out.print("Catégorie du produit : ");
                String categorie = scanner.nextLine();

                 // Appel DAO pour récupérer les produits correspondant à la catégorie
                List<Produit> produits = produitDAO.rechercherParCategorie(categorie);

                // Affiche produits trouver
                afficherListeProduits(produits);
            }
            default -> System.out.println("Choix invalide.");
        }
    }

    // Affiche une liste de produits dans la console
    private void afficherListeProduits(List<Produit> produits) {
        if (produits.isEmpty()) {
            System.out.println("Aucun produit trouvé.");
        } else {
            produits.forEach(System.out::println);
        }
    }

    public Object enregistrerVente(Scanner scanner) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'enregistrerVente'");
    }

    public Object gererRetour(Scanner scanner) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'gererRetour'");
    }

    public Object consulterStock(Scanner scanner) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'consulterStock'");
    }

}
