package com.log430.tp3.api.dto;

import java.time.LocalDateTime;

/**
 * ErrorResponse = “enveloppe standard” utilisée par l'API
 * lorsqu’une erreur se produit.
 * Avantages :
 *   • Le front-end reçoit toujours la même structure, quel que soit le bug.  
 *   • On garde des infos utiles pour le débogage (heure, chemin, message).  
 *   • On évite d’exposer la trace technique complète au client final.
 */
public class ErrorResponse {

    // Horraire automatique créé au moment de l’erreur
    private LocalDateTime timestamp;
    // Code HTTP (ex. 404, 500, 403…)
    private int status;
    // Libellé (NOT_FOUND, INTERNAL_SERVER_ERROR…)
    private String error;
    // Message explicite pour l’utilisateur ou le développeur
    private String message;
    // URI qui a provoqué l’erreur (ex. /api/v1/produits/99)
    private String path;

    /** Constructeur vide : positionne automatiquement l’horodatage à « maintenant ». */
    public ErrorResponse() {    this.timestamp = LocalDateTime.now();}

    /**
     * Constructeur pratique pour remplir tous les champs d’un coup.
     */
    public ErrorResponse(int status, String error, String message, String path) {
        this(); // Appelle le constructeur vide (timestamp = now)
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    // Getter/Setter
    public LocalDateTime getTimestamp() {    return timestamp;}
    public void setTimestamp(LocalDateTime timestamp) {   this.timestamp = timestamp;}

    public int getStatus() {    return status;}
    public void setStatus(int status) {    this.status = status;}

    public String getError() {    return error;}
    public void setError(String error) {    this.error = error;}

    public String getMessage() {   return message;}
    public void setMessage(String message) {    this.message = message;}

    public String getPath() {    return path;}
    public void setPath(String path) {    this.path = path;}
} 