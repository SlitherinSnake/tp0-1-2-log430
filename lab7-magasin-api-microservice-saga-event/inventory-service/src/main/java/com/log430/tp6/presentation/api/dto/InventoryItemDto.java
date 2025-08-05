package com.log430.tp7.presentation.api.dto;

import com.log430.tp7.domain.inventory.InventoryItem;

/**
 * Data Transfer Object for InventoryItem API responses.
 * Maps domain entities to frontend-expected format.
 */
public class InventoryItemDto {
    private Long id;
    private String name;
    private String description;
    private String category;
    private String brand;
    private Double price;
    private Integer stock;
    private String status;
    private Long created; // timestamp for sorting

    // Default constructor
    public InventoryItemDto() {}

    // Constructor from domain entity
    public InventoryItemDto(InventoryItem item) {
        if (item == null) {
            throw new IllegalArgumentException("InventoryItem cannot be null");
        }
        
        this.id = item.getId();
        this.name = item.getNom();
        this.description = item.getDescription();
        this.category = item.getCategorie();
        this.brand = ""; // Not available in current model
        this.price = item.getPrix();
        this.stock = item.getStockCentral();
        this.status = item.isActive() ? "active" : "inactive";
        this.created = item.getId(); // Use ID as a proxy for creation order
    }

    // Static factory method
    public static InventoryItemDto fromEntity(InventoryItem item) {
        return new InventoryItemDto(item);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getCreated() { return created; }
    public void setCreated(Long created) { this.created = created; }
}
