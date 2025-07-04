package com.log430.tp4.security.jwt;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.log430.tp4.security.services.UserDetailsServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


/**
 * Filtre exécuté UNE SEULE FOIS par requête HTTP.
 * Responsabilités :
 * 1. Récupérer le JWT présent dans l’en-tête "Authorization"
 * 2. Vérifier la validité du token (date d’expiration, signature…)
 * 3. Charger les informations de l’utilisateur et
 *    les placer dans le contexte de sécurité Spring
 */
public class AuthTokenFilter extends OncePerRequestFilter {

    /*  Dépendances injectées automatiquement par Spring  */
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // Extrait le JWT de l’en-tête HTTP
            String jwt = parseJwt(request);

            // S’il existe et qu’il est valide, on authentifie l’utilisateur
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {

                // Récupère le login contenu dans le token
                String username = jwtUtils.getUserNameFromJwtToken(jwt);

                // Charge les détails (rôles, droits…) depuis la base
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Crée l’objet d’authentification
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,                          // pas de mot de passe ici
                                userDetails.getAuthorities()); // rôles / permissions
                // Ajoute des infos sur la requête (IP, session…)
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Place l’utilisateur authentifié dans le contexte global
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Impossible de définir l'authentification de l'utilisateur: {}", e);
        }
        // Poursuit le traitement de la requête
        filterChain.doFilter(request, response);
    }

    /**
     * Extrait le token JWT de l’en-tête "Authorization: Bearer <token>"
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7); // Supprime le préfixe "Bearer "
        }

        return null;
    }
} 