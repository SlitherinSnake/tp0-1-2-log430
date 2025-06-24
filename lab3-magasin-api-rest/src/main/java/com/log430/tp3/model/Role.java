package com.log430.tp3.model;

import jakarta.persistence.*;

// Annotation JPA indiquant que cette classe est une entité persistante
@Entity
// Nom de la table dans la base de données
@Table(name = "roles")
public class Role {
    
    // Identifiant unique pour chaque retour
    @Id
    // BD auto-génère les IDs
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    /**
     * Nom fonctionnel du rôle.
     * Enum stocké en base sous forme de VARCHAR
     * Longueur maximale fixée à 20 caractères
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ERole name;
    
    // Constructeur par défaut requis par JPA
    public Role() {}
    
    /** Constructeur pratique pour créer un rôle avec son nom. */
    public Role(ERole name) { this.name = name;}
    
    // Getters / Setters
    public Integer getId() { return id;}
    public void setId(Integer id) {this.id = id; }
    
    public ERole getName() { return name;}
    public void setName(ERole name) {this.name = name; }
    
    /**
     * Catalogue des rôles possibles.  
     * On utilise l’ENUM pour :
     * Éviter les fautes de frappe dans le code
     * Faciliter l’évolution (ajout/suppression de rôles)
     */
    public enum ERole { ROLE_ADMIN, ROLE_EMPLOYEE, ROLE_VIEWER }
} 