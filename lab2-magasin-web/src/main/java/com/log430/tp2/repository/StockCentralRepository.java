package com.log430.tp2.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.log430.tp2.model.StockCentral;

public interface StockCentralRepository extends JpaRepository<StockCentral, Integer> {

    /**
     * Récupère toutes les demandes de réapprovisionnement d’un magasin spécifique.
     * Utile pour afficher l’historique des demandes par magasin.
     */
    @Query("SELECT s FROM StockCentral s WHERE s.magasin.id = :magasinId ORDER BY s.dateDemande DESC")
    List<StockCentral> findByMagasin(@Param("magasinId") int magasinId);

    /**
     * Récupère toutes les demandes de réapprovisionnement pour un produit donné.
     * Permet de tracer la demande globale pour un produit.
     */
    @Query("SELECT s FROM StockCentral s WHERE s.produit.id = :produitId ORDER BY s.dateDemande DESC")
    List<StockCentral> findByProduit(@Param("produitId") int produitId);

    /**
     * Vérifie s’il existe déjà une demande récente pour un produit dans un magasin.
     * Optionnel : permet d’éviter les doublons ou abus dans les demandes.
     */
    @Query("SELECT s FROM StockCentral s WHERE s.produit.id = :produitId AND s.magasin.id = :magasinId AND s.dateDemande = CURRENT_DATE")
    List<StockCentral> findDemandesDuJour(@Param("produitId") int produitId, @Param("magasinId") int magasinId);

}
