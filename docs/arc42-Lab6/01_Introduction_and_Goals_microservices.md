# 1. Introduction et Objectifs

Ce document décrit les exigences et les objectifs qui guident le développement du système de gestion de magasin réalisé dans le cadre du cours LOG430, ayant évolué d'une architecture monolithique vers une architecture microservices complète.

## 1.1. Évolution du projet

### Phase 1 : Application Console (Laboratoire 0-1)
Le projet a débuté comme une application console Java simple avec une architecture 2-tier :
- **Client** : Application console Java interactive (CLI)
- **Base de données** : PostgreSQL via Hibernate ORM
- **Fonctionnalités** : Recherche de produits, enregistrement de ventes, gestion des retours, consultation du stock

### Phase 2 : Application Web Monolithique (Laboratoire 2-4)
Migration vers une architecture web centralisée avec Spring Boot :
- **Interface** : Interface web moderne avec Thymeleaf
- **API REST** : Points d'entrée pour intégrations externes
- **Architecture DDD** : Séparation claire des domaines métier
- **Multi-magasins** : Gestion distribuée de plusieurs points de vente

### Phase 3 : Architecture Microservices (Laboratoire 5)
Transformation complète vers une architecture microservices distribuée :
- **7 services indépendants** : Discovery, Gateway, Frontend, Inventory, Transaction, Store, Personnel
- **Base de données par service** : Isolation complète des données
- **Service Discovery** : Découverte automatique avec Eureka
- **API Gateway** : Point d'entrée unique avec sécurité centralisée
- **Monitoring distribué** : Observabilité complète avec Prometheus et Grafana

## 1.2. Objectifs de l'architecture microservices

### Objectifs techniques
1. **Scalabilité** : Possibilité de scaler indépendamment chaque service selon ses besoins
2. **Résilience** : Isolation des pannes et tolérance aux défaillances
3. **Maintenabilité** : Code modulaire et services autonomes
4. **Déployabilité** : Déploiements indépendants et plus fréquents
5. **Évolutivité** : Facilité d'ajout de nouvelles fonctionnalités

### Objectifs organisationnels
1. **Développement parallèle** : Équipes autonomes par domaine métier
2. **Spécialisation** : Expertise technique focalisée par service
3. **Time-to-market** : Réduction des cycles de développement
4. **Autonomie** : Indépendance des équipes de développement

## 1.3. Architecture distribuée actuelle

Le système comprend désormais :

### Services métier
- **inventory-service** : Gestion des produits et du stock central
- **transaction-service** : Traitement des ventes et retours
- **store-service** : Gestion des magasins et localisations
- **personnel-service** : Gestion des employés et authentification

### Services d'infrastructure
- **discovery-server** : Service de découverte (Eureka)
- **api-gateway** : Routage, authentification et sécurité
- **frontend-service** : Interface web utilisateur

### Infrastructure de support
- **PostgreSQL** : Cluster avec bases de données séparées par service
- **Redis** : Cache distribué et gestion des sessions
- **Prometheus** : Collecte de métriques et monitoring
- **Grafana** : Visualisation et dashboards
- **Docker** : Containerisation et orchestration

## 1.4. Objectifs de qualité

### Qualité technique
1. **Disponibilité** : > 99.9% de disponibilité des services
2. **Performance** : Temps de réponse < 200ms pour 95% des requêtes
3. **Robustesse** : Tolérance aux pannes et récupération automatique
4. **Sécurité** : Authentification centralisée et autorisation par service
5. **Observabilité** : Monitoring complet et traçabilité des opérations

### Qualité fonctionnelle
1. **Extensibilité** : Facilité d'ajout de nouveaux services
2. **Interopérabilité** : APIs REST standardisées
3. **Autonomie** : Fonctionnement indépendant de chaque service
4. **Cohérence** : Données cohérentes malgré la distribution

## 1.5. Parties prenantes

| Rôle                  | Nom               | Intérêt                                                              |
|-----------------------|-------------------|----------------------------------------------------------------------|
| Étudiant développeur  | Vu Minh Vu-Le     | Conçoit, implémente et documente l'architecture microservices       |
| Enseignant            | Fabio Petrillo    | Évalue la rigueur de l'architecture distribuée et sa documentation  |
| Chargé de laboratoire | Hakim Ghlissi     | Fournit un encadrement technique sur les architectures modernes     |
| Équipe DevOps         | -                 | Responsable du déploiement et du monitoring des services            |
| Utilisateurs finaux   | -                 | Employés et clients utilisant l'interface web                       |

## 1.6. Contraintes et décisions architecturales

### Contraintes techniques
- **Containerisation** : Tous les services doivent être containerisés avec Docker
- **Communication** : APIs REST pour la communication inter-services
- **Persistance** : Base de données PostgreSQL dédiée par service
- **Monitoring** : Observabilité complète avec Prometheus/Grafana
- **Sécurité** : Authentification centralisée via API Gateway

### Décisions architecturales majeures
1. **Décomposition par domaine** : Services alignés sur les bounded contexts DDD
2. **Database per service** : Isolation complète des données
3. **API Gateway pattern** : Point d'entrée unique pour la sécurité
4. **Service discovery** : Découverte automatique avec Eureka
5. **Monitoring distribué** : Observabilité centralisée malgré la distribution

## 1.7. Prochaines étapes

### Court terme
- Amélioration de la sécurité (JWT, RBAC)
- Tests automatisés pour chaque service
- Optimisation des performances

### Moyen terme
- Event-driven architecture
- Distributed tracing
- Circuit breakers avancés

### Long terme
- Service mesh (Istio)
- Kubernetes deployment
- Multi-région support
