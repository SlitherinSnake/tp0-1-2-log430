# ADR 003 – Stratégie de persistance

## Status
Accepted

## Context
L'application doit contenir une couche de persistance abstraite. Celle-ci doit être intégrable avec Java. Afin de permettre l’enregistrement de ventes, le suivi de stock et la gestion des retours. Tout cela en s'assurant que c'est cohérent avec les données.

L'application fonctionnent que en local, l’accès aux données doit être direct depuis l’application console via une couche d’abstraction.

Dans le cadre du laboratoire, on impose l’utilise d'un ORM (Object-Relational Mapping) pour illustrer l’abstraction de la base de données.

## Decision
J'utilise' **Hibernate JPA** comme solution de persistance.

Hibernate est un framework ORM en Java qui simplifie l'interaction entre les applications Java et les bases de données. Elle fait cela en gèrent automatiquement la persistance des données.  

Les entités JPA seront utilisées dans la couche `model`, et les classes DAO vont encapsuler les opérations de persistance en utilisant Hibernate.

## Consequences
* **Avantages :**
  - L’interaction avec la base de données est simplifier.
  - Réduit les erreurs liées aux requêtes SQL.
  - S’intègre bien avec les projets Maven/Java.
  - Standard reconnu dans le monde Java (JPA).
  - Favorise une architecture claire (DAO, entités, services).

* **Inconvénients :**
  - Nécessite une configuration initiale (fichier `hibernate.cfg.xml` ou équivalent).
  - Peut se révéler être beaucoup pour un petit projet.
  - Nécessite une connaissance des annotations JPA et de la gestion des sessions Hibernate.
