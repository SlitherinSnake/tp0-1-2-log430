package com.log430.tp1.config;

//Import pour les entités JPA à enregistrer dans la config Hibernate
import com.log430.tp1.model.Employe;
import com.log430.tp1.model.Produit;
import com.log430.tp1.model.Vente;
import com.log430.tp1.model.Retour;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    // Une seule instance de Hibernate par sessionFactory
    private static final SessionFactory sessionFactory;

    static {
        try {
            // Création de mon instance config Hibernate
            Configuration config = new Configuration();

            // ========================
            // Configuration de BD
            // ========================

            // Nom du driver JDBC à utiliser pour PostgreSQL
            config.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
            
            // ========================
            // Choix de l'URL selon le contexte: 
            // ========================

            // Cas 1 : exécution dans Docker (Docker Compose) - (production / VM)
            config.setProperty("hibernate.connection.url", "jdbc:postgresql://db:5432/magasin");

            // Cas 2 : exécution locale (développement/test sur ton poste)
            config.setProperty("hibernate.connection.url", "jdbc:postgresql://localhost:5432/magasin");
            
            // Commande pour me connecter à la base depuis Docker CLI :
            // docker exec -it postgres-db psql -U admin -d magasin

            // Identifiants de connexion à la BD PostgreSQL
            config.setProperty("hibernate.connection.username", "admin");
            config.setProperty("hibernate.connection.password", "admin");

            // Dialecte SQL spécifique à PostgreSQL, utilisé par Hibernate pour générer les requêtes compatibles
            config.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");

            // ========================
            // Option Hibernate: 
            // ========================
            
            // "update" : soit crée les tables si elles n'existent pas, sinon elle les met à jour si nécessaire
            config.setProperty("hibernate.hbm2ddl.auto", "update");

            // Affiche les requêtes SQL générées dans la console pour debug si nécessaire
            config.setProperty("hibernate.show_sql", "true");

            // ========================
            // Enregistrer les entités:
            // ========================

            // Hibernate doit connaître toutes les classes @Entity qu’il va gérer
            config.addAnnotatedClass(Employe.class);
            config.addAnnotatedClass(Produit.class);
            config.addAnnotatedClass(Vente.class);
            config.addAnnotatedClass(Retour.class);

            // La construction de la SessionFactory ce fait à partir de la configuration ci-dessus
            sessionFactory = config.buildSessionFactory();
        } catch (Throwable ex) {
            // Message d'erreur que SessionFactory à crash
            System.err.println("Échec d'initialisation d'Hibernate: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    // Méthode statique pour récupérer l'unique SessionFactory de l'application
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
