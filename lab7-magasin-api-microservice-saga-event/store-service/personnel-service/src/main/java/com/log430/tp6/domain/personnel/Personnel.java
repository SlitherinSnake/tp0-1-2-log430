package com.log430.tp6.domain.personnel;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Unified Personnel domain combining Employee and User functionality.
 * Represents a person in the system who can both work as an employee
 * and have login credentials for system access.
 */
@Entity
@Table(name = "personnel", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = "username"),
           @UniqueConstraint(columnNames = "identifiant")
       })
public class Personnel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Employee information
    @Column(nullable = false, length = 100)
    private String nom;
    
    @Column(nullable = false, length = 50)
    private String identifiant;

    // Authentication information (optional - not all personnel need login)
    @Column(length = 50)
    private String username;
    
    private String password; // Encrypted password
    
    @Column(nullable = false)
    private boolean isActive = true;

    /**
     * Roles associated with this personnel for system access.
     * Only applicable if username/password are set.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "personnel_roles", 
              joinColumns = @JoinColumn(name = "personnel_id"),
              inverseJoinColumns = @JoinColumn(name = "role_id")) 
    private Set<Role> roles = new HashSet<>();

    // Constructors
    public Personnel() {}

    // Constructor for employee-only (no login)
    public Personnel(String nom, String identifiant) {
        this.nom = nom;
        this.identifiant = identifiant;
    }

    // Constructor for personnel with login
    public Personnel(String nom, String identifiant, String username, String password) {
        this.nom = nom;
        this.identifiant = identifiant;
        this.username = username;
        this.password = password;
    }

    // Business methods
    public boolean hasLoginAccess() {
        return username != null && password != null;
    }

    public boolean hasRole(String roleName) {
        return roles.stream().anyMatch(role -> role.getName().equals(roleName));
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getIdentifiant() { return identifiant; }
    public void setIdentifiant(String identifiant) { this.identifiant = identifiant; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }

    @Override
    public String toString() {
        return "Personnel{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", identifiant='" + identifiant + '\'' +
                ", username='" + username + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
