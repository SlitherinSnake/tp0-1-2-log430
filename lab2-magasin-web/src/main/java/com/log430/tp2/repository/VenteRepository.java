package com.log430.tp2.repository;

import java.time.LocalDate;
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

    /**
     * Calcule le chiffre d'affaires (montant total des ventes) par magasin.
     * Redondant avec totalVentesParMagasin mais peut être utilisé pour des filtres.
     * 
     * @return liste d’objets [nom du magasin, total du chiffre d'affaires]
     */
    @Query("SELECT v.magasin.nom, SUM(v.montantTotal) FROM Vente v GROUP BY v.magasin.nom")
    List<Object[]> chiffreAffaireParMagasin();

     /**
     * Calcule le chiffre d'affaires pour tous les magasins, même ceux
     * qui n'ont réalisé aucune vente.
     *
     * Utilise une jointure externe (LEFT JOIN) entre Magasin et Vente
     * afin d'inclure les magasins sans ventes (valeur par défaut : 0).
     *
     * @return Liste d'objets [nom du magasin, chiffre d'affaires total]
     *         Ex : [["Magasin A", 1200.0], ["Magasin B", 0.0], ["Magasin C", 850.0]]
     */
    @Query("""
            SELECT m.nom, COALESCE(SUM(v.montantTotal), 0)
            FROM Magasin m
            LEFT JOIN Vente v ON v.magasin.id = m.id
            GROUP BY m.nom
            """)
    List<Object[]> chiffreAffaireTousMagasins();

    /**
     * Récupère toutes les ventes après une certaine date,
     * triées par date croissante.
     * 
     * Utile pour le tableau de bord hebdomadaire (UC3).
     *
     * @param date date minimale (inclusivement)
     * @return liste triée de ventes récentes
     */
    List<Vente> findByDateVenteAfterOrderByDateVenteAsc(LocalDate date);
}
