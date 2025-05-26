package com.log430.tp1;

//import com.log430.tp1.config.HibernateUtil;
import com.log430.tp1.view.MagasinView;

//import jakarta.persistence.EntityManager;

public class MagasinApp {
    public static void main(String[] args) {
        new MagasinView().start();

    }
    /*
     * Pour tester la connexion à la db
     * public static void main(String[] args) {
     * 
     * System.out.println("Test de connexion PostgreSQL avec Hibernate");
     * 
     * EntityManager em = HibernateUtil.getSessionFactory().createEntityManager();
     * 
     * try {
     * em.getTransaction().begin();
     * System.out.println("Connexion réussie !");
     * em.getTransaction().commit();
     * } catch (Exception e) {
     * System.err.println("Échec de la connexion : " + e.getMessage());
     * } finally {
     * em.close();
     * }
     * }
     */
}
