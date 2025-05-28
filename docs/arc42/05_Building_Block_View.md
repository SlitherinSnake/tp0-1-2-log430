# 5. Vue des blocs de construction

## 5.1. Boîte blanche – Vue d’ensemble du système

### Diagramme
![Architecture 2-tier - Vue des blocs (MVC)](image.png)

### Motivation
J'ai pris l'architecture MVC, car je suis familier avec. Aussi elle offre une séparation claire entre l’interface utilisateur, la logique métier et la gestion des interactions utilisateurs.

### Blocs principaux (black box)

| Bloc         | Rôle                                              |
|--------------|---------------------------------------------------|
| View         | Affichage console et saisie utilisateur (CLI)     |
| Controller   | Gère les actions de l’utilisateur et appelle les services/DAO |
| Model/DAO    | Contient les entités JPA et l’accès aux données via Hibernate |
| SQLite       | Base de données locale relationnelle              |

## 5.2. Niveau 2

| Sous-bloc     | Description                                      |
|---------------|--------------------------------------------------|
| ProduitDAO    | Accès aux produits : recherche, liste, stock     |
| VenteDAO      | Enregistrement d’une vente                       |
| RetourDAO     | Gestion des retours et ajustement du stock       |
| HibernateUtil | Initialise les sessions Hibernate                |

## 5.3. Niveau 3

Ce niveau de détail n’est pas nécessaire dans notre contexte. Notre application repose sur une architecture à deux couches (client-console et base de données). Aucun sous-bloc supplémentaire n’est requis à ce stade.
