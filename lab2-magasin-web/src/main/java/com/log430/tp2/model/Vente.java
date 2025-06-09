package com.log430.tp2.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.*;

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

    private Double montantTotal;

    // plusieurs ventes peuvent être fait par un même employé.
    @ManyToOne
    // Ceci précise que dans la table vente, il y a une colonne employe_id pour
    // identifier l'employe.
    @JoinColumn(name = "employe_id")
    private Employe employe;

    // Constructeur par défaut requis par JPA
    public Vente() {
    }

    // Constructeur avec paramètre pour instancier une vente
    public Vente(LocalDate dateVente, Double montantTotal, Employe employe, List<VenteProduit> venteProduits) {
        this.dateVente = dateVente;
        this.montantTotal = montantTotal;
        this.employe = employe;
        this.venteProduits = venteProduits;
    }

    // Plusieurs produit sont dans une seule vente donc
    // c'est la liste des produits associés à cette vente, avec ca quantité (via
    // l'entité VenteProduit)
    @OneToMany(mappedBy = "vente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VenteProduit> venteProduits = new ArrayList<>();

    public void ajouterProduit(Produit produit, int quantite) {
        for (VenteProduit vp : venteProduits) {
            if (vp.getProduit().getId() == produit.getId()) {
                vp.setQuantite(vp.getQuantite() + quantite);
                return;
            }
        }
        VenteProduit nv = new VenteProduit(this, produit, quantite);
        venteProduits.add(nv);
    }

    // Supprimer un produit de la vente
    public void removeProduit(int produitId) {
        venteProduits.removeIf(vp -> vp.getProduit().getId() == produitId);
    }

    public List<VenteProduit> getItems() {
        return venteProduits;
    }

    // Méthode appelée lors de la validation de la vente
    public void calculerMontantTotal() {
        montantTotal = venteProduits.stream()
                .mapToDouble(vp -> vp.getProduit().getPrix() * vp.getQuantite())
                .sum();
    }

    // Initialisation dateVente automatiquement à la date du jour
    public void setDateVenteIfNull() {
        if (this.dateVente == null) {
            this.dateVente = LocalDate.now();
        }
    }

    // Getters / Setters
    public int getId() {
        return id;
    }

    // Pour test
    public int setId(int id) {
        return this.id = id;
    }

    public LocalDate getDateVente() {
        return dateVente;
    }

    public void setDateVente(LocalDate dateVente) {
        this.dateVente = dateVente;
    }

    public Double getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(Double montantTotal) {
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
