package com.log430.tp3.model;

import jakarta.persistence.*;

// Annotation JPA indiquant que cette classe est une entité persistante
@Entity
// Nom de la table dans la base de données
@Table(name = "produits")
public class Produit {

    // Identifiant unique pour chaque produit
    @Id
    // BD auto-génère les IDs
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private int id;
    private String nom;
    private String categorie;
    private float prix;
    private int quantite;

    // Constructeur par défaut requis par JPA
    public Produit() { }

    // Constructeur avec paramètres pour instancier un produit
    public Produit(String nom, String categorie, float prix, int quantite) {
        this.nom = nom;
        this.categorie = categorie;
        this.prix = prix;
        this.quantite = quantite;
    }

    // Méthode qui vérifie si la quantité demandée est disponible en stock
    // Retourne true si le stock actuel est supérieur ou égal à la quantité demandée
    public boolean hasStock(int qte) { return quantite >= qte; }

    // Méthode qui diminue le stock du produit d'une certaine quantité
    // Lève une exception si la quantité demandée dépasse le stock disponible
    public void decreaseStock(int qte) {
        if (!hasStock(qte))
            throw new IllegalArgumentException("Stock insuffisant");
        quantite -= qte;
    }

    // Getter/Setter
    public int getId() { return id; }
    // Pour test
    public int setId(int id) { return this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public float getPrix() { return prix; }
    public void setPrix(float prix) { this.prix = prix; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }

    @Override
    public String toString() {
        return "Produit{id=" + id + ", nom='" + nom + "', categorie='" + categorie +
                "', prix=" + prix + ", quantite=" + quantite + "}";
    }
}
