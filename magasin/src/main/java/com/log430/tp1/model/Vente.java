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
    // Ceci précise que dans la table vente, il y a une colonne employe_id pour
    // identifier l'employe.
    @JoinColumn(name = "employe_id")
    private Employe employe;
    
    // Plusieurs produit sont dans une seule vente donc
    // c'est la liste des produits associés à cette vente, avec ca quantité (via l'entité VenteProduit)
    @OneToMany(mappedBy = "vente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VenteProduit> venteProduits = new java.util.ArrayList<>();

    // Constructeur par défaut requis par JPA
    public Vente() {
    }

    // Constructeur avec paramètre pour instancier une vente
    public Vente(LocalDate dateVente, float montantTotal, Employe employe, List<VenteProduit> venteProduits) {
        this.dateVente = dateVente;
        this.montantTotal = montantTotal;
        this.employe = employe;
        this.venteProduits = venteProduits;
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

    public List<VenteProduit> getVenteProduits() {
        return venteProduits;
    }

    public void setVenteProduits(List<VenteProduit> venteProduits) {
        this.venteProduits = venteProduits;
    }

    @Override
    public String toString() {
        return "Vente{id=" + id + ", dateVente=" + dateVente +
                ", montantTotal=" + montantTotal +
                ", employe=" + employe +
                ", produits=" + venteProduits + "}";
    }
}
