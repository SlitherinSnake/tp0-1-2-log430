# ADR 002 – Choix du mécanisme de base de données (SQL vs NoSQL, local vs serveur)

## Status
Accepted

## Context
Mon application fonctionne localement dans une VM. Elle ne nécessite ni un serveur distant ni un accès réseau. La base de données doit être compatible avec mon ORM choisi qui est Hibernate. Celle-ci doit gérer les opérations de vente, de retour et de mise à jour du stock.  

## Decision
J'ai choisi **SQLite**, une base de données relationnelle **locale** qui est compatible avec Hibernate. La raison est le fait qu'elle fonctionne tout simplement avec un fichier `.db`. Ce qui veux dire que l'installation d'un serveur n'est pas nécessaire pour son fonctionnement. De plus, cela répond à l'architecture 2-tier imposée.

## Justification

J'ai choisi SQLite pour sa légèreté, sa portabilité, sa simplicité de configuration et sa parfaite compatibilité avec Hibernate. 

## Consequences
* **Avantages :**
  - Aucun serveur requis : fonctionne avec un fichier local.
  - SQLite est Légère, portable, simple à configurer.
  - Compatible avec Hibernate/JPA.
  - Idéale pour notre petit projet.

* **Inconvénients :**
  - Moins adaptée aux environnements multi-utilisateurs ou distribués.
  - Moins performante qu’un serveur comme PostgreSQL sous forte charge.

## Options envisagées

- Alternative 1 : MongoDB
- Alternative 2 : PostgreSQL