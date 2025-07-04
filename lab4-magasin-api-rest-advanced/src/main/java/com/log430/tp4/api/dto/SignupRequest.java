package com.log430.tp4.api.dto;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * SignupRequest = “formulaire” JSON que le client envoie
 * lorsqu’il veut créer un nouveau compte.
 * Champs attendus :
 *   • username  : login souhaité (3 à 20 caractères, obligatoire)  
 *   • password  : mot de passe (6 à 40 caractères, obligatoire)  
 *   • roles     : liste optionnelle de rôles (admin, employee, viewer…).  
 *
 * Les annotations de validation (@NotBlank, @Size) protègent
 * l’API : si le client ne respecte pas les règles, la requête est rejetée
 * avec une erreur 400 avant même d’atteindre la base de données.
 */
public class SignupRequest {
    // Nom d’utilisateur obligatoire, 3 à 20 caractères
    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Size(min = 3, max = 20, message = "Le nom d'utilisateur doit contenir entre 3 et 20 caractères")
    private String username;

    // Rôles demandés (peut être null → rôle par défaut côté serveur)
    private Set<String> roles;

    // Mot de passe obligatoire, 6 à 40 caractères
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, max = 40, message = "Le mot de passe doit contenir entre 6 et 40 caractères")
    private String password;

    // Getter/Setter
    public String getUsername() {    return username;}
    public void setUsername(String username) {    this.username = username;}

    public String getPassword() {    return password;}
    public void setPassword(String password) {    this.password = password;}

    public Set<String> getRoles() {    return this.roles;}
    public void setRoles(Set<String> roles) {    this.roles = roles;}
} 