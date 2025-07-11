package com.log430.tp5.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProductDTO {
    private Long id;
    private String nom;
    private String description;
    private String categorie;
    private Double prix;
    private Integer stockCentral;
    private Boolean isActive;
    private String marque;
    private String status;

    // Constructors
    public ProductDTO() {}

    public ProductDTO(Long id, String nom, String description, String categorie, 
                     Double prix, Integer stockCentral, Boolean isActive, String marque, String status) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.categorie = categorie;
        this.prix = prix;
        this.stockCentral = stockCentral;
        this.isActive = isActive;
        this.marque = marque;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public Double getPrix() { return prix; }
    public void setPrix(Double prix) { this.prix = prix; }

    public Integer getStockCentral() { return stockCentral; }
    public void setStockCentral(Integer stockCentral) { this.stockCentral = stockCentral; }

    @JsonProperty("isActive")
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public String getMarque() { return marque; }
    public void setMarque(String marque) { this.marque = marque; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
