package com.log430.tp3.repository;

import com.log430.tp3.model.StockMagasin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Interface de persistance pour le stock local des magasins.
 * Gère l'accès aux données de la table `stockMagasin`.
 */
public interface StockMagasinRepository extends JpaRepository<StockMagasin, Integer> {

    /**
     * Récupère tous les produits en stock pour un magasin donné.
     * Utile pour afficher l’état du stock local d’un magasin spécifique.
     *
     * @param magasinId l’identifiant du magasin
     * @return liste des stocks associés à ce magasin
     */
    List<StockMagasin> findByMagasinId(int magasinId);

    /**
     * Récupère une entrée unique de stock pour un produit dans un magasin
     * spécifique.
     * Pratique pour modifier la quantité locale après une vente ou une réception.
     *
     * @param produitId identifiant du produit
     * @param magasinId identifiant du magasin
     * @return stock du produit dans ce magasin, s’il existe
     */
    @Query("SELECT s FROM StockMagasin s WHERE s.produit.id = :produitId AND s.magasin.id = :magasinId")
    Optional<StockMagasin> findByProduitAndMagasin(@Param("produitId") int produitId,
            @Param("magasinId") int magasinId);

    /**
     * Récupère tous les stocks ayant exactement une certaine quantité.
     * Utile par exemple pour détecter les ruptures (quantité = 0).
     * 
     * @param quantite valeur exacte de stock recherchée
     * @return liste des stocks correspondants
     */
    List<StockMagasin> findByQuantite(int quantite);

    /**
     * Récupère tous les stocks dont la quantité est supérieure à un certain seuil.
     * Utile pour détecter les surstocks ou excès d'inventaire.
     * @param seuil valeur minimale de stock
     * @return liste des stocks en excès
     */
    List<StockMagasin> findByQuantiteGreaterThan(int seuil);

}
