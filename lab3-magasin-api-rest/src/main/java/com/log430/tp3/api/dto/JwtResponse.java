package com.log430.tp3.api.dto;

import java.util.List;

/**
 * JwtResponse = réponse renvoyée par l’API après un login réussi.
 * Contenu :
 *   • token  : le fameux JSON Web Token à placer dans l’en-tête des requêtes suivantes.  
 *   • type   : « Bearer » (nom du schéma utilisé dans l’en-tête).  
 *   • id     : identifiant interne de l’utilisateur.  
 *   • username : login de l’utilisateur.  
 *   • roles  : liste de ses rôles (ROLE_ADMIN, etc.), utile côté front-end pour gérer l’UI.
 */
public class JwtResponse {
    private String token; // Le jeton JWT lui-même
    private String type = "Bearer";
    private Long id;
    private String username;
    private List<String> roles;

    /** Constructeur : on stocke le token plus quelques infos complémentaires. */
    public JwtResponse(String accessToken, Long id, String username, List<String> roles) {
        this.token = accessToken;
        this.id = id;
        this.username = username;
        this.roles = roles;
    }

    // Getter/Setter
    public String getToken() {    return token;}
    public void setToken(String token) {    this.token = token;}
    
    // Alias for accessToken - for compatibility with tests
    public String getAccessToken() {    return token;}

    public String getType() {    return type;}
    public void setType(String type) {    this.type = type;}

    public Long getId() {    return id;}
    public void setId(Long id) {    this.id = id;}

    public String getUsername() {    return username;}
    public void setUsername(String username) {    this.username = username;}

    public List<String> getRoles() {    return roles;}
} 