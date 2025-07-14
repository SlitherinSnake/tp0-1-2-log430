# ADR 003 – Migration de SQLite vers PostgreSQL

## Status

Accepted

## Context

L'application initiale utilisait SQLite comme base de données locale pour sa simplicité et sa facilité de déploiement. Cependant, avec l'évolution vers une architecture microservices distribuée et l'ajout de fonctionnalités avancées (transactions concurrentes, monitoring, analytics), les limitations de SQLite sont devenues apparentes. De plus, l'architecture microservices nécessite une base de données robuste capable de gérer plusieurs services avec des bases de données séparées.

## Decision

Migration vers **PostgreSQL** comme système de gestion de base de données principal avec **architecture multi-base** pour les microservices :

- **PostgreSQL 14+** en production et développement
- **Base de données séparée** pour chaque microservice :
  - `gateway_db` : API Gateway et routage
  - `inventory_db` : Service d'inventaire
  - `transaction_db` : Service de transactions
  - `store_db` : Service de magasins
  - `personnel_db` : Service personnel
- **H2** pour les tests unitaires et l'environnement de développement rapide
- Configuration via Spring profiles (`docker` et `local`)
- Support des transactions ACID complètes
- Gestion de la concurrence et du verrouillage optimisé
- **Isolation des données** par microservice selon les principes DDD

## Justification

PostgreSQL offre des fonctionnalités avancées nécessaires pour une application de gestion de magasin :

- Support complet des transactions ACID
- Gestion de la concurrence avec verrouillage au niveau des lignes
- Performance supérieure avec de gros volumes de données
- Support natif des types JSON pour des données flexibles
- Outils de monitoring et d'analyse intégrés
- Compatibilité excellente avec Spring Boot et JPA

## Consequences

- **Avantages :**
  - Performance améliorée pour les requêtes complexes
  - Support robuste de la concurrence
  - Transactions ACID complètes
  - Extensibilité pour de futurs besoins
  - Outils de monitoring avancés
  - Support des requêtes analytiques

- **Inconvénients :**
  - Nécessite un serveur PostgreSQL (ajouté via Docker Compose)
  - Configuration plus complexe qu'avec SQLite
  - Empreinte mémoire supérieure

## Migration

La migration inclut :

- Script de migration des données existantes
- Configuration des profiles Spring
- Mise à jour du Docker Compose pour inclure PostgreSQL
- Tests de régression pour valider l'intégrité des données

## Options envisagées

- Alternative 1 : Garder SQLite (limitée pour la concurrence)
- Alternative 2 : MySQL (moins de fonctionnalités avancées)
- Alternative 3 : MongoDB (pas adapté pour les données relationnelles)
