package com.log430.tp1.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

// Annotation JPA indiquent que la classe est une entité persistante
@Entity
// Nom de la table dans la bd
@Table(name = "ventes")
public class Vente {

    // Identifiant unique pour chaque vente
    @Id
    // BD auto-génère les IDs
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private LocalDate dateVente;

    private float montantTotal;

    // plusieurs ventes peuvent être fait par un même employé.
    @ManyToOne
    // Ceci précise que dans la table vente, il y a une colonne employe_id pour identifier l'employe.
    @JoinColumn(name = "employe_id")
    private Employe employe;

    // plusieurs produits peuvent appartenir à une vente, 
    // et chaque produit peut apparaître dans plusieurs ventes
    @ManyToMany
    // Ceci est une table de jointure qui permettra de lier les IDs des produits aux IDs des ventes
    @JoinTable(
        name = "vente_produits",
        joinColumns = @JoinColumn(name = "vente_id"),
        inverseJoinColumns = @JoinColumn(name = "produit_id")
    )
    private List<Produit> produits;

    // Constructeur par défaut requis par JPA
    public Vente() {
    }

    // Constructeur avec paramètre pour instancier une vente
    public Vente(LocalDate dateVente, float montantTotal, Employe employe, List<Produit> produits) {
        this.dateVente = dateVente;
        this.montantTotal = montantTotal;
        this.employe = employe;
        this.produits = produits;
    }

    // Getters / Setters
    public int getId() {
        return id;
    }

    public LocalDate getDateVente() {
        return dateVente;
    }

    public void setDateVente(LocalDate dateVente) {
        this.dateVente = dateVente;
    }

    public float getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(float montantTotal) {
        this.montantTotal = montantTotal;
    }

    public Employe getEmploye() {
        return employe;
    }

    public void setEmploye(Employe employe) {
        this.employe = employe;
    }

    public List<Produit> getProduits() {
        return produits;
    }

    public void setProduits(List<Produit> produits) {
        this.produits = produits;
    }

    @Override
    public String toString() {
        return "Vente{id=" + id + ", dateVente=" + dateVente +
               ", montantTotal=" + montantTotal +
               ", employe=" + employe +
               ", produits=" + produits + "}";
    }
}
