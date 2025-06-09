package com.log430.tp2.repository;

import java.util.List;

// RetourRepository.java
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.log430.tp2.model.Retour;

public interface RetourRepository extends JpaRepository<Retour, Integer> {

    // Pour récupérer les IDs des ventes déjà retournées au complet
    @Query("SELECT r.vente.id FROM Retour r")
    List<Integer> findVenteIdsDejaRetournees();

    // Pour récupérer les IDs des produits déjà retournées au complet
    @Query("SELECT rp.produit.id FROM RetourProduit rp WHERE rp.retour.vente.id = :venteId")
    List<Integer> findProduitIdsDejaRetournesPourVente(@Param("venteId") int venteId);

    // Pour connaître les quantités déjà retournées par produit dans une vente 
    @Query("SELECT rp.produit.id, SUM(rp.quantite) FROM RetourProduit rp WHERE rp.retour.vente.id = :venteId GROUP BY rp.produit.id")
    List<Object[]> findQuantitesRetourneesPourVente(@Param("venteId") int venteId);

}
