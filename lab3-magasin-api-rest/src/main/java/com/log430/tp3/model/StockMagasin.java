package com.log430.tp3.model;

import jakarta.persistence.*;

// Annotation JPA indiquant que cette classe est une entité persistante
@Entity
// Nom de la table dans la base de données
@Table(name = "stockMagasin")
public class StockMagasin {

    // Identifiant unique pour chaque stock de magasin
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Plusieurs entrées de stock peuvent concerner un même produit
    @ManyToOne
    @JoinColumn(name = "produit_id")
    private Produit produit;

    // Plusieurs entrées de stock peuvent concerner un même magasin
    @ManyToOne
    @JoinColumn(name = "magasin_id")
    private Magasin magasin;

    // Quantité en stock local
    private int quantite;

    // Constructeur par défaut requis par JPA
    public StockMagasin() { }

    // Constructeur avec paramètres pour initialiser une entrée
    public StockMagasin(Produit produit, Magasin magasin, int quantite) {
        this.produit = produit;
        this.magasin = magasin;
        this.quantite = quantite;
    }

    // Getters / Setters
    public Integer getId() {
        return id;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public Magasin getMagasin() {
        return magasin;
    }

    public void setMagasin(Magasin magasin) {
        this.magasin = magasin;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    @Override
    public String toString() {
        return "StockMagasin{id=" + id +
                ", produit=" + (produit != null ? produit.getNom() : "null") +
                ", magasin=" + (magasin != null ? magasin.getNom() : "null") +
                ", quantite=" + quantite + "}";
    }
}
