package com.log430.tp4.model;

import jakarta.persistence.*;
import java.util.List;

// Annotation JPA indiquent que la classe est une entité persistante
@Entity
// Nom de la table dans la bd
@Table(name = "magasin")
public class Magasin {

    // Identifiant unique pour chaque vente
    @Id
    // BD auto-génère les IDs
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    
    private int id;
    private String nom;
    private String quartier;

    // Une Magasin peut être associée à plusieurs ventes.
    @OneToMany(mappedBy = "magasin")
    // Cela signifie qu’un magasin peut réaliser plusieurs ventes différentes.
    private List<Vente> ventes;

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getQuartier() { return quartier; }
    public void setQuartier(String quartier) { this.quartier = quartier; }

    public List<Vente> getVentes() { return ventes; }
    public void setVentes(List<Vente> ventes) { this.ventes = ventes; }
}
