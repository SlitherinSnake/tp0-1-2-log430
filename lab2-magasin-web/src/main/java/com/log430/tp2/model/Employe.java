package com.log430.tp2.model;

import jakarta.persistence.*;

// Annotation JPA indiquant que cette classe est une entité persistante
@Entity
// Nom de la table dans la base de données
@Table(name = "employes")
public class Employe {

    // Identifiant unique pour chaque employé
    @Id
    // BD auto-génère les IDs
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String nom;

    private String identifiant;

    // Constructeur par défaut requis par JPA
    public Employe() {
    }

    // Constructeur avec paramètres pour instancier un employé 
    public Employe(String nom, String identifiant) {
        this.nom = nom;
        this.identifiant = identifiant;
    }

    // Getter/Setter
    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getIdentifiant() {
        return identifiant;
    }

    public void setIdentifiant(String identifiant) {
        this.identifiant = identifiant;
    }

    @Override
    public String toString() {
        return "Employe{id=" + id + ", nom='" + nom + "', identifiant='" + identifiant + "'}";
    }
}
