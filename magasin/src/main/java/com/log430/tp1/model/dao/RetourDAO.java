package com.log430.tp1.model.dao;

import java.util.List;

import org.hibernate.Session;

import com.log430.tp1.config.HibernateUtil;
import com.log430.tp1.model.Retour;

public class RetourDAO {

    // Recherche un retour par son identifiant unique (ID auto-incrémenté)
    public Retour rechercherParId(int id) {
        // Ouvre session Hibernate pour accès à BD
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            /*
             * Utilise session.get() pour récupérer l'objet Retour
             * correspondant à l'ID fourni
             */
            return session.get(Retour.class, id);
        }
    }

    // Enregistre un objet Retour dans la base de données
    public void enregistrerRetour(Retour retour) {
        // Ouvre session Hibernate pour accès à BD
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            /*
             * Démarre une transaction, persiste l'objet Retour 
             * (y compris ses retourProduits associés),
             * puis l'envoie pour l'enregistrement dans la BD
             */
            session.beginTransaction();
            session.persist(retour);
            session.getTransaction().commit();
        }
    }

    // Recherche et retourne la liste de tous les retours enregistrés
    public List<Retour> getTousLesRetours() {
        // Ouvre session Hibernate pour accès à BD
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            /*
             * Exécute une requête HQL qui retourne l'ensemble
             * des objets Retour présents dans la base
             */
            return session.createQuery("FROM Retour", Retour.class).list();
        }
    }
}
