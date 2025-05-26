package com.log430.tp1.model.dao;

import java.util.List;
import org.hibernate.Session;
import com.log430.tp1.config.HibernateUtil;
import com.log430.tp1.model.Produit;

public class ProduitDAO {

    // Recherche produit par son ID qui est la clé primaire dans la bd de Produit
    public Produit rechercherParId(int id) {
        // Ouvre session Hibernate pour accès à BD
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            /*
             * Utilise session.get() pour trouver
             * l'entité Produit correspondant à l'ID fourni
             */
            return session.get(Produit.class, id);
        }
    }

    // Recherche tous les produits dont le nom contient une sous-chaîne spécifique
    public List<Produit> rechercherParNom(String nom) {
        // Ouvre session Hibernate pour accès à BD
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            /*
             * Effectue une requête HQL (Hibernate Query Language) pour récupérer
             * tous les produits dont le nom contient le mot-clé fourni
             * Exemple : "pom" retournera "pomme", "compote de pomme", etc.
             */
            return session.createQuery("FROM Produit WHERE LOWER(nom) LIKE :nom", Produit.class)
                    .setParameter("nom", "%" + nom.toLowerCase() + "%")
                    .list();
        }
    }

    // Recherche tous les produits associés à une catégorie spécifique
    public List<Produit> rechercherParCategorie(String categorie) {
        // Ouvre session Hibernate pour accès à BD
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            /*
             * Effectue une requête HQL qui retourne
             * tous les produits dont la catégorie contient le mot-clé fourni.
             * Exemple : "fruit" retourne "banane", "pomme", etc.
             */
            return session.createQuery("FROM Produit WHERE LOWER(categorie) LIKE :cat", Produit.class)
                    .setParameter("cat", "%" + categorie.toLowerCase() + "%")
                    .list();
        }
    }
}
