# Documentation arc42 - Architecture Microservices

## Table des matières

Cette documentation arc42 décrit l'architecture microservices du système de gestion de magasin, résultant de la migration d'une application monolithique Spring Boot vers une architecture distribuée moderne.

### Structure de la documentation

1. **[Introduction et objectifs](01_Introduction_and_Goals_microservices.md)**
   - Vue d'ensemble du système
   - Parties prenantes et exigences
   - Objectifs de qualité

2. **[Contraintes d'architecture](02_Architecture_Constraints_microservices.md)**
   - Contraintes techniques et organisationnelles
   - Conventions et standards

3. **[Portée et contexte du système](03_Context_and_Scope_microservices.md)**
   - Contexte métier et technique
   - Interfaces externes
   - Frontières du système

4. **[Stratégie de solution](04_Solution_Strategy_microservices.md)**
   - Décisions technologiques clés
   - Patterns architecturaux
   - Approche de migration

5. **[Vue des blocs de construction](05_Building_Block_View_microservices.md)**
   - Architecture globale
   - Décomposition en microservices
   - Interfaces entre services

6. **[Vue d'exécution](06_Runtime_View_microservices.md)**
   - Scénarios d'exécution
   - Flux de données
   - Comportements dynamiques

7. **[Vue de déploiement](07_Deployment_View_microservices.md)**
   - Infrastructure Docker
   - Configuration des environnements
   - Stratégies de déploiement

8. **[Concepts transversaux](08_Crosscutting_Concepts_microservices.md)**
   - Patterns de communication
   - Sécurité transversale
   - Monitoring et observabilité

9. **[Décisions d'architecture](09_Architecture_Decisions_microservices.md)**
   - Décisions clés et justifications
   - Alternatives considérées
   - Conséquences et trade-offs

10. **[Exigences de qualité](10_Quality_Requirements_microservices.md)**
    - Scénarios de qualité
    - Métriques et SLA
    - Stratégies de test

11. **[Risques et dette technique](11_Risks_Technical_Debt_microservices.md)**
    - Analyse des risques
    - Dette technique identifiée
    - Plans de mitigation

12. **[Glossaire](12_Glossary_microservices.md)**
    - Définitions techniques
    - Termes métier
    - Acronymes

## Architecture Overview

L'architecture microservices comprend :

### Services métier
- **inventory-service** (8081) : Gestion des produits et stocks
- **transaction-service** (8082) : Gestion des ventes et transactions
- **store-service** (8083) : Gestion des magasins
- **personnel-service** (8084) : Gestion des employés

### Services d'infrastructure
- **discovery-server** (8761) : Service discovery Eureka
- **api-gateway** (8765) : Point d'entrée et routage
- **frontend-service** (8080) : Interface utilisateur

### Infrastructure
- **PostgreSQL** (5432) : Bases de données par service
- **Redis** (6379) : Cache distribué et sessions
- **Prometheus** (9090) : Collecte de métriques
- **Grafana** (3000) : Dashboards de monitoring

## Technologies principales

- **Java 21** + **Spring Boot 3.2**
- **Spring Cloud 2025.0.0** (Gateway, Eureka, LoadBalancer)
- **PostgreSQL 14** avec pattern Database per Service
- **Docker** + **Docker Compose** pour la conteneurisation
- **Prometheus** + **Grafana** pour l'observabilité

## Liens vers la documentation connexe

- **[ADR (Architecture Decision Records)](../ADR-Lab5/)** : Décisions architecturales détaillées
- **[Diagrammes](../diagrams/)** : Représentations visuelles de l'architecture
- **[Code source](../../)** : Implémentation des microservices

## État de la documentation

| Section | Statut | Dernière mise à jour |
|---------|--------|---------------------|
| 01 - Introduction | ✅ Complète | 2024 |
| 02 - Contraintes | ✅ Complète | 2024 |
| 03 - Contexte | ✅ Complète | 2024 |
| 04 - Stratégie | ✅ Complète | 2024 |
| 05 - Blocs de construction | ✅ Complète | 2024 |
| 06 - Vue d'exécution | ✅ Complète | 2024 |
| 07 - Déploiement | ✅ Complète | 2024 |
| 08 - Concepts transversaux | ✅ Complète | 2024 |
| 09 - Décisions | ✅ Complète | 2024 |
| 10 - Qualité | ✅ Complète | 2024 |
| 11 - Risques | ✅ Complète | 2024 |
| 12 - Glossaire | ✅ Complète | 2024 |

---

*Cette documentation est maintenue à jour avec l'évolution du projet. Pour toute question ou suggestion d'amélioration, veuillez contacter l'équipe d'architecture.*
