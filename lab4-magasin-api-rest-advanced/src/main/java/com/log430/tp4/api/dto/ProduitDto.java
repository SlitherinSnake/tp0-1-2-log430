package com.log430.tp4.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * ProduitDto = “carte d’identité” simplifiée d’un produit
 * envoyée ou reçue par l’API (Data Transfer Object).
 * Pourquoi un DTO ?
 *   • On contrôle exactement les champs exposés au client web/app.  
 *   • On applique facilement des règles de validation (prix positif, nom non vide…).  
 *   • On évite de révéler des détails internes de l’entité JPA.
 */
public class ProduitDto {
    
    private Integer id;
    
    // Nom obligatoire : pas vide, pas d’espaces seulement
    @NotBlank(message = "Le nom du produit est obligatoire")
    private String nom;
    
    // Catégorie obligatoire
    @NotBlank(message = "La catégorie est obligatoire")
    private String categorie;
    
    // Prix obligatoire et forcément positif
    @NotNull(message = "Le prix est obligatoire")
    @Positive(message = "Le prix doit être positif")
    private Float prix;
    
    // Quantité obligatoire, ne peut jamais être négative
    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 0, message = "La quantité ne peut pas être négative")
    private Integer quantite;
    
    // Constructeur par défaut requis par JPA
    public ProduitDto() {}
    
    /**
     * Constructeur pratique pour créer le DTO en une seule ligne.
     * @param id        identifiant
     * @param nom       nom lisible du produit
     * @param categorie famille du produit (ex. « Électronique »)
     * @param prix      prix unitaire
     * @param quantite  stock disponible
     */
    public ProduitDto(Integer id, String nom, String categorie, Float prix, Integer quantite) {
        this.id = id;
        this.nom = nom;
        this.categorie = categorie;
        this.prix = prix;
        this.quantite = quantite;
    }

    // Getter/Setter
    public Integer getId() {   return id;}
    public void setId(Integer id) {  this.id = id;}

    public String getNom() {   return nom;}
    public void setNom(String nom) {    this.nom = nom;}

    public String getCategorie() {    return categorie;}
    public void setCategorie(String categorie) {   this.categorie = categorie;}

    public Float getPrix() {   return prix;}
    public void setPrix(Float prix) {    this.prix = prix;}

    public Integer getQuantite() {   return quantite;}
    public void setQuantite(Integer quantite) {   this.quantite = quantite;}
} 