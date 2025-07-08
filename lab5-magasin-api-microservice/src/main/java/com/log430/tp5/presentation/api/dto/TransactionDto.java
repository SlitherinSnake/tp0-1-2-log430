package com.log430.tp5.presentation.api.dto;

import java.util.List;

import com.log430.tp5.domain.transaction.Transaction;
import com.log430.tp5.domain.transaction.TransactionItem;

/**
 * Data Transfer Object for Transaction API responses.
 * Maps domain entities to frontend-expected format.
 */
public class TransactionDto {
    private Long id;
    private String date;
    private String clientName;
    private String client;
    private List<TransactionItemDto> items;
    private Double total;
    private String status;
    private String type;
    private Long personnelId;
    private Long storeId;

    private static final String DEFAULT_CLIENT = "Client";
    private static final String UNKNOWN = "UNKNOWN";

    // Default constructor
    public TransactionDto() {}

    // Constructor from domain entity
    public TransactionDto(Transaction transaction) {
        this.id = transaction.getId();
        this.date = transaction.getDateTransaction() != null ? 
                   transaction.getDateTransaction().toString() : null;
        this.clientName = DEFAULT_CLIENT;
        this.client = DEFAULT_CLIENT;
        this.items = transaction.getItems() != null ? 
                    transaction.getItems().stream()
                        .map(TransactionItemDto::fromEntity)
                        .toList() : List.of();
        this.total = transaction.getMontantTotal();
        this.status = transaction.getStatut() != null ? 
                     transaction.getStatut().name() : UNKNOWN;
        this.type = transaction.getTypeTransaction() != null ? 
                   transaction.getTypeTransaction().name() : UNKNOWN;
        this.personnelId = transaction.getPersonnelId();
        this.storeId = transaction.getStoreId();
    }

    // Constructor from domain entity with product name resolver
    public TransactionDto(Transaction transaction, java.util.function.LongFunction<String> productNameResolver) {
        this.id = transaction.getId();
        this.date = transaction.getDateTransaction() != null ? 
                   transaction.getDateTransaction().toString() : null;
        this.clientName = DEFAULT_CLIENT;
        this.client = DEFAULT_CLIENT;
        this.items = transaction.getItems() != null ? 
                    transaction.getItems().stream()
                        .map(item -> TransactionItemDto.fromEntity(item, productNameResolver.apply(item.getInventoryItemId())))
                        .toList() : List.of();
        this.total = transaction.getMontantTotal();
        this.status = transaction.getStatut() != null ? 
                     transaction.getStatut().name() : UNKNOWN;
        this.type = transaction.getTypeTransaction() != null ? 
                   transaction.getTypeTransaction().name() : UNKNOWN;
        this.personnelId = transaction.getPersonnelId();
        this.storeId = transaction.getStoreId();
    }

    // Static factory method
    public static TransactionDto fromEntity(Transaction transaction) {
        return new TransactionDto(transaction);
    }

    // Static factory method with product name resolver
    public static TransactionDto fromEntity(Transaction transaction, java.util.function.LongFunction<String> productNameResolver) {
        return new TransactionDto(transaction, productNameResolver);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getClient() { return client; }
    public void setClient(String client) { this.client = client; }

    public List<TransactionItemDto> getItems() { return items; }
    public void setItems(List<TransactionItemDto> items) { this.items = items; }

    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getPersonnelId() { return personnelId; }
    public void setPersonnelId(Long personnelId) { this.personnelId = personnelId; }

    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }

    // Inner DTO for transaction items
    public static class TransactionItemDto {
        private Long id;
        private String productName;
        private Integer quantity;
        private Double price;
        private Double sousTotal;

        public TransactionItemDto() {}

        /**
         * Constructor using TransactionItem and product name.
         */
        public TransactionItemDto(TransactionItem item, String productName) {
            this.id = item.getId();
            this.productName = productName;
            this.quantity = item.getQuantite();
            this.price = item.getPrixUnitaire();
            this.sousTotal = item.getSousTotal();
        }

        /**
         * Legacy constructor (kept for backward compatibility, uses default name).
         */
        public TransactionItemDto(TransactionItem item) {
            this(item, "Produit ID " + item.getInventoryItemId());
        }

        /**
         * Factory method to create DTO with product name.
         */
        public static TransactionItemDto fromEntity(TransactionItem item, String productName) {
            return new TransactionItemDto(item, productName);
        }

        public static TransactionItemDto fromEntity(TransactionItem item) {
            return new TransactionItemDto(item);
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
    }
}
