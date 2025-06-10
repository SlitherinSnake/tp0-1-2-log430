# 3. Contexte du système

La portée et le contexte du système, comme leur nom l'indique, délimitent votre système (c'est-à-dire votre portée) de tous ses partenaires de communication (systèmes voisins et utilisateurs, c'est-à-dire le contexte de votre système). Ils précisent ainsi les interfaces externes.

Si nécessaire, différenciez le contexte métier (entrées et sorties spécifiques au domaine) du contexte technique (canaux, protocoles, matériel).

## 3.1. Contexte métier

Notre application est un système de caisse pour un petit magasin de quartier. Il permet à un employé d'effectuer les opérations suivantes :

- de rechercher un produit (par identifiant, nom ou catégorie),
- d’enregistrer une vente (sélection des produits et calcul du total),
- de gérer les retours (annuler une vente),
- de consulter l’état du stock des produits.

Les utilisateurs utilisent un CLI pour intéragir avec l'application. 

## 3.2. Contexte technique

L’application s’exécute dans un environnement local, à l’intérieur d’une machine virtuelle. Elle suit une architecture client/serveur à deux niveaux (2-tier) :

- Le **client** est une application Java console.
- Le **serveur** est représenté par une base de données locale PostgreSQL, accédée directement via Hibernate.

La communication se fait entièrement en mémoire locale. Hibernate est l'ORM utilisé pour mapper les entités métiers vers ma base de données relationnelle (PostgreSQL).

