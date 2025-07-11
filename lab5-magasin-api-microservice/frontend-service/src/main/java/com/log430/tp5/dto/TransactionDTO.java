package com.log430.tp5.dto;

import java.time.LocalDateTime;
import java.util.List;

public class TransactionDTO {
    private Long id;
    private String date;
    private Long personnelId;
    private Long storeId;
    private String type;
    private String status;
    private Double total;
    private List<TransactionItemDTO> items;

    // Constructors
    public TransactionDTO() {
        // Default constructor for JSON deserialization
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public Long getPersonnelId() { return personnelId; }
    public void setPersonnelId(Long personnelId) { this.personnelId = personnelId; }

    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }

    public List<TransactionItemDTO> getItems() { return items; }
    public void setItems(List<TransactionItemDTO> items) { this.items = items; }

    // Legacy getters for backward compatibility
    public String getTypeTransaction() { return type; }
    public void setTypeTransaction(String type) { this.type = type; }
    
    public String getStatut() { return status; }
    public void setStatut(String status) { this.status = status; }
    
    public Double getMontantTotal() { return total; }
    public void setMontantTotal(Double total) { this.total = total; }
    
    public LocalDateTime getDateTransaction() { 
        // For backward compatibility, return null since we're now using String date
        return null; 
    }
    public void setDateTransaction(LocalDateTime dateTransaction) { 
        // For backward compatibility, convert to string
        this.date = dateTransaction != null ? dateTransaction.toString() : null;
    }

    // Nested class for transaction items
    public static class TransactionItemDTO {
        private Long id;
        private String productName;
        private Integer quantity;
        private Double price;
        private Double sousTotal;

        public TransactionItemDTO() {
            // Default constructor for JSON deserialization
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }

        public Double getSousTotal() { return sousTotal; }
        public void setSousTotal(Double sousTotal) { this.sousTotal = sousTotal; }

        // Legacy getters for backward compatibility
        public Long getInventoryItemId() { return id; }
        public void setInventoryItemId(Long inventoryItemId) { this.id = inventoryItemId; }

        public String getItemName() { return productName; }
        public void setItemName(String itemName) { this.productName = itemName; }

        public Integer getQuantite() { return quantity; }
        public void setQuantite(Integer quantite) { this.quantity = quantite; }

        public Double getPrixUnitaire() { return price; }
        public void setPrixUnitaire(Double prixUnitaire) { this.price = prixUnitaire; }
    }
}
