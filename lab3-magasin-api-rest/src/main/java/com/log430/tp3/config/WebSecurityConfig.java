package com.log430.tp3.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfigurationSource;

import com.log430.tp3.security.jwt.AuthEntryPointJwt;
import com.log430.tp3.security.jwt.AuthTokenFilter;
import com.log430.tp3.security.services.UserDetailsServiceImpl;

/**
 * Configuration centrale de la sécurité.
 * - Décrit comment les utilisateurs se connectent (formulaire, JWT…)
 * - Spécifie quelles pages sont libres d’accès et lesquelles sont protégées
 * - Ajoute les filtres (ex. AuthTokenFilter) qui lisent et valident les JWT
 */
@Configuration
@EnableWebSecurity // Active la sécurité Web de Spring
@EnableMethodSecurity // Permet d’utiliser @PreAuthorize sur les méthodes
public class WebSecurityConfig {

    /* Dépendances injectées automatiquement par Spring */
    @Autowired
    UserDetailsServiceImpl userDetailsService; // Va chercher les utilisateurs en base

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler; // Répond 401 quand l'accès est interdit

    @Autowired
    private CorsConfigurationSource corsConfigurationSource; // Configuration CORS centralisée

    // Beans (= objets créés et gérés par Spring)
    /**
     * Filtre personnalisé qui analyse chaque requête HTTP :
     * s’il y a un JWT, il le valide et authentifie l’utilisateur.
     */
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    /**
     * Fournit à Spring :
     * - la source pour récupérer l’utilisateur (userDetailsService)
     * - l’algorithme de hachage pour vérifier le mot de passe
     */
    @SuppressWarnings("deprecation")
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService); // Où trouver l’utilisateur
        authProvider.setPasswordEncoder(passwordEncoder()); // Comment comparer les mdp

        return authProvider;
    }

    /** Expose l’AuthenticationManager que Spring utilise pour la connexion. */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /** Algorithme BCrypt pour chiffrer/valider les mots de passe. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //1) Chaîne de filtres pour l'api (JSON + JWT)  
    @Bean
    @Order(1) // Traité avant le filtre « formulaire » car ordre = 1
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
                // Cette chaîne ne s'applique qu'aux URLs commençant par /api/**
                .securityMatcher("/api/**")
                // Configuration CORS sécurisée (remplace les @CrossOrigin permissifs)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // Pas de protection CSRF pour les appels API (gérés par token)
                .csrf(csrf -> csrf.disable())
                // Si l’utilisateur n’est pas connecté → renvoie 401 en JSON
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                // Pas de session serveur : on est « stateless » grâce aux JWT
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Règles d’accès
                .authorizeHttpRequests(auth -> auth.requestMatchers("/api/v1/auth/**").permitAll() // login / register
                        .requestMatchers("/api/auth/token").permitAll() // obtention token
                        .requestMatchers("/api/v1/produits/**").permitAll() // exemples publics
                        .anyRequest().authenticated()); // le reste = protégé

        // Ajoute notre provider et notre filtre JWT
        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 2) Chaîne de filtres pour le site web (formulaire + session)  
    @SuppressWarnings("removal")
    @Bean
    @Order(2) // Traité après apiFilterChain
    public SecurityFilterChain formLoginFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(
                        auth -> auth.requestMatchers("/login", "/api-docs/**", "/swagger-ui/**").permitAll() // pages publiques
                                .requestMatchers("/api-docs/swagger-ui/**").permitAll()
                                .requestMatchers("/", "/home", "/css/**", "/js/**", "/images/**").permitAll()
                                .anyRequest().authenticated()) // tout le reste est protégé
                // Active la page /login avec redirection vers la racine après succès
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll())
                // Configuration de la déconnexion
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login?logout")
                        .permitAll());

        return http.build();
    }
}