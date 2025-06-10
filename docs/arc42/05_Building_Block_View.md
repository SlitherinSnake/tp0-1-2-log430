# 5. Vue des blocs de construction

## 5.1. Boîte blanche – Vue d’ensemble du système

### Diagramme
### 2.1. Vue logique
#### Diagramme de classe représentant les entités et composants principaux du système :
![dcl](https://img.plantuml.biz/plantuml/svg/lLRBRjim4BmBq3yiV8cTmYXoDP0WG9n3WQJ8eqZFZBGMY5Aa8r95YwB_qcFz7VwnBb-Ig3ZEeqKl0SnPpixExb8vrOOrfgr5dXbk1CAIiwfXTOrG8XI2QOqmXqjMCSqbVCPxcDwr249L_A75MPxbsG4S7n_3kII2xKR3kk6ou6icK5l0HZWUEA_NGdr7-95dG9oyX0LmQVmlgMhwUMleSXLn9CeLPzAaoDp-VGSLcW_bTDQJkAEFgdPdVNo8xCYcinxzsQkB9RvGtKDBld5i_bxQTM1Ai86mgqQLBJSl5LOmWvLgE8xEgItVw6Wb53DT2RPdEE1xY_wpc70NU4LPtNcNDzRiKlSv1WNVKQkUc9AISdbu0HUgO6991nwgbJH4-aaP9f8wzzQqZ8GEIIWTVZbW7TOnbhktIQyrUgyxDsXKspnjJy37_ZphWhytjk5HulzNwArHnPUzM_dlqvrHBnkQdxtfbbWpMIAEdG_dhxF-UaIwipS1tDaS__hUe626AwdjittFwD4EJ-z1U7Qt6sYs6oOuGL_jn6f00uthgSwp-3PFZYOa-0GcXnE8YuQh5HQ67lsJtN7Tn95rjXidFg-_BlXsWx3z1Qs4sZSpppnaxpeEuefzNj05R2D5OBOSdY9epxE0T3AEUhc5aWKsSgJZS3vFDtLQg7gjDBUkpJf9n6CZk_9ZjAEbLvg-HQas6vrkneXue3Gof_6FgIzu9T5rqbNxtdUfAum2oSorlnwKHxAXfuC4_5uuFzBCpmHRAR4lpARKTaNeqqy3c6SH6Kv2ih8B81VqzdVPaSKSQvf4RSie6GrimfemxD3dYLmK565P5sumoh1zjCkgeAzVRz5u-f9DtvdZoOZBo95dfoXBzt_J7m00)

###  Modèle de domaine – *Magasin Web (Spring Boot)*

| Classe             | Attributs clés                                                                                   | Description                                                                                     |
|--------------------|--------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------|
| `Employe`          | `id: int`, `nom: String`, `identifiant: String`                                                  | Représente un employé pouvant effectuer des ventes et des retours.                             |
| `Produit`          | `id: int`, `nom: String`, `categorie: String`, `prix: float`, `quantite: int`                    | Article disponible en stock, vendable ou retournable.                                          |
| `Vente`            | `id: int`, `dateVente: LocalDate`, `montantTotal: double`, `employe`, `magasin`, `venteProduits`| Vente complète validée par un employé, contenant plusieurs produits.                           |
| `Retour`           | `id: int`, `dateRetour: LocalDate`, `vente`, `employe`, `retourProduits`                         | Retour effectué après une vente existante, validé par un employé.                              |
| `VenteProduit`     | `id: int`, `vente`, `produit`, `quantite: int`                                                   | Ligne de vente d’un produit avec quantité.                                                     |
| `RetourProduit`    | `id: int`, `retour`, `produit`, `quantite: int`                                                  | Ligne de retour d’un produit avec quantité.                                                    |
| `Magasin`          | `id: int`, `nom: String`, `quartier: String`, `ventes`                                           | Point de vente physique, associé aux stocks et aux ventes.                                     |
| `StockMagasin`     | `id: int`, `produit`, `magasin`, `quantite: int`                                                 | Stock local d’un produit dans un magasin donné.                                                |
| `StockCentral`     | `id: int`, `produit`, `magasin`, `quantiteDemandee: int`, `dateDemande: LocalDate`              | Demande de réapprovisionnement effectuée par un magasin.                                       |

---

### Architecture Web MVC – *Spring Boot*

| Composant                  | Rôle                                                                                     |
|----------------------------|------------------------------------------------------------------------------------------|
| `VenteController`          | Gère le panier, les ajouts/retraits de produits, la validation de vente (via AJAX).     |
| `RetourController`         | Permet de sélectionner une vente passée et de retourner un ou plusieurs produits.       |
| `ProduitController`        | Gère l’affichage, l’ajout, la suppression, la modification et la recherche de produits. |
| `DashboardController`      | Affiche les indicateurs (ruptures, surstocks, CA, tendances hebdomadaires).             |
| `RapportController`        | Génère les rapports consolidés : par magasin, par produit, stock global.                |
| `StockCentralController`   | Permet de consulter le stock central et d’envoyer des demandes de réapprovisionnement.  |

| Service                    | Rôle                                                                                     |
|----------------------------|------------------------------------------------------------------------------------------|
| `RapportService`           | Calcule les ventes par magasin, identifie les produits les plus vendus, retourne le stock actuel. |
| `StockCentralService`      | Gère les demandes de réapprovisionnement, filtre les doublons, affiche l’historique.    |

| Repository                 | Rôle                                                                                     |
|----------------------------|------------------------------------------------------------------------------------------|
| `ProduitRepository`        | Accès aux produits, requêtes de filtrage par nom/catégorie, stock critique.             |
| `VenteRepository`          | Accès aux ventes, calculs de chiffre d’affaires, produits les plus vendus.              |
| `RetourRepository`         | Accès aux retours, quantités déjà retournées par vente/produit.                         |
| `EmployeRepository`        | Accès aux employés.                                                                      |
| `MagasinRepository`        | Accès aux magasins.                                                                      |
| `StockMagasinRepository`   | Accès aux stocks locaux, recherche par magasin et produit.                              |
| `StockCentralRepository`   | Accès aux demandes de réapprovisionnement central.   

### Motivation
J'ai pris l'architecture MVC, car je suis familier avec. Aussi elle offre une séparation claire entre l’interface utilisateur, la logique métier et la gestion des interactions utilisateurs.

### Blocs principaux (black box)

| Bloc         | Rôle                                              |
|--------------|---------------------------------------------------|
| View         | Affichage console et saisie utilisateur (CLI)     |
| Controller   | Gère les actions de l’utilisateur et appelle les services/DAO |
| Model/DAO    | Contient les entités JPA et l’accès aux données via Hibernate |
| PostgreSQL       | Base de données locale relationnelle              |

## 5.2. Niveau 2

| Sous-bloc     | Description                                      |
|---------------|--------------------------------------------------|
| EmployeDAO    | L'employer qui fait les ventes, retours et consulation du stock        |
| ProduitDAO    | Accès aux produits : recherche, liste, stock     |
| VenteDAO      | Enregistrement d’une vente                       |
| RetourDAO     | Gestion des retours et ajustement du stock       |
| HibernateUtil | Initialise les sessions Hibernate                |

### Vue d’implémentation – Organisation des modules (Spring Boot Web)
L’application suit une architecture **MVC enrichie**, avec une séparation claire des responsabilités :

- **Contrôleurs (`controller`)** : Gèrent les interactions utilisateur (formulaires, AJAX, vues Thymeleaf).
- **Services (`service`)** : Appliquent les règles d’affaires (calculs, regroupements, filtrage, validations métier).
- **Repositories (`repository`)** : Abstraction de la base de données via Spring Data JPA (équivalent DAO).
- **Modèles (`model`)** : Entités JPA mappées aux tables de la base de données (`Produit`, `Vente`, `Retour`, etc.).
- **Vues (`.html`)** : Templates Thymeleaf côté client (non inclus dans ce diagramme technique mais couplés aux contrôleurs).


![di](https://img.plantuml.biz/plantuml/svg/dPL1JiCm44NtaNo7KJU0K2i4R8ig5POLLIfrRn8hM7NYo7QGAeJK5GU05KTWpmrwavu4awHf6uyRODKc_zkPFtdZZBIXoXIf887XHZ2O5mpYpUg3fxbOVwOiCzHmcS5czGxtAg4PrzMDc6b8PLm8-tiwcO_X76PCFV68sNyXowNcHghb6Gb8S0B3uH2cD5hGnDhj1GdowWfwaKm7GYRNLnS3aryE8faP9OLWgWVF90281DKQfie-Yvjne-wakIsIOKB8Z2mKfioCZ1PZkqvHWQwaUIuL-hHRgXyV95NnllZoazANI5TjEOXJFw3k65vO8nxTNJMjnlgrUn6jVc52rIzCkRCqo2MwQ48Jgdc6IMMOBhqEkwyVpMkPOmABsWhOZkM4QVcTNQfbpkqw3vuIprOdggOWPA8S80qbwUilmGgbOVFw1ZKezO0u34A_V-E0WvDSMb6MVb56GnJVbk8CNxa_WbXh5FzYSwZ4J0Y7c0rXplQpTHfKxNIGHsrESQWBIKe2vBp0j_GPVLFaywDL7C44sReBgdyiyN4uspB_Grxea7U8k-TTk9xE7dIOVTQEuFjMt-56WX7BulAB-mq0)

## 5.3. Niveau 3

Ce niveau de détail n’est pas nécessaire dans notre contexte. Notre application repose sur une architecture à deux couches (client-console et base de données). Aucun sous-bloc supplémentaire n’est requis à ce stade.
