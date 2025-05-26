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
    // Ceci précise que dans la table retour, il y a une colonne vente_id pour identifier la vente.
    @JoinColumn(name = "vente_id")
    private Vente vente;

    // Plusieurs retours peuvent être effectués par un même employé
    @ManyToOne
    // Ceci précise que dans la table retour, il y a une colonne employe_id pour identifier l'employe.
    @JoinColumn(name = "employe_id")
    private Employe employe;

    // Plusieurs produits peuvent être retournés dans un même retour,
    // et un même produit peut être retourné dans plusieurs retours
    @ManyToMany
    // Ceci est une table de jointure qui permettra de lier les IDs des produits aux IDs des retours
    @JoinTable(
        name = "retour_produits",
        joinColumns = @JoinColumn(name = "retour_id"),
        inverseJoinColumns = @JoinColumn(name = "produit_id")
    )
    private List<Produit> produitsRetournes;

    // Constructeur par défaut requis par JPA
    public Retour() {
    }

    // Constructeur avec paramètres pour instancier un retour 
    public Retour(LocalDate dateRetour, Vente vente, Employe employe, List<Produit> produitsRetournes) {
        this.dateRetour = dateRetour;
        this.vente = vente;
        this.employe = employe;
        this.produitsRetournes = produitsRetournes;
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

    public List<Produit> getProduitsRetournes() {
        return produitsRetournes;
    }

    public void setProduitsRetournes(List<Produit> produitsRetournes) {
        this.produitsRetournes = produitsRetournes;
    }

    @Override
    public String toString() {
        return "Retour{id=" + id +
               ", dateRetour=" + dateRetour +
               ", vente=" + vente +
               ", employe=" + employe +
               ", produitsRetournes=" + produitsRetournes + "}";
    }
}
