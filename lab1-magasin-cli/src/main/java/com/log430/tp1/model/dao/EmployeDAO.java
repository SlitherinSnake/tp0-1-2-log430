package com.log430.tp1.model.dao;

import java.util.List;

import org.hibernate.Session;

import com.log430.tp1.config.HibernateUtil;
import com.log430.tp1.model.Employe;

public class EmployeDAO {

    // Recherche un employé par son ID qui est la clé primaire dans la bd de Employe
    public Employe rechercherParId(int id) {
        // Ouvre session Hibernate pour accès à BD
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            /*
             * Utilise session.get() pour trouver
             * l'entité Employe correspondant à l'ID fourni
             */
            return session.get(Employe.class, id);
        }
    }

    // Recherche tous les employés dont le nom contient une sous-chaîne spécifique
    public List<Employe> rechercherParNom(String nom) {
        // Ouvre session Hibernate pour accès à BD
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            /*
             * Effectue une requête HQL (Hibernate Query Language) pour récupérer
             * tous les employés dont le nom contient le mot-clé fourni
             * Exemple : "ali" retournera "Alice", "Malik", "Khalid", etc.
             */
            return session.createQuery("FROM Employe WHERE lower(nom) LIKE :nom", Employe.class)
                    .setParameter("nom", "%" + nom.toLowerCase() + "%")
                    .list();
        }
    }
}
