package com.log430.tp7.dto;

/**
 * DTO that matches the structure returned by the inventory service API.
 * This is used to deserialize the API response before mapping to ProductDTO.
 */
public class InventoryApiResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String category;
    private String brand;
    private Double price;
    private Integer stock;
    private String status;
    private Long created;

    // Default constructor required for JSON deserialization
    public InventoryApiResponseDTO() {
        // Empty constructor for Jackson JSON deserialization
    }

    // Convert to ProductDTO with proper field mapping
    public ProductDTO toProductDTO() {
        return new ProductDTO(
            this.id,
            this.name,           // name -> nom
            this.description,
            this.category,       // category -> categorie
            this.price,          // price -> prix
            this.stock,          // stock -> stockCentral
            "active".equals(this.status), // status -> isActive
            this.brand,          // brand -> marque
            this.status
        );
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
