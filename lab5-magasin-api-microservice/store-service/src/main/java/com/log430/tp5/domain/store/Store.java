package com.log430.tp5.domain.store;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Represents a physical store location in the retail chain.
 * Stores can have sales, returns, and inventory.
 */
@Entity
@Table(name = "stores")
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String quartier;

    @Column(length = 255)
    private String adresse;

    @Column(length = 20)
    private String telephone;

    @Column(nullable = false)
    private boolean isActive = true;

    // Constructors
    public Store() {}

    public Store(String nom, String quartier) {
        this.nom = nom;
        this.quartier = quartier;
    }

    public Store(String nom, String quartier, String adresse, String telephone) {
        this.nom = nom;
        this.quartier = quartier;
        this.adresse = adresse;
        this.telephone = telephone;
    }

    // Business methods
    public String getFullLocation() {
        return nom + " (" + quartier + ")";
    }

    public boolean isOperational() {
        return isActive;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getQuartier() { return quartier; }
    public void setQuartier(String quartier) { this.quartier = quartier; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    @Override
    public String toString() {
        return "Store{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", quartier='" + quartier + '\'' +
                ", adresse='" + adresse + '\'' +
                ", telephone='" + telephone + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
