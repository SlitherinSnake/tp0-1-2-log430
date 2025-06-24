package com.log430.tp3.api.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.log430.tp3.api.dto.ErrorResponse;
import com.log430.tp3.api.dto.MessageResponse;
import com.log430.tp3.api.dto.ProduitDto;
//import com.log430.tp3.api.mapper.ProduitMapper;
import com.log430.tp3.model.Produit;
import com.log430.tp3.repository.ProduitRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Contrôleur API REST dédié aux produits :
 * - GET    /produits          → liste
 * - GET    /produits/{id}     → détail
 * - POST   /produits          → création
 * - PUT    /produits/{id}     → mise à jour
 * - DELETE /produits/{id}     → suppression
 *
 * Règles d’accès :
 * - Lecture ouverte
 * - Création / édition : ADMIN ou EMPLOYEE
 * - Suppression : ADMIN seul
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/produits")
@Tag(name = "Produits", description = "Produits gestion API")
public class ProduitApiController {

    @Autowired
    private ProduitRepository produitRepository;

    //@Autowired
    //private ProduitMapper produitMapper;

    //1) LISTE COMPLÈTE
    @GetMapping
    @Operation(summary = "Obtenez tous les produits", description = "Renvoie tous les produits")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès"),
        @ApiResponse(responseCode = "401", description = "Vous n'êtes pas autorisé à consulter la ressource",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "L'accès à la ressource que vous tentiez d'atteindre est interdit",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<ProduitDto>> getAllProduits() {
        List<Produit> produits = produitRepository.findAll();
        // Conversion manuelle en DTO (on pourrait appeler produitMapper.toDtoList) < À revoir
        List<ProduitDto> produitDtos = new ArrayList<>();
        
        for (Produit produit : produits) {
            ProduitDto dto = new ProduitDto(
                produit.getId(),
                produit.getNom(),
                produit.getCategorie(),
                produit.getPrix(),
                produit.getQuantite()
            );
            produitDtos.add(dto);
        }
        
        return ResponseEntity.ok(produitDtos);
    }

    //2) RÉCUPÉRATION PAR ID    
    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un produit par ID", description = "Renvoie un produit par ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Produit récupéré avec succès"),
        @ApiResponse(responseCode = "404", description = "Produit introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Vous n'êtes pas autorisé à consulter la ressource",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "L'accès à la ressource que vous tentiez d'atteindre est interdit",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProduitDto> getProduitById(@PathVariable int id) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produit non trouvé avec l'ID: " + id));
        
        ProduitDto dto = new ProduitDto(
            produit.getId(),
            produit.getNom(),
            produit.getCategorie(),
            produit.getPrix(),
            produit.getQuantite()
        );
        
        return ResponseEntity.ok(dto);
    }

    // 3) CRÉATION (ADMIN | EMPLOYEE)  
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @Operation(
        summary = "Créer un nouveau produit", 
        description = "Crée un nouveau produit",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Produit créé avec succès"),
        @ApiResponse(responseCode = "400", description = "Entrée invalide",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Vous n'êtes pas autorisé à créer la ressource",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "L'accès à la ressource que vous tentiez d'atteindre est interdit",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProduitDto> createProduit(@Valid @RequestBody ProduitDto produitDto) {

        Produit produit = new Produit();

        // Si l’ID est fourni, on le recopie (sinon la base le générera)
        if (produitDto.getId() != null) {
            produit.setId(produitDto.getId());
        }
        
        produit.setNom(produitDto.getNom());
        produit.setCategorie(produitDto.getCategorie());
        produit.setPrix(produitDto.getPrix());
        produit.setQuantite(produitDto.getQuantite());
        
        produit = produitRepository.save(produit); // Sauvegarde
        
        ProduitDto savedDto = new ProduitDto(
            produit.getId(),
            produit.getNom(),
            produit.getCategorie(),
            produit.getPrix(),
            produit.getQuantite()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }

    // 4) MISE À JOUR (ADMIN | EMPLOYEE)       
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @Operation(
        summary = "Mettre à jour un produit", 
        description = "Met à jour un produit existant",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Produit mis à jour avec succès"),
        @ApiResponse(responseCode = "400", description = "Entrée invalide",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Produit introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Vous n'êtes pas autorisé à mettre à jour la ressource",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "L'accès à la ressource que vous tentiez d'atteindre est interdit",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProduitDto> updateProduit(@PathVariable int id, @Valid @RequestBody ProduitDto produitDto) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produit non trouvé avec l'ID: " + id));
        
        // Mise à jour partielle (on ne touche qu’aux champs fournis)
        if (produitDto.getNom() != null) {    produit.setNom(produitDto.getNom());}
        if (produitDto.getCategorie() != null) {    produit.setCategorie(produitDto.getCategorie());}
        if (produitDto.getPrix() != null) {    produit.setPrix(produitDto.getPrix());}
        if (produitDto.getQuantite() != null) {    produit.setQuantite(produitDto.getQuantite());}
        
        produit = produitRepository.save(produit);
        
        ProduitDto updatedDto = new ProduitDto(
            produit.getId(),
            produit.getNom(),
            produit.getCategorie(),
            produit.getPrix(),
            produit.getQuantite()
        );
        
        return ResponseEntity.ok(updatedDto);
    }

    // 5) SUPPRESSION (ADMIN) 
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Supprimer un produit", 
        description = "Supprime un produit par ID",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Produit supprimé avec succès"),
        @ApiResponse(responseCode = "404", description = "Produit introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Vous n'êtes pas autorisé à supprimer la ressource",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "L'accès à la ressource que vous tentiez d'atteindre est interdit",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MessageResponse> deleteProduit(@PathVariable int id) {
        if (!produitRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Produit non trouvé avec l'ID: " + id);
        }
        
        produitRepository.deleteById(id);
        return ResponseEntity.ok(new MessageResponse("Produit supprimé avec succès"));
    }
} 