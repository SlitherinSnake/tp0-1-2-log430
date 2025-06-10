package com.log430.tp2.repository;

import java.util.List;

// VenteRepository.java
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.log430.tp2.model.Vente;

public interface VenteRepository extends JpaRepository<Vente, Integer> {

    /**
     * Calcule le total des ventes par magasin (nom du magasin).
     * Retourne une liste de tableaux : [nom_magasin, total_ventes]
     * Exemple : [["Magasin A", 1200.0], ["Magasin B", 850.0]]
     */
    @Query("SELECT v.magasin.nom, SUM(v.montantTotal) FROM Vente v GROUP BY v.magasin.nom")
    List<Object[]> totalVentesParMagasin();

    /**
     * Retourne les produits les plus vendus toutes ventes confondues,
     * triés par quantité décroissante.
     * Format : [produit, somme_quantité]
     */
    @Query("SELECT vp.produit, SUM(vp.quantite) as total FROM VenteProduit vp GROUP BY vp.produit ORDER BY total DESC")
    List<Object[]> produitsLesPlusVendus();

    /**
     * Même logique que produitsLesPlusVendus(), mais limitée à un seul magasin.
     * Filtrage via l'ID du magasin passé en paramètre.
     */
    @Query("SELECT vp.produit, SUM(vp.quantite) FROM VenteProduit vp WHERE vp.vente.magasin.id = :magasinId GROUP BY vp.produit ORDER BY SUM(vp.quantite) DESC")
    List<Object[]> produitsLesPlusVendusParMagasin(@Param("magasinId") Integer magasinId);

}
