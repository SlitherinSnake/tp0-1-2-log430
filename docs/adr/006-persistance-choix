# ADR 006 – Persistance : PostgreSQL centralisé vs SQLite locale

## Status
Accepted

## Context
Le prototype initial (Lab 1) utilisait **SQLite** en fichier local pour sa légèreté.  
Les nouvelles exigences multi-magasins (Lab 2) imposent :  
- Une **base partagée** entre magasins et centre logistique.  
- Des **transactions concurrentes** (ventes simultanées).  
- La possibilité de **répliquer** ou de faire des sauvegardes point-in-time.

## Decision
Migrer vers **PostgreSQL 15** comme SGBD unique, exécuté dans un conteneur Docker et accessible par tous les services Spring Boot.

## Justification
- **Concurrence & ACID** : PostgreSQL gère les transactions multi-session avec verrouillage MVCC.  
- **Extensions** : JSONB, logical replication → utiles pour la future synchronisation offline.  
- **Écosystème** : support total par Spring Data, Testcontainers, pgAdmin.  
- **Outils DevOps** : backups `pg_dump`, monitoring (pg_stat*), intégration facile en CI avec images officielles.

## Consequences
*Avantages*   
- Vue **consolidée** des données (rapports, stock central).  
- Préparation à la **scalabilité** (read replicas, partitionnement).  
- Alignement sur les pratiques professionnelles.

*Inconvénients*   
- Poids et configuration supérieurs à SQLite.  
- Besoin d’un conteneur supplémentaire en développement local.  
- Scripts de migration (Flyway/Liquibase) à maintenir.

## Options envisagées
| Option | Pour | Contre |
|--------|------|--------|
| **SQLite** | Ultra-léger, zéro config | Mono-fichier, pas de concurrence multi-client, pas de réplication |
| **PostgreSQL** | Robuste, riche, multi-utilisateur | Plus lourd, nécessite administration |
| **MySQL/MariaDB** | Populaire, similaire | Transactions moins riches (historiquement), licensing MySQL |

