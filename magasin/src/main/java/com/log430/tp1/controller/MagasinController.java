package com.log430.tp1.controller;

import java.util.List;
import java.util.Scanner;

import com.log430.tp1.model.Employe;
import com.log430.tp1.model.Produit;
import com.log430.tp1.model.Vente;
import com.log430.tp1.model.VenteProduit;
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

    public void enregistrerVente(Scanner scanner) {
        System.out.println("\n=== Enregistrement d'une vente ===");

        // Étape 1 : sélection de l'employé
        System.out.print("Entrez l'ID ou le nom de l'employé : ");
        String saisieEmploye = scanner.nextLine().trim();
        Employe employe = null;

        try {
            // Tente de lire l'ID de l'employé
            int id = Integer.parseInt(saisieEmploye);
            employe = employeDAO.rechercherParId(id);
        } catch (NumberFormatException e) {
            // Sinon, si ne lit pas un ID, cherche par nom (partiel ou complet)
            List<Employe> resultats = employeDAO.rechercherParNom(saisieEmploye);

            // Si un seul résultat, on le sélectionne directement
            if (resultats.size() == 1) {
                employe = resultats.get(0);
            }
            // Sinon, on laisse l'utilisateur choisir dans la liste
            else if (resultats.size() > 1) {
                System.out.println("Plusieurs employés trouvés :");
                for (int i = 0; i < resultats.size(); i++) {
                    System.out.println((i + 1) + ". " + resultats.get(i).getNom());
                }
                System.out.print("Choisissez un numéro : ");
                int choix = Integer.parseInt(scanner.nextLine());
                employe = resultats.get(choix - 1);
            }
        }

        // Vérifie si aucun employé valide n’a été trouvé
        if (employe == null) {
            System.out.println("Aucun employé trouvé. Abandon de la vente.");
            return;
        }

        // Affiche le nom de l'employé sélectionné
        System.out.println("Employé sélectionné : " + employe.getNom());

        // Étape 2 : saisie les produits et la quantités de produit venduedans
        // venteProduits
        List<VenteProduit> venteProduits = new java.util.ArrayList<>();
        // Liste des produits vendus
        float montantTotal = 0f; // Montant total <- vente
        String continuer = "y"; // Controle boucle d'ajout

        do {
            // Demande l’ID du produit à ajouter à la vente
            System.out.print("Entrez l'ID du produit : ");
            int produitId = Integer.parseInt(scanner.nextLine());
            Produit produit = produitDAO.rechercherParId(produitId);

            // Si le produit n’existe pas, retour au début et demande un nouveau id
            if (produit == null) {
                System.out.println("Produit non trouvé.");
                continue;
            }

            // Affiche les infos du produit sélectionné
            System.out.println("Produit sélectionné : " + produit.getNom() +
                    " | Prix : " + produit.getPrix() +
                    " | Quantité : " + produit.getQuantite());

            // Demande la quantité désirée
            System.out.print("Quantité : ");
            int quantite = Integer.parseInt(scanner.nextLine());

            // Vérifie si la quantité est disponible en stock
            if (produit.getQuantite() < quantite) {
                System.out.println("Stock insuffisant. Quantité disponible : " + produit.getQuantite());
                continue;
            }

            // Création de l'objet VenteProduit sans vente pour l’instant, cela sera assigné
            // après
            VenteProduit vp = new VenteProduit(null, produit, quantite);
            venteProduits.add(vp);

            // Met à jour la quantité restante dans la BD
            int quantiteRestante = produit.getQuantite() - quantite;
            produitDAO.mettreAJourStock(produit, quantiteRestante);

            // Ajoute le sous-total de ce produit au montant total
            montantTotal += produit.getPrix() * quantite;

            // Demande si l'utilisateur veut ajouter un autre produit
            System.out.print("Ajouter un autre produit ? (y/n) : ");
            continuer = scanner.nextLine().trim().toLowerCase();

        } while (continuer.equals("y")); // Infinite boucle tant réponse = "o"

        // Si aucun produit n’a été validé, annule la vente
        if (venteProduits.isEmpty()) {
            System.out.println("Aucun produit saisi. Abandon de la vente.");
            return;
        }

        // Étape 3 : création de la vente
        // Instancie une nouvelle vente avec les infos saisies
        Vente vente = new Vente();
        // Attribution de l’employé à vente, liste prod vendu, montant total, date vente
        vente.setEmploye(employe);
        vente.setMontantTotal(montantTotal);
        vente.setDateVente(java.time.LocalDate.now());

        // Associe chaque VenteProduit à cette vente
        for (VenteProduit vp : venteProduits) {
            vp.setVente(vente);
        }

        vente.setVenteProduits(venteProduits);

        // Persist la vente en base de données via le DAO
        venteDAO.enregistrerVente(vente);

        System.out.println("Vente enregistrée avec succès !");

        // Affichage final sous forme de facture
        System.out.println("\n=== Facture ===");
        System.out.println("Employé : " + employe.getNom());
        System.out.println("Date : " + vente.getDateVente());
        System.out.println("\nProduits vendus :");
        for (VenteProduit vp : venteProduits) {
            Produit produit = vp.getProduit();
            int qte = vp.getQuantite();
            float sousTotal = produit.getPrix() * qte;
            System.out.println("- " + produit.getNom() + " x" + qte +
                    " @ " + produit.getPrix() + " = " + String.format("%.2f", sousTotal) + "$");
        }
        System.out.println("\nTotal : " + String.format("%.2f", montantTotal) + "$");
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
