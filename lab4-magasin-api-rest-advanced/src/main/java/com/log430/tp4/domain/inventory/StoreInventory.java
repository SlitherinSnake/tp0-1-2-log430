package com.log430.tp4.domain.inventory;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Represents store-specific inventory levels for products.
 * Tracks local stock quantities and transfer requests.
 */
@Entity
@Table(name = "store_inventory", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"inventory_item_id", "store_id"})
       })
public class StoreInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_item_id", nullable = false)
    private InventoryItem inventoryItem;

    @Column(name = "store_id", nullable = false)
    private Long storeId; // Reference to Store domain

    @Column(name = "quantite_locale", nullable = false)
    private Integer quantiteLocale = 0;

    @Column(name = "quantite_demandee")
    private Integer quantiteDemandee;

    @Column(name = "date_demande")
    private LocalDate dateDemande;

    @Column(name = "date_derniere_maj")
    private LocalDate dateDerniereMaj;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_demande")
    private StatutDemande statutDemande = StatutDemande.AUCUNE;

    // Constructors
    public StoreInventory() {}

    public StoreInventory(InventoryItem inventoryItem, Long storeId, Integer quantiteLocale) {
        this.inventoryItem = inventoryItem;
        this.storeId = storeId;
        this.quantiteLocale = quantiteLocale;
        this.dateDerniereMaj = LocalDate.now();
    }

    // Business methods
    public boolean hasLocalStock(int quantite) {
        return quantiteLocale >= quantite;
    }

    public void decreaseLocalStock(int quantite) {
        if (!hasLocalStock(quantite)) {
            throw new IllegalArgumentException("Stock local insuffisant. Disponible: " + quantiteLocale + ", Demandé: " + quantite);
        }
        quantiteLocale -= quantite;
        dateDerniereMaj = LocalDate.now();
    }

    public void increaseLocalStock(int quantite) {
        if (quantite < 0) {
            throw new IllegalArgumentException("La quantité à ajouter doit être positive");
        }
        quantiteLocale += quantite;
        dateDerniereMaj = LocalDate.now();
    }

    public void requestStockTransfer(int quantite) {
        this.quantiteDemandee = quantite;
        this.dateDemande = LocalDate.now();
        this.statutDemande = StatutDemande.EN_ATTENTE;
    }

    public void approveStockTransfer() {
        if (statutDemande == StatutDemande.EN_ATTENTE && quantiteDemandee != null) {
            statutDemande = StatutDemande.APPROUVEE;
        }
    }

    public void rejectStockTransfer() {
        if (statutDemande == StatutDemande.EN_ATTENTE) {
            statutDemande = StatutDemande.REJETEE;
        }
    }

    public void completeStockTransfer() {
        if (statutDemande == StatutDemande.APPROUVEE && quantiteDemandee != null) {
            increaseLocalStock(quantiteDemandee);
            quantiteDemandee = null;
            dateDemande = null;
            statutDemande = StatutDemande.COMPLETEE;
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public InventoryItem getInventoryItem() { return inventoryItem; }
    public void setInventoryItem(InventoryItem inventoryItem) { this.inventoryItem = inventoryItem; }

    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }

    public Integer getQuantiteLocale() { return quantiteLocale; }
    public void setQuantiteLocale(Integer quantiteLocale) { 
        this.quantiteLocale = quantiteLocale;
        this.dateDerniereMaj = LocalDate.now();
    }

    public Integer getQuantiteDemandee() { return quantiteDemandee; }
    public void setQuantiteDemandee(Integer quantiteDemandee) { this.quantiteDemandee = quantiteDemandee; }

    public LocalDate getDateDemande() { return dateDemande; }
    public void setDateDemande(LocalDate dateDemande) { this.dateDemande = dateDemande; }

    public LocalDate getDateDerniereMaj() { return dateDerniereMaj; }
    public void setDateDerniereMaj(LocalDate dateDerniereMaj) { this.dateDerniereMaj = dateDerniereMaj; }

    public StatutDemande getStatutDemande() { return statutDemande; }
    public void setStatutDemande(StatutDemande statutDemande) { this.statutDemande = statutDemande; }

    @Override
    public String toString() {
        return "StoreInventory{" +
                "id=" + id +
                ", storeId=" + storeId +
                ", quantiteLocale=" + quantiteLocale +
                ", quantiteDemandee=" + quantiteDemandee +
                ", statutDemande=" + statutDemande +
                '}';
    }

    public enum StatutDemande {
        AUCUNE,
        EN_ATTENTE,
        APPROUVEE,
        REJETEE,
        COMPLETEE
    }
}
