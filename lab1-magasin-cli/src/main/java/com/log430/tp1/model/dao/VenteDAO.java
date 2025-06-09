package com.log430.tp1.model.dao;

import java.util.Map;

import org.hibernate.Session;

import com.log430.tp1.config.HibernateUtil;
import com.log430.tp1.model.Produit;
import com.log430.tp1.model.Vente;

public class VenteDAO {

    // Recherche une vente par son ID, incluant ses produits vendus (venteProduits)
    public Vente rechercherParId(int id) {
        // Ouvre une session Hibernate pour interagir avec la BD
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            /*
             * Utilise une requête HQL pour charger :
             * - la vente correspondant à l'ID fourni
             * - ainsi que la collection associée de venteProduits grâce au JOIN FETCH
             * 
             * Le LEFT JOIN FETCH permet de récupérer la vente 
             * et ses produits en une seule requête, 
             */
            return session.createQuery(
                    "SELECT v FROM Vente v LEFT JOIN FETCH v.venteProduits WHERE v.id = :id", Vente.class)
                    .setParameter("id", id) // Lie valeur du param :id à l'id passé
                    .uniqueResult(); // Retourne la seule vente correspondante, ou null si aucune
        }
    }

    // Enregistre la vente dans la BD,
    // en incluant les produits et leurs quantités et total de prix
    public void enregistrerVente(Vente vente) {
        // Ouvre session Hibernate pour accès à BD
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            /*
             * Démarre une transaction, enregistre l'objet Vente et ses relations,
             * puis l'envoie pour persister la vente complète dans la BD
             */
            session.beginTransaction();
            session.persist(vente);
            session.getTransaction().commit();
        }
    }

    // Calcule le total d'une vente à partir des produits et des quantités associées
    public double calculerTotal(Map<Produit, Integer> produitsEtQuantites) {
        double total = 0.0;

        /*
         * Parcourt chaque paire (produit, quantité) pour additionner
         * le prix total de chaque produit à la vente
         */
        for (Map.Entry<Produit, Integer> entry : produitsEtQuantites.entrySet()) {
            Produit produit = entry.getKey();
            int quantite = entry.getValue();
            total += produit.getPrix() * quantite;
        }

        return total;
    }
}
