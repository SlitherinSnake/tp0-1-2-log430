package com.log430.tp7.domain.inventory.readmodel;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Read model for inventory queries optimized for performance.
 * This model is updated by inventory events and provides fast read access.
 */
@Entity
@Table(name = "inventory_read_model")
public class InventoryReadModel {
    
    @Id
    @Column(name = "inventory_item_id")
    private Long inventoryItemId;
    
    @Column(nullable = false, length = 100)
    private String nom;
    
    @Column(nullable = false, length = 50)
    private String categorie;
    
    @Column(nullable = false)
    private Double prix;
    
    @Column(length = 500)
    private String description;
    
    @Column(name = "stock_central", nullable = false)
    private Integer stockCentral = 0;
    
    @Column(name = "stock_reserved", nullable = false)
    private Integer stockReserved = 0;
    
    @Column(name = "stock_available", nullable = false)
    private Integer stockAvailable = 0;
    
    @Column(name = "stock_minimum")
    private Integer stockMinimum = 0;
    
    @Column(name = "date_derniere_maj")
    private LocalDate dateDerniereMaj;
    
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
    
    @Column(nullable = false)
    private boolean isActive = true;
    
    @Column(name = "total_value")
    private Double totalValue = 0.0;
    
    @Column(name = "needs_restock")
    private boolean needsRestock = false;
    
    // Constructors
    public InventoryReadModel() {}
    
    public InventoryReadModel(Long inventoryItemId, String nom, String categorie, Double prix, 
                             Integer stockCentral, Integer stockMinimum, boolean isActive) {
        this.inventoryItemId = inventoryItemId;
        this.nom = nom;
        this.categorie = categorie;
        this.prix = prix;
        this.stockCentral = stockCentral;
        this.stockReserved = 0;
        this.stockAvailable = stockCentral;
        this.stockMinimum = stockMinimum;
        this.dateDerniereMaj = LocalDate.now();
        this.lastUpdated = LocalDateTime.now();
        this.isActive = isActive;
        this.totalValue = prix * stockCentral;
        this.needsRestock = stockCentral <= stockMinimum;
    }
    
    // Business methods
    public void updateStock(Integer newStockCentral, Integer newStockReserved) {
        this.stockCentral = newStockCentral;
        this.stockReserved = newStockReserved;
        this.stockAvailable = newStockCentral - newStockReserved;
        this.totalValue = prix * newStockCentral;
        this.needsRestock = newStockCentral <= stockMinimum;
        this.lastUpdated = LocalDateTime.now();
        this.dateDerniereMaj = LocalDate.now();
    }
    
    public void updatePrice(Double newPrice) {
        this.prix = newPrice;
        this.totalValue = newPrice * stockCentral;
        this.lastUpdated = LocalDateTime.now();
    }
    
    public void updateBasicInfo(String nom, String categorie, String description) {
        this.nom = nom;
        this.categorie = categorie;
        this.description = description;
        this.lastUpdated = LocalDateTime.now();
    }
    
    public void updateActiveStatus(boolean isActive) {
        this.isActive = isActive;
        this.lastUpdated = LocalDateTime.now();
    }
    
    public void recalculateValues() {
        this.stockAvailable = stockCentral - stockReserved;
        this.totalValue = prix * stockCentral;
        this.needsRestock = stockCentral <= stockMinimum;
        this.lastUpdated = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getInventoryItemId() { return inventoryItemId; }
    public void setInventoryItemId(Long inventoryItemId) { this.inventoryItemId = inventoryItemId; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    
    public Double getPrix() { return prix; }
    public void setPrix(Double prix) { this.prix = prix; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Integer getStockCentral() { return stockCentral; }
    public void setStockCentral(Integer stockCentral) { this.stockCentral = stockCentral; }
    
    public Integer getStockReserved() { return stockReserved; }
    public void setStockReserved(Integer stockReserved) { this.stockReserved = stockReserved; }
    
    public Integer getStockAvailable() { return stockAvailable; }
    public void setStockAvailable(Integer stockAvailable) { this.stockAvailable = stockAvailable; }
    
    public Integer getStockMinimum() { return stockMinimum; }
    public void setStockMinimum(Integer stockMinimum) { this.stockMinimum = stockMinimum; }
    
    public LocalDate getDateDerniereMaj() { return dateDerniereMaj; }
    public void setDateDerniereMaj(LocalDate dateDerniereMaj) { this.dateDerniereMaj = dateDerniereMaj; }
    
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }
    
    public Double getTotalValue() { return totalValue; }
    public void setTotalValue(Double totalValue) { this.totalValue = totalValue; }
    
    public boolean isNeedsRestock() { return needsRestock; }
    public void setNeedsRestock(boolean needsRestock) { this.needsRestock = needsRestock; }
    
    @Override
    public String toString() {
        return String.format("InventoryReadModel{inventoryItemId=%d, nom='%s', stockCentral=%d, stockReserved=%d, stockAvailable=%d, isActive=%s}", 
                           inventoryItemId, nom, stockCentral, stockReserved, stockAvailable, isActive);
    }
}