package com.log430.tp3.security.jwt;

import java.security.Key;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.log430.tp3.security.services.UserDetailsImpl;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Outil central pour la gestion des JSON Web Tokens (JWT) :
 * - Génération d’un token signé
 * - Extraction d’informations du token
 * - Validation (signature, expiration, etc...)
 */
@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    // Clé secrète et durée de vie lues depuis application.properties
    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationMs}")
    private int jwtExpirationMs;

    /**
     * Génère un JWT signé pour l’utilisateur authentifié.
     * @param authentication l’objet Authentication actuel
     * @return chaîne JWT prête à être renvoyée au client
     */
    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject((userPrincipal.getUsername())) // login
                .setIssuedAt(new Date()) // date d’émission
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs)) // date d’expiration
                .signWith(key(), SignatureAlgorithm.HS256) // signature HMAC-SHA256
                .compact(); // sérialise le token
    }
    
    /**
     * Transforme la clé secrète (Base64) en objet Key utilisable par JJWT.
     */
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    /**
     * Extrait le nom d’utilisateur (login) contenu dans le JWT.
     */
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    /**
     * Vérifie la validité d’un JWT :
     * - signature correcte
     * - format valide
     * - non expiré
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken);
            return true; // Token OK
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false; // Token KO
    }
} 