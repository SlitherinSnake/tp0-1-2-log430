package com.log430.tp3.api.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.log430.tp3.api.dto.JwtResponse;
import com.log430.tp3.api.dto.LoginRequest;
import com.log430.tp3.api.dto.MessageResponse;
import com.log430.tp3.api.dto.SignupRequest;
import com.log430.tp3.model.Role;
import com.log430.tp3.model.Role.ERole;
import com.log430.tp3.model.User;
import com.log430.tp3.repository.RoleRepository;
import com.log430.tp3.repository.UserRepository;
import com.log430.tp3.security.jwt.JwtUtils;
import com.log430.tp3.security.services.UserDetailsImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Point d’entrée REST pour la gestion de l’authentification :
 * - /login  → le client s’authentifie et reçoit un jeton JWT
 * - /signup → le client crée un nouveau compte
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentification", description = "Authentification API")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager; // Exécute la vérification login/mot de passe

    @Autowired
    UserRepository userRepository; // Accès aux comptes en base

    @Autowired
    RoleRepository roleRepository; // Accès aux rôles en base

    @Autowired
    PasswordEncoder encoder; // Chiffre les mots de passe

    @Autowired
    JwtUtils jwtUtils; // Génère et valide les JWT

    // Login
    @PostMapping("/login")
    @Operation(summary = "Authentifier l'utilisateur", description = "Authentifier un utilisateur et renvoyer un jeton JWT")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        // 1. Vérifie les identifiants
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        // 2. Enregistre l’utilisateur courant dans le contexte (thread local)
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Génère un jeton signé (valable X minutes/heures)
        String jwt = jwtUtils.generateJwtToken(authentication);
        
        // 4. Récupère les rôles pour les renvoyer dans la réponse
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        // 5. Renvoie le jeton + quelques infos utiles (id, login, rôles)
        return ResponseEntity.ok(new JwtResponse(jwt,
                                                 userDetails.getId(), 
                                                 userDetails.getUsername(), 
                                                 roles));
    }

    // 2) INSCRIPTION  
    @PostMapping("/signup")
    @Operation(summary = "Enregistrer un utilisateur", description = "Enregistrez un nouvel utilisateur")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {

        // Vérifie l’unicité du login
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Erreur : le nom d'utilisateur est déjà pris !"));
        }

        // Crée l’objet User et chiffre le mot de passe
        User user = new User(signUpRequest.getUsername(),
                             encoder.encode(signUpRequest.getPassword()));

        
        /* Attribution des rôles :
           - Si aucun rôle fourni → ROLE_VIEWER par défaut
           - Sinon on parcourt la liste demandée (admin / employee / viewer) */
        Set<String> strRoles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) { // Aucun rôle fourni
            Role viewerRole = roleRepository.findByName(ERole.ROLE_VIEWER)
                    .orElseThrow(() -> new RuntimeException("Erreur : le rôle n’est pas trouvé."));
            roles.add(viewerRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                case "admin":
                    Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                            .orElseThrow(() -> new RuntimeException("Erreur : le rôle n’est pas trouvé."));
                    roles.add(adminRole);
                    break;
                case "employee":
                    Role empRole = roleRepository.findByName(ERole.ROLE_EMPLOYEE)
                            .orElseThrow(() -> new RuntimeException("Erreur : le rôle n’est pas trouvé."));
                    roles.add(empRole);
                    break;
                default:
                    Role viewerRole = roleRepository.findByName(ERole.ROLE_VIEWER)
                            .orElseThrow(() -> new RuntimeException("Erreur : le rôle n’est pas trouvé."));
                    roles.add(viewerRole);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);  // Persistance en base

        return ResponseEntity.ok(new MessageResponse("Utilisateur enregistré avec succès !"));
    }
} 