package com.log430.tp1.model;

import jakarta.persistence.*;

// Annotation JPA indiquent que la classe est une entité persistante
@Entity
// Nom de la table dans la bd
@Table(name = "retour_produit")
public class RetourProduit {


    // Identifiant unique pour chaque retour
    @Id
    // BD auto-génère les IDs
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // Plusieurs retourProduit peuvent être liés à un seul retour
    @ManyToOne
    // Ceci précise que dans la table retourProduit, il y a une colonne retour_id pour identifier le retour.
    @JoinColumn(name = "retour_id")
    private Retour retour;

    // Plusieurs retour de produit peuvent être effectués dans un retour
    @ManyToOne
    // Ceci précise que dans la table retourProduit, il y a une colonne produit_id pour identifier les produits.
    @JoinColumn(name = "produit_id")
    private Produit produit;

    private int quantite;

    // Constructeur par défaut requis par JPA
    public RetourProduit() {

    }

     // Constructeur avec paramètres pour instancier un RetourProduit 
    public RetourProduit(Retour retour, Produit produit, int quantite) {
        this.retour = retour;
        this.produit = produit;
        this.quantite = quantite;
    }

    // Getters / Setters
    public int getId() {
        return id;
    }

    public Retour getRetour() {
        return retour;
    }

    public void setRetour(Retour retour) {
        this.retour = retour;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    @Override
    public String toString() {
        return "RetourProduit{id=" + id +
               ", produit=" + produit.getNom() +
               ", quantite=" + quantite + "}";
    }
}
