package com.log430.tp4.domain.transaction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Represents individual items within a transaction (sale or return).
 * Links to inventory items with quantity and pricing information.
 */
@Entity
@Table(name = "transaction_items")
public class TransactionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @Column(name = "inventory_item_id", nullable = false)
    private Long inventoryItemId; // Reference to InventoryItem

    @Column(nullable = false)
    private Integer quantite;

    @Column(name = "prix_unitaire", nullable = false)
    private Double prixUnitaire;

    @Column(name = "sous_total", nullable = false)
    private Double sousTotal;

    // Constructors
    public TransactionItem() {}

    public TransactionItem(Transaction transaction, Long inventoryItemId, Integer quantite, Double prixUnitaire) {
        this.transaction = transaction;
        this.inventoryItemId = inventoryItemId;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
        calculateSousTotal();
    }

    // Business methods
    public void updateQuantite(Integer nouvelleQuantite) {
        if (nouvelleQuantite < 0) {
            throw new IllegalArgumentException("La quantité ne peut pas être négative");
        }
        this.quantite = nouvelleQuantite;
        calculateSousTotal();
    }

    public void updatePrixUnitaire(Double nouveauPrix) {
        if (nouveauPrix < 0) {
            throw new IllegalArgumentException("Le prix unitaire ne peut pas être négatif");
        }
        this.prixUnitaire = nouveauPrix;
        calculateSousTotal();
    }

    private void calculateSousTotal() {
        this.sousTotal = quantite * prixUnitaire;
    }

    public Double getTotalValue() {
        return sousTotal;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Transaction getTransaction() { return transaction; }
    public void setTransaction(Transaction transaction) { this.transaction = transaction; }

    public Long getInventoryItemId() { return inventoryItemId; }
    public void setInventoryItemId(Long inventoryItemId) { this.inventoryItemId = inventoryItemId; }

    public Integer getQuantite() { return quantite; }
    public void setQuantite(Integer quantite) { 
        this.quantite = quantite;
        calculateSousTotal();
    }

    public Double getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(Double prixUnitaire) { 
        this.prixUnitaire = prixUnitaire;
        calculateSousTotal();
    }

    public Double getSousTotal() { return sousTotal; }

    @Override
    public String toString() {
        return "TransactionItem{" +
                "id=" + id +
                ", inventoryItemId=" + inventoryItemId +
                ", quantite=" + quantite +
                ", prixUnitaire=" + prixUnitaire +
                ", sousTotal=" + sousTotal +
                '}';
    }
}
