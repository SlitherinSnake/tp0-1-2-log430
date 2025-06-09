# 4. Stratégie de solution

Résumé des décisions clés qui structurent l’architecture du système.

## 4.1. Choix technologiques

- Java comme langage principal
- SQLite comme base de données locale
- Hibernate (JPA) pour l’abstraction de la persistance
- Maven pour le build
- JUnit pour les tests unitaires
- GitHub Actions pour le CI/CD
- Docker + Docker Compose pour le déploiement local

## 4.2. Architecture globale

- Pattern MVC (Model-View-Controller)
- Architecture 2-tier : console Java ↔ base de données SQLite
- DAO encapsulé dans le modèle, utilisant Hibernate

## 4.3. Objectifs de qualité visés

- Simple : l'aplication ne doit pas être complexe. 
- Robuste : l'application doit fonctionner de façon stable même en cas de problèmes.
- Autonome : le système doit fonctionner localement sans dépendre d'un réseau.

## 4.4. Organisation

- Documentation basée sur arc42
- Décisions techniques tracées via ADR
- Projet réalisé tout seul

## 4.5. Vue des cas d’utilisation

Acteur principal : Employé 

Cas d’usage principaux :
* Rechercher un produit
* Enregistrer une vente
* Gérer un retour
* Consulter l’état du stock

![cu-menu](https://img.plantuml.biz/plantuml/svg/NP0nJWD134NxbVOENzi0GYb8KgE89A9A0-80rgorZ9YTYSQUI152oXsek04vnzua9s4sMOfDuURx-VlR2r6AcbfN5chLCLQMcaXjowWPXWJrJLBhh93Qu74wV6D33OdrPL4MP3H4hDkj2tlkXSX6oJVPg7hTYtQ__qPMX75hWfUGc_TuMi45GuxlAdoM1P24yxeyzyBcdMDVI1xR6EfajKAEyhPy695h7xcnel6CCdRibGToEYAVk-C5GcGDAGxGR0GjSxZaD7FkTFZfZagAEa4qc8zXO5uMN_sPmyMOJ1ulgSR2z5gONGlptcN1lZw__Wy0)
