package com.log430.tp2.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

// Annotation JPA indiquent que la classe est une entité persistante
@Entity
// Nom de la table dans la bd
@Table(name = "vente_produit")
public class VenteProduit {

    // Identifiant unique pour chaque vente
    @Id
    // BD auto-génère les IDs
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private int id;

    // Plusieurs venteProduit peuvent être liés à une seule vente
    @ManyToOne
    // Ceci précise que dans la table venteProduit, il y a une colonne vente_id pour
    // identifier la vente.
    @JoinColumn(name = "vente_id")
    @JsonIgnore
    private Vente vente;

    // Plusieurs produit peuvent être effectués dans une vente produit
    @ManyToOne
    // Ceci précise que dans la table venteProduit, il y a une colonne produit_id
    // pour identifier les produits.
    @JoinColumn(name = "produit_id")

    private Produit produit;
    private int quantite;

    // Constructeur par défaut requis par JPA
    public VenteProduit() { }

    // Constructeur avec paramètres pour instancier une VenteProduit
    public VenteProduit(Vente vente, Produit produit, int quantite) {
        this.vente = vente;
        this.produit = produit;
        this.quantite = quantite;
    }
    
    // Calcule le sous-total pour un produit retourné ou vendu
    // Multiplie le prix unitaire du produit par la quantité concernée
    // Retourne le montant total partiel pour cet item
    public double getSousTotal() { return produit.getPrix() * quantite; }

    // Getters / Setters
    public int getId() { return id; }

    public Vente getVente() { return vente; }
    public void setVente(Vente vente) { this.vente = vente; }

    public Produit getProduit() { return produit; }
    public void setProduit(Produit produit) { this.produit = produit; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }

    @Override
    public String toString() { return "VenteProduit{id=" + id + ", produit=" + produit.getNom() + ", quantite=" + quantite + "}"; }
}
