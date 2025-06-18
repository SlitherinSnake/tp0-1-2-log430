package com.log430.tp3.model;

import java.time.LocalDate;

import jakarta.persistence.*;

// Annotation JPA indiquant que cette classe est une entité persistante
@Entity
// Nom de la table dans la base de données
@Table(name = "stockCentral")
public class StockCentral {
    
    // Identifiant unique pour chaque employé
    @Id
    // BD auto-génère les IDs
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    
    private int id;

    // Plusieurs demandes de stock peuvent concerner un même produit
    @ManyToOne
    // Cela génère une colonne "produit_id" dans la table stock
    @JoinColumn(name = "produit_id")
    private Produit produit;

    // Plusieurs demandes peuvent concerner un même magasin
    @ManyToOne
    // Cela génère une colonne "magasin_id" dans la table stock
    private Magasin magasin;

    private int quantiteDemandee;
    private LocalDate dateDemande;

    // Constructeur par défaut requis par JPA
    public StockCentral() { }

    // Constructeur avec paramètres pour initialiser une demande
    public StockCentral(Produit produit, Magasin magasin, int quantiteDemandee, LocalDate dateDemande) {
        this.produit = produit;
        this.magasin = magasin;
        this.quantiteDemandee = quantiteDemandee;
        this.dateDemande = dateDemande;
    }

     // Getters / Setters
    public int getId() { return id; }

    public Produit getProduit() { return produit; }
    public void setProduit(Produit produit) { this.produit = produit; }

    public Magasin getMagasin() { return magasin; }
    public void setMagasin(Magasin magasin) { this.magasin = magasin; }

    public int getQuantiteDemandee() { return quantiteDemandee; }
    public void setQuantiteDemandee(int quantiteDemandee) { this.quantiteDemandee = quantiteDemandee; }

    public LocalDate getDateDemande() { return dateDemande; }
    public void setDateDemande(LocalDate dateDemande) { this.dateDemande = dateDemande; }

    @Override
    public String toString() {
        return "Stock{id=" + id +
                ", produit=" + (produit != null ? produit.getNom() : "null") +
                ", magasin=" + (magasin != null ? magasin.getNom() : "null") +
                ", quantiteDemandee=" + quantiteDemandee +
                ", dateDemande=" + dateDemande + "}";
    }
}
