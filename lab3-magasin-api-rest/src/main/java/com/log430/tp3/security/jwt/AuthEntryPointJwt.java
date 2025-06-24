package com.log430.tp3.security.jwt;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Point d’entrée déclenché lorsqu’un utilisateur non authentifié
 * tente d’accéder à une ressource protégée.
 * - Renvoie automatiquement un code 401 (Unauthorized)
 * - Retourne une réponse JSON lisible par le client
 */
@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    // Journalisation (utile pour le débogage et l’audit)
    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);

    /**
     * Méthode appelée à chaque tentative d’accès non autorisée
     * @param request  requête HTTP reçue
     * @param response réponse HTTP à envoyer
     * @param authException exception contenant la cause de l’échec
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        // Trace l’erreur côté serveur
        logger.error("Unauthorized error: {}", authException.getMessage());

        // Prépare la réponse 401 au format JSON
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Corps JSON renvoyé au client (message, chemin, etc...)
        final Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "Unauthorized");
        body.put("message", authException.getMessage());
        body.put("path", request.getServletPath());
        body.put("timestamp", System.currentTimeMillis());

        // Sérialise le corps en JSON et l’écrit dans la réponse
        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }
} 