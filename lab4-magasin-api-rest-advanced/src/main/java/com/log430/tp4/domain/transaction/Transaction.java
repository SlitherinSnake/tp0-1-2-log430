package com.log430.tp4.domain.transaction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Unified Transaction domain representing both sales and returns.
 * Uses transaction type to distinguish between different operations.
 */
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_transaction", nullable = false)
    private TypeTransaction typeTransaction;

    @Column(name = "date_transaction", nullable = false)
    private LocalDate dateTransaction;

    @Column(name = "montant_total")
    private Double montantTotal = 0.0;

    @Column(name = "personnel_id", nullable = false)
    private Long personnelId; // Reference to Personnel domain

    @Column(name = "store_id", nullable = false)
    private Long storeId; // Reference to Store domain

    // For returns: reference to original sale transaction
    @Column(name = "transaction_originale_id")
    private Long transactionOriginaleId;

    @Column(name = "motif_retour")
    private String motifRetour; // Only for returns

    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    private StatutTransaction statut = StatutTransaction.EN_COURS;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransactionItem> items = new ArrayList<>();

    // Constructors
    public Transaction() {}

    // Constructor for sale
    public Transaction(Long personnelId, Long storeId) {
        this.typeTransaction = TypeTransaction.VENTE;
        this.dateTransaction = LocalDate.now();
        this.personnelId = personnelId;
        this.storeId = storeId;
    }

    // Constructor for return
    public Transaction(Long personnelId, Long storeId, Long transactionOriginaleId, String motifRetour) {
        this.typeTransaction = TypeTransaction.RETOUR;
        this.dateTransaction = LocalDate.now();
        this.personnelId = personnelId;
        this.storeId = storeId;
        this.transactionOriginaleId = transactionOriginaleId;
        this.motifRetour = motifRetour;
    }

    // Business methods
    public void addItem(Long inventoryItemId, Integer quantite, Double prixUnitaire) {
        // Check if item already exists in transaction
        for (TransactionItem item : items) {
            if (item.getInventoryItemId().equals(inventoryItemId)) {
                item.updateQuantite(item.getQuantite() + quantite);
                recalculateTotal();
                return;
            }
        }
        
        // Add new item
        TransactionItem newItem = new TransactionItem(this, inventoryItemId, quantite, prixUnitaire);
        items.add(newItem);
        recalculateTotal();
    }

    public void removeItem(Long inventoryItemId) {
        items.removeIf(item -> item.getInventoryItemId().equals(inventoryItemId));
        recalculateTotal();
    }

    public void updateItemQuantity(Long inventoryItemId, Integer nouvelleQuantite) {
        for (TransactionItem item : items) {
            if (item.getInventoryItemId().equals(inventoryItemId)) {
                item.updateQuantite(nouvelleQuantite);
                recalculateTotal();
                return;
            }
        }
    }

    public void recalculateTotal() {
        this.montantTotal = items.stream()
                .mapToDouble(TransactionItem::getSousTotal)
                .sum();
    }

    public void complete() {
        if (statut == StatutTransaction.EN_COURS) {
            recalculateTotal();
            statut = StatutTransaction.COMPLETEE;
        }
    }

    public void cancel() {
        if (statut == StatutTransaction.EN_COURS) {
            statut = StatutTransaction.ANNULEE;
        }
    }

    public boolean isSale() {
        return typeTransaction == TypeTransaction.VENTE;
    }

    public boolean isReturn() {
        return typeTransaction == TypeTransaction.RETOUR;
    }

    public boolean isCompleted() {
        return statut == StatutTransaction.COMPLETEE;
    }

    public Integer getTotalItems() {
        return items.stream()
                .mapToInt(TransactionItem::getQuantite)
                .sum();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TypeTransaction getTypeTransaction() { return typeTransaction; }
    public void setTypeTransaction(TypeTransaction typeTransaction) { this.typeTransaction = typeTransaction; }

    public LocalDate getDateTransaction() { return dateTransaction; }
    public void setDateTransaction(LocalDate dateTransaction) { this.dateTransaction = dateTransaction; }

    public Double getMontantTotal() { return montantTotal; }
    public void setMontantTotal(Double montantTotal) { this.montantTotal = montantTotal; }

    public Long getPersonnelId() { return personnelId; }
    public void setPersonnelId(Long personnelId) { this.personnelId = personnelId; }

    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }

    public Long getTransactionOriginaleId() { return transactionOriginaleId; }
    public void setTransactionOriginaleId(Long transactionOriginaleId) { this.transactionOriginaleId = transactionOriginaleId; }

    public String getMotifRetour() { return motifRetour; }
    public void setMotifRetour(String motifRetour) { this.motifRetour = motifRetour; }

    public StatutTransaction getStatut() { return statut; }
    public void setStatut(StatutTransaction statut) { this.statut = statut; }

    public List<TransactionItem> getItems() { return items; }
    public void setItems(List<TransactionItem> items) { this.items = items; }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", typeTransaction=" + typeTransaction +
                ", dateTransaction=" + dateTransaction +
                ", montantTotal=" + montantTotal +
                ", personnelId=" + personnelId +
                ", storeId=" + storeId +
                ", statut=" + statut +
                '}';
    }

    public enum TypeTransaction {
        VENTE,
        RETOUR
    }

    public enum StatutTransaction {
        EN_COURS,
        COMPLETEE,
        ANNULEE
    }
}
