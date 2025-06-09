# ADR 002 – Séparation des responsabilités (MVC)

## Status
Accepted

## Context
L'application doit être maintenable, modulaire et évolutive. La raison de ce développement est à cause que l'application pourrait évoluer dans les futurs laboratoires. Ceci permet alors la réutilisabilité du code et permet la facilité de la réalisation des tests. Aussi, un facteur important est de faciliter l’intégration avec les couches de persistance et de présentation dans un modèle client-serveur 2-tier. Donc l'utilisation d’un modèle architectural clair s’impose.

## Decision
J’ai choisi d’utiliser le **pattern MVC (Model-View-Controller)** pour organiser l’application. L'architecture MVC est: 

- **Model** : entités JPA, logique métier et DAO.
- **View** : gère l'affichage en ligne de commande. (CLI)
- **Controller** : lien entre view et model.

Le **DAO** est encapsulé dans le `Model` et s’appuie sur **Hibernate** pour l’accès aux données.
## Consequences
* **Avantages :**
  - Code clair, structuré et facilité de maintenance.
  - Meilleure testabilité des composants.
  - Architecture extensible (GUI ou Web dans le futur).

* **Inconvénients :**
  - Exige une bonne structuration dès le départ.
  - Le niveau de complexité initiale du projet augmente légèrement.