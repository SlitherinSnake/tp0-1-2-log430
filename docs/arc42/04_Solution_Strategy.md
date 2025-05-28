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
