package com.log430.tp4.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * LoginRequest = contenu attendu dans le corps JSON
 * de la requête POST /login.
 * Deux champs : username et password, tous deux obligatoires.
 * Les annotations @NotBlank déclenchent une erreur 400
 * si l’un des champs est vide ou manquant.
 */
public class LoginRequest {
    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    private String username;

    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;

    // Getter/Setter
    public String getUsername() {    return username;}
    public void setUsername(String username) {    this.username = username;}

    public String getPassword() {    return password;}
    public void setPassword(String password) {    this.password = password;}
} 