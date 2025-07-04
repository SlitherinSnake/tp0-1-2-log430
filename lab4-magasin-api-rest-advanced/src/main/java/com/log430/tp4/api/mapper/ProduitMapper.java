package com.log430.tp4.api.mapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.log430.tp4.api.dto.ProduitDto;
import com.log430.tp4.model.Produit;

/**
 * ProduitMapper fait le “pont” entre
 * - l’objet Produit stocké dans la base de données (ENTITÉ)
 * - l’objet ProduitDto envoyé/reçu par notre API (DTO)
 *
 * Pourquoi séparer les deux ?
 *   • Sécurité : on expose uniquement les champs nécessaires au client.  
 *   • Souplesse : on peut faire évoluer la base sans casser l’API.  
 *   • Validation : les règles (prix positif, nom non vide…) sont dans le DTO.
 */
@Component  // Permet à Spring d’injecter automatiquement ce mapper où on en a besoin
public class ProduitMapper {

    // 1) ENTITÉ → DTO 

    /**
     * Transforme un objet Produit (côté base de données)
     * en ProduitDto (côté API).
     */
    public ProduitDto toDto(Produit produit) {
        if (produit == null) { // Sécurité : si l’entrée est nulle, on renvoie null
            return null;
        }
        
        // On va chercher chaque champ de l’entité et on le place dans le DTO
        return new ProduitDto(
            produit.getId(),
            produit.getNom(),
            produit.getCategorie(),
            produit.getPrix(),
            produit.getQuantite()
        );
    }

    /**
     * Transforme une liste d’entités Produit en liste de DTO.
     * Utilisé, par exemple, pour renvoyer tout le catalogue en JSON.
     */
    public List<ProduitDto> toDtoList(List<Produit> produits) {
        if (produits == null) { // Liste vide → on renvoie une liste vide
            return new ArrayList<>();
        }
        
        List<ProduitDto> dtoList = new ArrayList<>();
        for (Produit produit : produits) {  // Conversion élément par élément
            dtoList.add(toDto(produit));
        }
        return dtoList;
    }

    // 2) DTO → ENTITÉ  
    /**
     * Transforme un ProduitDto (reçu depuis le client)
     * en entité Produit (prête à être sauvegardée en base).
     */
    public Produit toEntity(ProduitDto dto) {
        if (dto == null) {  // DTO absent → on renvoie null
            return null;
        }
        
        Produit produit = new Produit();
        
        // On copie le champ id seulement s’il est présent (cas “mise à jour”)
        if (dto.getId() != null) {
            produit.setId(dto.getId());
        }
        
        // On copie les autres champs sans conditions
        produit.setNom(dto.getNom());
        produit.setCategorie(dto.getCategorie());
        produit.setPrix(dto.getPrix());
        produit.setQuantite(dto.getQuantite());
        
        return produit;
    }

    /**
     * Met à jour une entité Produit existante à partir d’un DTO partiel.
     * Seuls les champs non nuls du DTO écrasent ceux de l’entité.
     * Pratique pour les requêtes PATCH ou PUT.
     */
    public void updateEntityFromDto(ProduitDto dto, Produit produit) {
        if (dto == null || produit == null) { // Rien à faire si l’un des deux est null
            return;
        }
        
        // Chaque bloc vérifie d’abord que le champ a bien été fourni dans le DTO
        if (dto.getNom() != null) {
            produit.setNom(dto.getNom());
        }
        
        if (dto.getCategorie() != null) {
            produit.setCategorie(dto.getCategorie());
        }
        
        if (dto.getPrix() != null) {
            produit.setPrix(dto.getPrix());
        }
        
        if (dto.getQuantite() != null) {
            produit.setQuantite(dto.getQuantite());
        }
    }
} 