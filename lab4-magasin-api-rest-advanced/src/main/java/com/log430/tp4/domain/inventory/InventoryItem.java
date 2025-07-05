package com.log430.tp4.domain.inventory;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Unified Inventory domain combining Product information with stock management.
 * Represents a product with its central stock and store-specific stock levels.
 */
@Entity
@Table(name = "inventory_items")
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Product information
    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 50)
    private String categorie;

    @Column(nullable = false)
    private Double prix;

    @Column(length = 500)
    private String description;

    // Central stock information
    @Column(name = "stock_central", nullable = false)
    private Integer stockCentral = 0;

    @Column(name = "stock_minimum")
    private Integer stockMinimum = 0;

    @Column(name = "date_derniere_maj")
    private LocalDate dateDerniereMaj;

    @Column(nullable = false)
    private boolean isActive = true;

    // Constructors
    public InventoryItem() {}

    public InventoryItem(String nom, String categorie, Double prix, Integer stockCentral) {
        this.nom = nom;
        this.categorie = categorie;
        this.prix = prix;
        this.stockCentral = stockCentral;
        this.dateDerniereMaj = LocalDate.now();
    }

    // Business methods
    public boolean hasStock(int quantite) {
        return stockCentral >= quantite;
    }

    public void decreaseStock(int quantite) {
        if (!hasStock(quantite)) {
            throw new IllegalArgumentException("Stock central insuffisant. Disponible: " + stockCentral + ", Demandé: " + quantite);
        }
        stockCentral -= quantite;
        dateDerniereMaj = LocalDate.now();
    }

    public void increaseStock(int quantite) {
        if (quantite < 0) {
            throw new IllegalArgumentException("La quantité à ajouter doit être positive");
        }
        stockCentral += quantite;
        dateDerniereMaj = LocalDate.now();
    }

    public boolean needsRestock() {
        return stockCentral <= stockMinimum;
    }

    public Double calculateValue() {
        return prix * stockCentral;
    }

    public boolean isAvailable() {
        return isActive && stockCentral > 0;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public Double getPrix() { return prix; }
    public void setPrix(Double prix) { 
        this.prix = prix;
        this.dateDerniereMaj = LocalDate.now();
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getStockCentral() { return stockCentral; }
    public void setStockCentral(Integer stockCentral) { 
        this.stockCentral = stockCentral;
        this.dateDerniereMaj = LocalDate.now();
    }

    public Integer getStockMinimum() { return stockMinimum; }
    public void setStockMinimum(Integer stockMinimum) { this.stockMinimum = stockMinimum; }

    public LocalDate getDateDerniereMaj() { return dateDerniereMaj; }
    public void setDateDerniereMaj(LocalDate dateDerniereMaj) { this.dateDerniereMaj = dateDerniereMaj; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }

    @Override
    public String toString() {
        return "InventoryItem{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", categorie='" + categorie + '\'' +
                ", prix=" + prix +
                ", stockCentral=" + stockCentral +
                ", stockMinimum=" + stockMinimum +
                ", isActive=" + isActive +
                '}';
    }
}
