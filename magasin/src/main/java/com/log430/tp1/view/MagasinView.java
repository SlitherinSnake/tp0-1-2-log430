package com.log430.tp1.view;

import java.util.Scanner;

import com.log430.tp1.controller.MagasinController;

public class MagasinView {
    private Scanner scanner = new Scanner(System.in);
    private MagasinController controller = new MagasinController();

    public void start() {
        int choix;
        do {
            System.out.println("\n==== MENU ====");
            System.out.println("1. Consulter le stock");
            System.out.println("2. Rechercher un produit");
            System.out.println("3. Enregistrer une vente");
            System.out.println("4. Gérer un retour");
            System.out.println("0. Quitter");
            System.out.print("Choix : ");
            choix = scanner.nextInt();
            scanner.nextLine();

            switch (choix) {
                case 1 -> controller.consulterStock(scanner);
                case 2 -> controller.rechercherProduit(scanner);
                case 3 -> controller.enregistrerVente(scanner);
                case 4 -> controller.gererRetour(scanner);
                case 0 -> System.out.println("Fermeture du système.");
                default -> System.out.println("Choix invalide.");
            }
        } while (choix != 0);
        scanner.close();
        System.exit(0);
    }
}
