package com.log430.tp1.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

// Annotation JPA indiquent que la classe est une entité persistante
@Entity
// Nom de la table dans la bd
@Table(name = "retours")
public class Retour {

    // Identifiant unique pour chaque retour
    @Id
    // BD auto-génère les IDs
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private LocalDate dateRetour;

    // Plusieurs retours peuvent être liés à une seule vente
    @ManyToOne
    // Ceci précise que dans la table retour, il y a une colonne vente_id pour
    // identifier la vente.
    @JoinColumn(name = "vente_id")
    private Vente vente;

    // Plusieurs retours peuvent être effectués par un même employé
    @ManyToOne
    // Ceci précise que dans la table retour, il y a une colonne employe_id pour
    // identifier l'employe.
    @JoinColumn(name = "employe_id")
    private Employe employe;

    // Plusieurs produit sont dans un seul retour donc
    // c'est la liste des produits associés à ce retour, avec ca quantité (via l'entité RetourProduit)
    @OneToMany(mappedBy = "retour", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RetourProduit> retourProduits = new java.util.ArrayList<>();

    // Constructeur par défaut requis par JPA
    public Retour() {
    }

    // Constructeur avec paramètres pour instancier un retour
    public Retour(LocalDate dateRetour, Vente vente, Employe employe, List<RetourProduit> retourProduits) {
        this.dateRetour = dateRetour;
        this.vente = vente;
        this.employe = employe;
        this.retourProduits = retourProduits;
    }

    // Getters / Setters
    public int getId() {
        return id;
    }

    public LocalDate getDateRetour() {
        return dateRetour;
    }

    public void setDateRetour(LocalDate dateRetour) {
        this.dateRetour = dateRetour;
    }

    public Vente getVente() {
        return vente;
    }

    public void setVente(Vente vente) {
        this.vente = vente;
    }

    public Employe getEmploye() {
        return employe;
    }

    public void setEmploye(Employe employe) {
        this.employe = employe;
    }

    public List<RetourProduit> getProduitsRetournes() {
        return retourProduits;
    }

    public void setProduitsRetournes(List<RetourProduit> retourProduits) {
        this.retourProduits = retourProduits;
    }

    @Override
    public String toString() {
        return "Retour{id=" + id +
                ", dateRetour=" + dateRetour +
                ", vente=" + vente +
                ", employe=" + employe +
                ", produitsRetournes=" + retourProduits + "}";
    }
}
