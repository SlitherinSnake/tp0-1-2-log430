# 12. Glossaire

## 12.1. Termes techniques

### 12.1.1. Architecture et patterns

**API Gateway**
: Point d'entrée unique pour toutes les requêtes client vers les microservices. Gère l'authentification, l'autorisation, le routage, la limitation de débit et l'agrégation des réponses. Dans ce projet : Spring Cloud Gateway sur le port 8765.

**Circuit Breaker**
: Pattern de résilience qui prévient les appels vers un service défaillant. Trois états : CLOSED (normal), OPEN (service en panne), HALF_OPEN (test de récupération). Implémenté avec Resilience4j.

**Database per Service**
: Pattern microservices où chaque service possède sa propre base de données pour garantir l'isolation des données et l'autonomie. Cinq bases PostgreSQL distinctes dans le projet.

**Domain-Driven Design (DDD)**
: Approche de conception logicielle qui place le domaine métier au centre du développement. Utilisé pour découper l'application monolithique en microservices cohérents.

**Event Sourcing**
: Pattern qui stocke tous les changements d'état comme une séquence d'événements immuables. Prévu pour améliorer la cohérence des données distribuées.

**Microservices**
: Style architectural qui structure une application comme un ensemble de services faiblement couplés, déployables indépendamment. Sept services dans l'architecture actuelle.

**Service Discovery**
: Mécanisme permettant aux services de se localiser et communiquer automatiquement. Implémenté avec Netflix Eureka sur le port 8761.

**Service Mesh**
: Infrastructure dédiée à la communication inter-services avec fonctionnalités de sécurité, observabilité et gestion du trafic. Considéré pour les évolutions futures.

### 12.1.2. Technologies et frameworks

**Docker**
: Plateforme de conteneurisation qui encapsule les applications et leurs dépendances. Tous les services sont containerisés pour la portabilité et la cohérence.

**Docker Compose**
: Outil d'orchestration pour définir et exécuter des applications multi-conteneurs. Utilisé pour déployer l'ensemble de la stack microservices.

**Eureka**
: Service de découverte développé par Netflix, intégré à Spring Cloud. Permet l'enregistrement automatique et la découverte des instances de services.

**Grafana**
: Plateforme d'analyse et de monitoring interactive. Utilisée pour visualiser les métriques Prometheus via des dashboards sur le port 3000.

**HikariCP**
: Pool de connexions JDBC haute performance. Configuration par défaut de Spring Boot pour gérer les connexions PostgreSQL.

**Micrometer**
: Façade de métriques pour les applications JVM. Intégré à Spring Boot Actuator pour exporter les métriques vers Prometheus.

**PostgreSQL**
: Système de gestion de base de données relationnel objet. Version 14 utilisée avec une instance par service pour l'isolation des données.

**Prometheus**
: Système de monitoring et d'alerting avec base de données time-series. Collecte les métriques des services via l'endpoint `/actuator/prometheus`.

**Redis**
: Base de données clé-valeur en mémoire. Utilisée pour le cache distribué et la gestion des sessions utilisateur sur le port 6379.

**Resilience4j**
: Bibliothèque de résilience inspirée de Netflix Hystrix. Fournit Circuit Breaker, Rate Limiter, Retry, Time Limiter et Bulkhead patterns.

**Spring Boot**
: Framework Java qui simplifie le développement d'applications Spring. Version 3.2 avec Java 21 pour tous les microservices.

**Spring Cloud**
: Suite d'outils pour développer des systèmes distribués. Version 2025.0.0 avec Gateway, Eureka et LoadBalancer.

**Spring Cloud Gateway**
: Gateway API réactif basé sur Spring WebFlux. Remplace Zuul pour le routage et le filtrage des requêtes.

**Thymeleaf**
: Moteur de template Java pour les applications web. Utilisé dans le frontend-service pour le rendu côté serveur.

### 12.1.3. Monitoring et observabilité

**Actuator**
: Module Spring Boot qui fournit des endpoints de monitoring et de gestion. Expose `/actuator/health`, `/actuator/prometheus`, `/actuator/info`.

**Golden Signals**
: Quatre métriques clés pour monitorer un service : Latency (latence), Traffic (trafic), Errors (erreurs), Saturation (saturation).

**Health Check**
: Vérification automatique de l'état d'un service. Inclut les checks de base de données, services externes et ressources critiques.

**MTTR (Mean Time To Recovery)**
: Temps moyen de récupération après un incident. Objectif < 5 minutes pour les pannes critiques.

**Prometheus Query Language (PromQL)**
: Langage de requête pour interroger les métriques time-series de Prometheus. Utilisé dans Grafana et les alertes.

**SLA (Service Level Agreement)**
: Accord formel définissant le niveau de service attendu. Exemple : disponibilité 99.9%, latence < 200ms.

**SLI (Service Level Indicator)**
: Métrique quantitative d'un aspect du niveau de service. Exemple : pourcentage de requêtes réussies.

**SLO (Service Level Objective)**
: Objectif de performance pour un SLI. Exemple : 99.9% des requêtes doivent réussir.

**Time Series Database (TSDB)**
: Base de données optimisée pour les données horodatées. Prometheus utilise une TSDB pour stocker les métriques.

### 12.1.4. Sécurité

**Authentication**
: Processus de vérification de l'identité d'un utilisateur. Gérée par l'API Gateway avec JWT tokens.

**Authorization**
: Processus de vérification des permissions d'un utilisateur authentifié. Basée sur les rôles : ADMIN, MANAGER, EMPLOYEE.

**CORS (Cross-Origin Resource Sharing)**
: Mécanisme permettant à une page web d'accéder à des ressources d'un autre domaine. Configuré dans l'API Gateway.

**CSRF (Cross-Site Request Forgery)**
: Attaque forçant un utilisateur authentifié à exécuter des actions non désirées. Protection désactivée pour les APIs REST.

**JWT (JSON Web Token)**
: Standard ouvert pour transmettre des informations de manière sécurisée. Utilisé pour l'authentification stateless.

**OAuth 2.0**
: Framework d'autorisation permettant l'accès sécurisé aux ressources. Protocole utilisé avec Spring Security.

**Rate Limiting**
: Technique de contrôle du nombre de requêtes par unité de temps. Implémentée dans l'API Gateway avec Redis.

**RBAC (Role-Based Access Control)**
: Modèle de contrôle d'accès basé sur les rôles utilisateur. Quatre rôles définis : ADMIN, MANAGER, EMPLOYEE, CUSTOMER.

## 12.2. Termes métier

### 12.2.1. Domaine magasin

**Catalogue**
: Ensemble des produits disponibles à la vente avec leurs caractéristiques, prix et disponibilité.

**Catégorie**
: Classification hiérarchique des produits pour faciliter la navigation et la gestion. Exemple : Électronique > Smartphones.

**Employee (Employé)**
: Personne travaillant dans l'organisation avec des droits d'accès spécifiques selon son rôle et département.

**Inventaire**
: Ensemble des stocks de produits disponibles dans les différents magasins avec suivi des quantités.

**Magasin**
: Point de vente physique avec ses employés, stocks locaux et configuration spécifique.

**Produit**
: Article vendable identifié par un SKU avec nom, description, prix et catégorie.

**SKU (Stock Keeping Unit)**
: Identifiant unique d'un produit dans le système d'inventaire. Format : lettres et chiffres.

**Stock**
: Quantité disponible d'un produit dans un magasin donné, incluant les réservations temporaires.

**Transaction**
: Opération de vente incluant un ou plusieurs produits, client, montant total et magasin.

### 12.2.2. Domaine utilisateurs

**Administrateur**
: Utilisateur avec accès complet au système, gestion des utilisateurs et configuration globale.

**Client**
: Personne effectuant des achats dans le magasin, peut avoir un compte pour le suivi des transactions.

**Département**
: Division organisationnelle regroupant des employés par fonction : Ventes, Gestion, IT, RH.

**Manager**
: Utilisateur avec droits étendus sur son magasin ou département, peut gérer les employés.

**Poste (Position)**
: Fonction spécifique d'un employé avec responsabilités et permissions définies.

**Rôle**
: Ensemble de permissions attribuées à un utilisateur : ADMIN, MANAGER, EMPLOYEE, CUSTOMER.

**Session**
: Période d'activité d'un utilisateur authentifié, gérée par Redis pour la scalabilité.

**Utilisateur**
: Personne ayant accès au système avec identifiants, profil et permissions spécifiques.

## 12.3. Termes DevOps

### 12.3.1. Déploiement et infrastructure

**Blue-Green Deployment**
: Stratégie de déploiement avec deux environnements identiques pour basculement sans interruption.

**CI/CD (Continuous Integration/Continuous Deployment)**
: Pratique d'intégration continue et déploiement automatisé. Pipeline GitHub Actions prévu.

**Configuration Drift**
: Divergence progressive entre la configuration déployée et la configuration de référence.

**Container Registry**
: Dépôt pour stocker et distribuer les images Docker. Docker Hub utilisé actuellement.

**Environment Parity**
: Principe de similitude entre les environnements de développement, test et production.

**Horizontal Scaling**
: Augmentation de la capacité en ajoutant plus d'instances de service plutôt qu'en augmentant les ressources.

**Infrastructure as Code (IaC)**
: Gestion de l'infrastructure via du code versionné. Docker Compose files pour définir l'architecture.

**Load Balancer**
: Composant distribuant le trafic entre plusieurs instances d'un service. Spring Cloud LoadBalancer utilisé.

**Rolling Update**
: Stratégie de mise à jour graduelle des instances de service sans interruption complète.

**Service Mesh**
: Infrastructure réseau dédiée pour la communication inter-services avec observabilité et sécurité intégrées.

### 12.3.2. Monitoring et qualité

**APM (Application Performance Monitoring)**
: Surveillance des performances applicatives avec métriques détaillées et traces.

**Alerting**
: Système de notification automatique basé sur des seuils de métriques ou événements critiques.

**Chaos Engineering**
: Practice d'injection de pannes contrôlées pour tester la résilience du système.

**Log Aggregation**
: Centralisation et corrélation des logs de tous les services pour faciliter l'analyse.

**Observability**
: Capacité à comprendre l'état interne d'un système via ses outputs : métriques, logs, traces.

**Runbook**
: Documentation procédurale pour les opérations courantes et gestion d'incidents.

**Synthetic Monitoring**
: Tests automatisés simulant des interactions utilisateur pour détecter les problèmes proactivement.

**Tracing**
: Suivi des requêtes à travers les différents services pour analyser les performances et erreurs.

## 12.4. Métriques et KPIs

### 12.4.1. Métriques techniques

**CPU Utilization**
: Pourcentage d'utilisation du processeur. Seuil d'alerte à 85%, objectif < 70%.

**Database Connection Pool**
: Nombre de connexions actives/disponibles vers la base de données. Pool HikariCP configuré.

**Error Rate**
: Pourcentage de requêtes échouées (HTTP 4xx/5xx). Objectif < 1%, alerte > 2%.

**Garbage Collection (GC)**
: Nettoyage automatique de la mémoire JVM. Surveillance des pauses GC et fréquence.

**Latency P95**
: 95e percentile du temps de réponse. 95% des requêtes traitées sous ce seuil. Objectif < 200ms.

**Memory Usage**
: Consommation mémoire des conteneurs. Seuil d'alerte à 90%, objectif < 80%.

**Network I/O**
: Trafic réseau entrant/sortant des services. Surveillance de la bande passante et latence.

**Requests Per Second (RPS)**
: Nombre de requêtes traitées par seconde. Métrique de charge et capacité.

**Thread Pool**
: État du pool de threads Tomcat. Surveillance des threads actifs/disponibles.

**Throughput**
: Débit de traitement des requêtes ou transactions par unité de temps.

### 12.4.2. Métriques métier

**Average Transaction Value**
: Valeur moyenne des transactions pour analyser les performances commerciales.

**Customer Acquisition Rate**
: Taux d'acquisition de nouveaux clients sur une période donnée.

**Inventory Turnover**
: Rotation des stocks pour optimiser la gestion d'inventaire.

**Peak Hour Traffic**
: Charge maximale pendant les heures de pointe pour dimensionner l'infrastructure.

**Product Popularity**
: Classement des produits par volume de ventes pour optimiser le catalogue.

**Revenue Per Hour**
: Chiffre d'affaires par heure pour analyser les performances temporelles.

**Stock Shortage Events**
: Nombre d'événements de rupture de stock impactant les ventes.

**User Session Duration**
: Durée moyenne des sessions utilisateur pour évaluer l'engagement.

## 12.5. Outils et technologies

### 12.5.1. Stack technique actuelle

| Outil | Version | Usage | Port |
|-------|---------|-------|------|
| **Spring Boot** | 3.2 | Framework application | - |
| **Spring Cloud** | 2025.0.0 | Patterns microservices | - |
| **PostgreSQL** | 14 | Base de données | 5432 |
| **Redis** | 7-alpine | Cache et sessions | 6379 |
| **Prometheus** | latest | Collecte métriques | 9090 |
| **Grafana** | latest | Visualisation | 3000 |
| **Docker** | latest | Conteneurisation | - |
| **Docker Compose** | 3.8 | Orchestration | - |

### 12.5.2. Services déployés

| Service | Description | Port | Base de données |
|---------|-------------|------|-----------------|
| **discovery-server** | Service discovery Eureka | 8761 | - |
| **api-gateway** | Point d'entrée unique | 8765 | gateway_db |
| **frontend-service** | Interface utilisateur | 8080 | - |
| **inventory-service** | Gestion produits/stocks | 8081 | inventory_db |
| **transaction-service** | Gestion ventes | 8082 | transaction_db |
| **store-service** | Gestion magasins | 8083 | store_db |
| **personnel-service** | Gestion employés | 8084 | personnel_db |

### 12.5.3. Environnements

**Développement local**
: Environnement Docker Compose sur machine développeur avec hot reload et debug.

**Test (CI)**
: Environnement éphémère créé lors des pipelines CI/CD avec TestContainers.

**Staging**
: Environnement de pré-production miroir de la production pour les tests d'acceptation.

**Production**
: Environnement de production avec monitoring, alerting et backups automatiques.

## 12.6. Processus et méthodologies

### 12.6.1. Développement

**Agile/Scrum**
: Méthodologie de développement itératif avec sprints de 2 semaines.

**Code Review**
: Processus de relecture du code par les pairs avant intégration. Pull requests obligatoires.

**Definition of Done**
: Critères de complétude pour une user story : code, tests, documentation, déploiement.

**Feature Branch**
: Stratégie de branching Git avec une branche par fonctionnalité avant merge.

**Test-Driven Development (TDD)**
: Approche où les tests sont écrits avant le code de production.

**Trunk-based Development**
: Stratégie d'intégration continue avec commits fréquents sur la branche principale.

### 12.6.2. Opérations

**Incident Management**
: Processus structuré de gestion des incidents avec classification par sévérité.

**Post-mortem**
: Analyse après incident pour identifier les causes et actions d'amélioration.

**Root Cause Analysis (RCA)**
: Méthode d'analyse pour identifier la cause fondamentale d'un problème.

**Service Level Management**
: Gestion des niveaux de service avec SLA, SLO et SLI clairement définis.

## 12.7. Acronymes

| Acronyme | Signification |
|----------|---------------|
| **ADR** | Architecture Decision Record |
| **API** | Application Programming Interface |
| **CRUD** | Create, Read, Update, Delete |
| **DTO** | Data Transfer Object |
| **GDPR** | General Data Protection Regulation |
| **HTTP** | HyperText Transfer Protocol |
| **HTTPS** | HTTP Secure |
| **JSON** | JavaScript Object Notation |
| **LDAP** | Lightweight Directory Access Protocol |
| **MVP** | Minimum Viable Product |
| **REST** | Representational State Transfer |
| **SOAP** | Simple Object Access Protocol |
| **SQL** | Structured Query Language |
| **SSL/TLS** | Secure Sockets Layer/Transport Layer Security |
| **UUID** | Universally Unique Identifier |
| **XML** | eXtensible Markup Language |
| **YAML** | YAML Ain't Markup Language |

---

*Ce glossaire est maintenu à jour avec l'évolution du projet et des technologies utilisées. Les définitions reflètent l'usage spécifique dans le contexte de cette architecture microservices.*
