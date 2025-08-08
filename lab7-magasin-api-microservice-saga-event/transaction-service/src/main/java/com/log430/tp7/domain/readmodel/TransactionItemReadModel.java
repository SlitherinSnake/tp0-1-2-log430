package com.log430.tp7.domain.readmodel;

import jakarta.persistence.*;

/**
 * Read model for transaction items optimized for query operations.
 * Part of the CQRS implementation for transaction read operations.
 */
@Entity
@Table(name = "transaction_item_read_model")
public class TransactionItemReadModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private TransactionReadModel transactionReadModel;
    
    @Column(name = "inventory_item_id", nullable = false)
    private Long inventoryItemId;
    
    @Column(name = "product_name")
    private String productName; // Denormalized for faster queries
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Column(name = "unit_price", nullable = false)
    private Double unitPrice;
    
    @Column(name = "subtotal", nullable = false)
    private Double subtotal;
    
    @Column(name = "category")
    private String category; // Denormalized for reporting
    
    // Constructors
    public TransactionItemReadModel() {}
    
    public TransactionItemReadModel(TransactionReadModel transactionReadModel, Long inventoryItemId,
                                   String productName, Integer quantity, Double unitPrice, 
                                   Double subtotal, String category) {
        this.transactionReadModel = transactionReadModel;
        this.inventoryItemId = inventoryItemId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
        this.category = category;
    }
    
    // Business methods
    public void updateQuantity(Integer newQuantity) {
        this.quantity = newQuantity;
        this.subtotal = newQuantity * this.unitPrice;
    }
    
    public void updatePrice(Double newPrice) {
        this.unitPrice = newPrice;
        this.subtotal = this.quantity * newPrice;
    }
    
    public void updateProductInfo(String productName, String category) {
        this.productName = productName;
        this.category = category;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public TransactionReadModel getTransactionReadModel() { return transactionReadModel; }
    public void setTransactionReadModel(TransactionReadModel transactionReadModel) { this.transactionReadModel = transactionReadModel; }
    
    public Long getInventoryItemId() { return inventoryItemId; }
    public void setInventoryItemId(Long inventoryItemId) { this.inventoryItemId = inventoryItemId; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }
    
    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    @Override
    public String toString() {
        return "TransactionItemReadModel{" +
                "id=" + id +
                ", inventoryItemId=" + inventoryItemId +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", subtotal=" + subtotal +
                ", category='" + category + '\'' +
                '}';
    }
}