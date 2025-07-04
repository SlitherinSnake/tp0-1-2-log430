package com.log430.tp4.api.dto;

/**
 * MessageResponse = petite enveloppe JSON
 * servant à renvoyer un simple message texte (succès ou info).
 * Exemple : “User registered successfully!”, “Produit supprimé avec succès”, etc.
 */
public class MessageResponse {
    private String message;

    /** Constructeur direct : on fournit le texte au moment de la création. */
    public MessageResponse(String message) {    this.message = message;}

    // Getter/Setter
    public String getMessage() {    return message;}
    public void setMessage(String message) {    this.message = message;}
} 