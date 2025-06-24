package com.log430.tp3.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;


/**
 * Représente un utilisateur du système (compte de connexion).
 * Chaque utilisateur peut posséder plusieurs rôles via la table
 * d’association user_roles
 */
@Entity
@Table(name = "users", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = "username") // Un login doit être unique
       })
public class User {

    // Identifiant unique pour chaque retour
    @Id
    // BD auto-génère les IDs
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nom d’utilisateur (login) – non nul, 50 car. max
    @Column(nullable = false, length = 50)
    private String username;
    
    // Mot de passe chiffré – non nul
    @Column(nullable = false)
    private String password;
    
     /**
     * Rôles associés à l’utilisateur.
     * fetch = EAGER : on charge les rôles dès qu’on
     * récupère un utilisateur (utile pour la sécurité Spring Security).  
     * La table user_roles matérialise la relation many-to-many.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", 
              joinColumns = @JoinColumn(name = "user_id"),
              inverseJoinColumns = @JoinColumn(name = "role_id")) 
    private Set<Role> roles = new HashSet<>();

    // Constructeur par défaut requis par JPA
    public User() {
    }

    /**
     * Constructeur pratique pour créer un utilisateur
     * avec son login et son mot de passe.
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

     // Getters / Setters
    public Long getId() {return id;}
    public void setId(Long id) {  this.id = id;}

    public String getUsername() {   return username;}
    public void setUsername(String username) {    this.username = username;}

    public String getPassword() {   return password;}
    public void setPassword(String password) {   this.password = password;}

    public Set<Role> getRoles() {   return roles;}
    public void setRoles(Set<Role> roles) {    this.roles = roles;}

    @Override
    public String toString() {
        return "User{id=" + id +
               ", username='" + username + '\'' +
               ", roles=" + roles + '}';
    }
} 