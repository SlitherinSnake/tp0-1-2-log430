# Documentation arc42 - Architecture Microservices

## Table des matières

Cette documentation arc42 décrit l'architecture microservices du système de gestion de magasin, résultant de la migration d'une application monolithique Spring Boot vers une architecture distribuée moderne pour le Laboratoire 5.

### Structure de la documentation

1. **Introduction et objectifs** - Vue d'ensemble du système et parties prenantes
2. **Contraintes d'architecture** - Limitations techniques et organisationnelles  
3. **Contexte et portée** - Frontières du système et interfaces
4. **Stratégie de solution** - Décisions technologiques clés
5. **Vue des blocs de construction** - Architecture des microservices
6. **Vue d'exécution** - Scénarios d'utilisation et flux de données
7. **Vue de déploiement** - Infrastructure Docker et déploiement
8. **Concepts transversaux** - Patterns, sécurité, monitoring
9. **Décisions d'architecture** - Justifications et alternatives
10. **Exigences de qualité** - Performance, fiabilité, sécurité
11. **Risques et dette technique** - Analyse des risques et mitigation
12. **Glossaire** - Définitions complètes des termes

---

# 1. Introduction et Objectifs

Ce document décrit l'architecture microservices du système de gestion de magasin, évolution majeure du projet initial réalisé dans le cadre du cours LOG430. Le système a migré d'une architecture monolithique vers une architecture distribuée moderne avec conteneurisation et monitoring.

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

Décomposition en microservices indépendants :

- **Services métier** : Séparation par domaine (inventaire, transactions, magasins, personnel)
- **Services d'infrastructure** : Discovery server, API Gateway, Frontend
- **Containerisation** : Docker et Docker Compose
- **Monitoring** : Prometheus et Grafana
- **Bases de données** : Une base PostgreSQL par service

## 1.2. Objectifs de qualité

1. **Scalabilité** : Chaque service peut être scalé indépendamment selon la charge
2. **Résilience** : Tolérance aux pannes avec circuit breakers et fallbacks
3. **Maintenabilité** : Services découplés facilitant l'évolution et la maintenance
4. **Observabilité** : Monitoring et métriques complètes avec Prometheus/Grafana
5. **Portabilité** : Déploiement conteneurisé sur n'importe quel environnement

## 1.3. Parties prenantes

| Rôle                  | Nom               | Intérêt                                                              |
|-----------------------|-------------------|----------------------------------------------------------------------|
| Étudiant              | Vu Minh Vu-Le     | Conçoit, implémente et documente l'architecture microservices       |
| Enseignant            | Fabio Petrillo    | Évalue la migration vers les microservices et l'architecture         |
| Chargé de laboratoire | Hakim Ghlissi     | Fournit un encadrement technique sur les technologies cloud         |

## 1.4. Vue d'ensemble de l'architecture

Le système est composé de 7 microservices déployés avec Docker Compose :

### Services métier

- **inventory-service** (8081) : Gestion des produits et du stock
- **transaction-service** (8082) : Gestion des ventes et retours  
- **store-service** (8083) : Gestion des magasins
- **personnel-service** (8084) : Gestion des employés

### Services d'infrastructure

- **discovery-server** (8761) : Service de découverte Eureka
- **api-gateway** (8765) : Routage et authentification
- **frontend-service** (8080) : Interface web utilisateur

### Services externes

- **PostgreSQL** : Base de données avec schémas séparés par service
- **Redis** : Cache distribué
- **Prometheus** (9090) : Collecte de métriques
- **Grafana** (3000) : Visualisation et monitoring

---

# 2. Contraintes architecturales

## 2.1. Contraintes techniques

- Le système doit être développé en **Java** avec **Spring Boot**
- Architecture **microservices** avec communication REST
- Chaque service doit avoir sa **propre base de données PostgreSQL**
- **Conteneurisation** obligatoire avec Docker
- **Service discovery** avec Netflix Eureka
- **API Gateway** centralisé pour le routage
- **Monitoring** avec Prometheus et Grafana

## 2.2. Contraintes opérationnelles

- Déploiement avec **Docker Compose** pour l'environnement local
- Configuration centralisée via variables d'environnement
- **Health checks** pour tous les services
- **Logging** structuré et centralisé

## 2.3. Contraintes de qualité

- **Haute disponibilité** : Services résilients aux pannes
- **Performance** : Réponse < 2 secondes pour les opérations CRUD
- **Sécurité** : Authentification via API Gateway
- **Observabilité** : Métriques complètes et tableaux de bord

---

# 3. Contexte du système

## 3.1. Contexte métier

Le système de gestion de magasin permet de :

- **Gestion des produits** : Création, modification, consultation du catalogue
- **Gestion des stocks** : Suivi des quantités, alertes de rupture
- **Gestion des ventes** : Enregistrement des transactions, calcul des totaux
- **Gestion des retours** : Traitement des retours client avec motifs
- **Gestion des magasins** : Multiple points de vente avec géolocalisation
- **Gestion du personnel** : Employés avec rôles et permissions
- **Rapports** : Analyses des ventes, performance des magasins

## 3.2. Contexte technique

### Architecture globale

```
[Frontend Web] → [API Gateway] → [Microservices] → [PostgreSQL]
                      ↓
              [Service Discovery]
                      ↓
              [Monitoring Stack]
```

### Interfaces externes

- **Interface utilisateur** : Application web Thymeleaf
- **API REST** : Points d'entrée pour intégrations externes
- **Base de données** : PostgreSQL avec schémas séparés
- **Cache** : Redis pour la performance
- **Monitoring** : Prometheus pour les métriques, Grafana pour la visualisation

---

# 4. Stratégie de solution

## 4.1. Décomposition en microservices

La décomposition suit les principes du **Domain-Driven Design (DDD)** :

### Bounded Contexts identifiés

1. **Inventory** : Gestion des produits et du stock
2. **Transaction** : Ventes, achats et retours
3. **Store** : Magasins et géolocalisation
4. **Personnel** : Employés et authentification

### Services d'infrastructure

1. **API Gateway** : Point d'entrée unique, routage, sécurité
2. **Service Discovery** : Localisation dynamique des services
3. **Frontend** : Interface utilisateur découplée

## 4.2. Patterns architecturaux

### Database per Service

- Chaque microservice possède sa propre base de données
- Isolation des données et autonomie des équipes
- Schémas PostgreSQL séparés : `inventory_db`, `transaction_db`, `store_db`, `personnel_db`

### API Gateway Pattern

- Point d'entrée unique pour tous les clients
- Routage intelligent vers les microservices
- Authentification et autorisation centralisées
- Rate limiting et circuit breaking

### Service Discovery

- Enregistrement automatique des services avec Eureka
- Load balancing côté client
- Health checks et failover automatique

### Circuit Breaker

- Protection contre les pannes en cascade
- Fallback et réponses dégradées
- Resilience4j pour la tolérance aux pannes

## 4.3. Technologies choisies

| Composant | Technologie | Justification |
|-----------|-------------|---------------|
| Framework | Spring Boot 3.5 | Écosystème mature, auto-configuration |
| Service Discovery | Netflix Eureka | Intégration native Spring Cloud |
| API Gateway | Spring Cloud Gateway | Performance réactive, filtres |
| Base de données | PostgreSQL 14 | ACID, performances, JSON support |
| Cache | Redis 7 | Performance, session partagée |
| Conteneurisation | Docker + Docker Compose | Portabilité, isolation |
| Monitoring | Prometheus + Grafana | Standard industrie, métriques riches |

---

# 5. Vue des blocs de construction

## 5.1. Vue d'ensemble de l'architecture

### Diagramme de l'architecture microservices

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Web Browser   │    │   Mobile App    │    │  External API   │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                    ┌─────────────┴──────────────┐
                    │      API Gateway           │
                    │      (port 8765)           │
                    └─────────────┬──────────────┘
                                  │
        ┌─────────────────────────┼─────────────────────────┐
        │                         │                         │
┌───────┴──────┐       ┌─────────┴────────┐       ┌────────┴─────────┐
│ Frontend      │       │ Discovery Server │       │ Monitoring Stack │
│ Service       │       │ (Eureka)         │       │ Prometheus       │
│ (port 8080)   │       │ (port 8761)      │       │ Grafana          │
└───────────────┘       └──────────────────┘       └──────────────────┘
        │
        │            Business Microservices
        │
        ├─────────┬─────────┬─────────┬─────────┐
        │         │         │         │         │
┌───────┴──┐ ┌────┴───┐ ┌───┴────┐ ┌──┴──────┐ │
│Inventory │ │Transaction│ │ Store  │ │Personnel│ │
│Service   │ │ Service  │ │Service │ │ Service │ │
│(8081)    │ │ (8082)   │ │ (8083) │ │ (8084)  │ │
└───────┬──┘ └────┬───┘ └───┬────┘ └──┬──────┘ │
        │         │         │         │        │
┌───────┴──┐ ┌────┴───┐ ┌───┴────┐ ┌──┴──────┐ │
│inventory │ │transaction│ │store  │ │personnel│ │
│_db       │ │_db      │ │_db     │ │_db      │ │
└──────────┘ └────────┘ └────────┘ └─────────┘ │
                                                │
┌─────────────────────────────────────────────┬─┘
│                PostgreSQL                   │
│              (port 5432)                    │
└─────────────────────────────────────────────┘
```

## 5.2. Services métier (Niveau 2)

### 5.2.1. Inventory Service

**Responsabilités :**

- Gestion du catalogue de produits
- Suivi des stocks centraux
- Catégorisation des articles
- Alertes de rupture de stock

**APIs principales :**

- `GET /api/inventory/items` : Liste des produits actifs
- `POST /api/inventory/items` : Création de produit
- `PUT /api/inventory/items/{id}/stock` : Mise à jour du stock
- `GET /api/inventory/items/restock` : Articles nécessitant un réapprovisionnement

### 5.2.2. Transaction Service

**Responsabilités :**

- Enregistrement des ventes
- Gestion des retours
- Calcul des totaux et taxes
- Historique des transactions

**APIs principales :**

- `POST /api/transactions/sales` : Création d'une vente
- `POST /api/transactions/returns` : Traitement d'un retour
- `GET /api/transactions/personnel/{id}` : Transactions d'un employé
- `GET /api/transactions/store/{id}/sales` : Ventes d'un magasin

### 5.2.3. Store Service

**Responsabilités :**

- Gestion des magasins
- Géolocalisation des points de vente
- Configuration des horaires
- États actif/inactif

**APIs principales :**

- `GET /api/stores` : Liste des magasins actifs
- `POST /api/stores` : Création d'un magasin
- `GET /api/stores/quartier/{quartier}` : Magasins par quartier
- `PUT /api/stores/{id}/status` : Activation/désactivation

### 5.2.4. Personnel Service

**Responsabilités :**

- Gestion des employés
- Authentification et autorisation
- Rôles et permissions
- Affectation aux magasins

**APIs principales :**

- `GET /api/personnel` : Liste du personnel actif
- `POST /api/personnel` : Création d'un employé
- `POST /api/personnel/authenticate` : Authentification
- `GET /api/personnel/store/{id}` : Personnel d'un magasin

## 5.3. Services d'infrastructure (Niveau 2)

### 5.3.1. API Gateway

**Responsabilités :**

- Routage des requêtes vers les microservices
- Authentification et autorisation
- Rate limiting et throttling
- Circuit breaking et retry
- Transformation des réponses

**Configuration des routes :**

```yaml
routes:
  - id: inventory-service
    uri: lb://inventory-service
    predicates:
      - Path=/api/inventory/**
  - id: transaction-service
    uri: lb://transaction-service
    predicates:
      - Path=/api/transactions/**
```

### 5.3.2. Discovery Server (Eureka)

**Responsabilités :**

- Enregistrement des services
- Health checking
- Load balancing côté client
- Failover automatique

### 5.3.3. Frontend Service

**Responsabilités :**

- Interface utilisateur web (Thymeleaf)
- Agrégation des données des microservices
- Session management
- Cache côté client

---

# 6. Vue d'exécution

## 6.1. Scénario : Recherche de produit

### Description

Un utilisateur recherche un produit via l'interface web. La requête transite par l'API Gateway, est routée vers le service d'inventaire, qui interroge sa base de données dédiée.

### Diagramme de séquence

```
Client → Frontend → API Gateway → Inventory Service → PostgreSQL
  |         |           |              |                |
  |---------|-----------|--------------|----------------|
  | GET /products?search=nom           |                |
  |         |           |              |                |
  |         | WebClient |              |                |
  |         |---------->|              |                |
  |         |           | Route to     |                |
  |         |           | inventory-   |                |
  |         |           | service      |                |
  |         |           |------------->|                |
  |         |           |              | JPA Query      |
  |         |           |              |--------------->|
  |         |           |              |<---------------|
  |         |           |<-------------|                |
  |         |<----------|              |                |
  |<--------|           |              |                |
```

### Points clés

1. **Discovery** : API Gateway utilise Eureka pour localiser inventory-service
2. **Load Balancing** : Si plusieurs instances, load balancing automatique
3. **Circuit Breaker** : Protection contre les timeouts du service d'inventaire
4. **Cache** : Mise en cache des résultats fréquents dans Redis

## 6.2. Scénario : Enregistrement d'une vente

### Description

Processus complexe impliquant plusieurs microservices : vérification du stock (inventory), création de la transaction (transaction), validation du personnel (personnel).

### Diagramme de séquence

```
Client → Frontend → API Gateway → Transaction Service → Inventory Service
  |         |           |              |                     |
  |---------|-----------|--------------|---------------------|
  | POST /cart/checkout |              |                     |
  |         |           |              |                     |
  |         | Aggregate |              |                     |
  |         | Cart Data |              |                     |
  |         |---------->|              |                     |
  |         |           | Create Sale  |                     |
  |         |           |------------->|                     |
  |         |           |              | Verify Stock        |
  |         |           |              |-------------------->|
  |         |           |              |<--------------------|
  |         |           |              | Update Stock        |
  |         |           |              |-------------------->|
  |         |           |              | Save Transaction    |
  |         |           |              |---->PostgreSQL      |
  |         |           |<-------------|                     |
  |         |<----------|              |                     |
  |<--------|           |              |                     |
```

### Gestion des erreurs

1. **Stock insuffisant** : Transaction annulée, message d'erreur
2. **Service indisponible** : Circuit breaker activé, fallback response
3. **Timeout** : Retry automatique avec backoff exponentiel

## 6.3. Scénario : Consultation des rapports consolidés

### Description

Agrégation de données provenant de plusieurs microservices pour générer un rapport consolidé des ventes.

### Orchestration par le Frontend

```
Frontend Service (Orchestrator)
    |
    ├─→ Transaction Service (ventes du mois)
    ├─→ Inventory Service (stock actuel)
    ├─→ Store Service (liste des magasins)
    └─→ Personnel Service (vendeurs actifs)
    |
    └─→ Agrégation et présentation
```

---

# 7. Vue de déploiement

## 7.1. Infrastructure Docker

### 7.1.1. Architecture de conteneurs

```
┌─────────────────────────────────────────────────────────────┐
│                    Docker Host                              │
│                                                             │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐        │
│  │ Discovery    │ │ API Gateway  │ │ Frontend     │        │
│  │ Server       │ │              │ │ Service      │        │
│  │ :8761        │ │ :8765        │ │ :8080        │        │
│  └──────────────┘ └──────────────┘ └──────────────┘        │
│                                                             │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐        │
│  │ Inventory    │ │ Transaction  │ │ Store        │        │
│  │ Service      │ │ Service      │ │ Service      │        │
│  │ :8081        │ │ :8082        │ │ :8083        │        │
│  └──────────────┘ └──────────────┘ └──────────────┘        │
│                                                             │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐        │
│  │ Personnel    │ │ PostgreSQL   │ │ Redis        │        │
│  │ Service      │ │              │ │              │        │
│  │ :8084        │ │ :5432        │ │ :6379        │        │
│  └──────────────┘ └──────────────┘ └──────────────┘        │
│                                                             │
│  ┌──────────────┐ ┌──────────────┐                         │
│  │ Prometheus   │ │ Grafana      │                         │
│  │ :9090        │ │ :3000        │                         │
│  │              │ │              │                         │
│  └──────────────┘ └──────────────┘                         │
└─────────────────────────────────────────────────────────────┘
```

### 7.1.2. Configuration Docker Compose

```yaml
version: '3.8'
services:
  # Service Discovery
  discovery-server:
    build: ./discovery-server
    ports: ["8761:8761"]
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8761/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  # API Gateway
  api-gateway:
    build: ./api-gateway
    ports: ["8765:8765"]
    depends_on:
      - discovery-server
      - redis
      - postgres
    environment:
      - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://discovery-server:8761/eureka/
      - SPRING_DATA_REDIS_HOST=redis
      - POSTGRES_URL=jdbc:postgresql://postgres:5432/gateway_db

  # Business Services
  inventory-service:
    build: ./inventory-service
    ports: ["8081:8081"]
    depends_on: [discovery-server, postgres]
    environment:
      - POSTGRES_URL=jdbc:postgresql://postgres:5432/inventory_db

  # Infrastructure Services
  postgres:
    image: postgres:14
    environment:
      POSTGRES_DB: magasin_db
      POSTGRES_USER: magasin
      POSTGRES_PASSWORD: password
    volumes:
      - ./init-db.sql:/docker-entrypoint-initdb.d/init-db.sql

  # Monitoring
  prometheus:
    image: prom/prometheus:latest
    ports: ["9090:9090"]
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
```

## 7.2. Séquence de démarrage

### 7.2.1. Ordre de démarrage

1. **Infrastructure externe** : PostgreSQL, Redis
2. **Service Discovery** : Eureka Server
3. **Services métier** : Inventory, Transaction, Store, Personnel
4. **Services d'infrastructure** : API Gateway, Frontend
5. **Monitoring** : Prometheus, Grafana

### 7.2.2. Health Checks

Chaque service expose un endpoint `/actuator/health` vérifiant :

- Connectivité à la base de données
- Enregistrement auprès d'Eureka
- État des dépendances critiques

## 7.3. Configuration réseau

### 7.3.1. Réseaux Docker

- **Réseau interne** : Communication inter-services
- **Exposition externe** : Ports mappés pour l'accès client

### 7.3.2. Variables d'environnement

Configuration centralisée via Docker Compose :

```yaml
environment:
  - SPRING_PROFILES_ACTIVE=docker
  - POSTGRES_URL=jdbc:postgresql://postgres:5432/service_db
  - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://discovery-server:8761/eureka/
  - MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics,prometheus
```

---

# 8. Concepts transversaux

## 8.1. Communication inter-services

### 8.1.1. Protocoles

- **REST/HTTP** : Communication synchrone via API Gateway
- **WebClient** : Client réactif pour les appels entre services
- **Circuit Breaker** : Resilience4j pour la tolérance aux pannes

### 8.1.2. Format d'échange

- **JSON** : Format standard pour les APIs REST
- **DTOs** : Objets de transfert pour découpler les APIs internes
- **OpenAPI** : Documentation automatique des APIs

## 8.2. Gestion des données

### 8.2.1. Database per Service

- **Isolation** : Chaque service possède son schéma PostgreSQL
- **Autonomie** : Évolution indépendante des modèles de données
- **Cohérence** : Transactions locales au service

### 8.2.2. Cache distribué

- **Redis** : Cache partagé pour les sessions et données fréquentes
- **Spring Cache** : Annotations pour la mise en cache automatique
- **TTL** : Expiration automatique des données cached

## 8.3. Observabilité

### 8.3.1. Métriques (Prometheus)

```yaml
# Métriques collectées par service
- http_requests_total : Nombre de requêtes HTTP
- http_request_duration_seconds : Durée des requêtes
- db_connections_active : Connexions DB actives
- circuit_breaker_calls_total : Appels circuit breaker
```

### 8.3.2. Tableaux de bord (Grafana)

- **Golden Signals** : Latency, Traffic, Errors, Saturation
- **Business Metrics** : Ventes, stock, performance par magasin
- **Infrastructure** : CPU, mémoire, disque, réseau

### 8.3.3. Logging

```yaml
# Configuration commune
logging:
  level:
    com.log430.tp5: INFO
    root: WARN
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
```

## 8.4. Sécurité

### 8.4.1. Authentification

- **API Gateway** : Point d'entrée sécurisé
- **API Keys** : Authentification des services internes
- **CORS** : Configuration pour les accès web

### 8.4.2. Autorisation

- **Personnel Service** : Gestion des rôles et permissions
- **Filtres Gateway** : Validation des tokens
- **Resource access** : Contrôle d'accès par endpoint

## 8.5. Configuration

### 8.5.1. Profils Spring

- **local** : Développement local
- **docker** : Conteneurisation
- **prod** : Production (future)

### 8.5.2. Configuration externalisée

```yaml
# application-docker.yml
spring:
  datasource:
    url: ${POSTGRES_URL}
    username: ${POSTGRES_USER}
    password: ${POSTGRES_PASSWORD}
eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE}
```

---

# 9. Décisions d'architecture

Cette section référence les principales décisions architecturales prises lors de la migration vers les microservices. Chaque décision est documentée sous forme d'ADR (Architecture Decision Record).

## 9.1. Liste des ADR

| ID      | Titre                                              | Statut   | Description                                                       |
|---------|---------------------------------------------------|----------|-------------------------------------------------------------------|
| ADR-001 | Choix de la plateforme                            | Accepted | Justification du choix de Java/Spring Boot                       |
| ADR-002 | Architecture Spring Boot (Lab 2)                  | Superseded | Migration du CLI vers l'architecture web                        |
| ADR-003 | Migration vers PostgreSQL                         | Accepted | Remplacement de SQLite par PostgreSQL                           |
| ADR-004 | Architecture Domain-Driven Design                 | Accepted | Structuration en domaines métier                                 |
| ADR-005 | Interface web et APIs                             | Accepted | Exposition d'APIs REST et interface Thymeleaf                   |
| ADR-006 | Monitoring et observabilité                       | Accepted | Intégration de Prometheus et Grafana                            |
| ADR-007 | **Migration vers l'architecture microservices**   | Accepted | Décomposition du monolithe en microservices                     |
| ADR-008 | Infrastructure et conteneurisation                | Accepted | Docker, Docker Compose et orchestration                         |

## 9.2. ADR-007 : Migration vers les microservices (Clé)

### Contexte

L'application monolithique Spring Boot présentait des limitations :

- Couplage fort entre domaines métier
- Déploiement monolithique
- Scalabilité limitée
- Base de données partagée

### Décision

Migration vers une architecture microservices avec :

- **4 services métier** : inventory, transaction, store, personnel
- **3 services infrastructure** : discovery, gateway, frontend
- **Database per service** : schémas PostgreSQL séparés
- **Conteneurisation** : Docker et Docker Compose

### Conséquences positives

- ✅ Scalabilité indépendante des services
- ✅ Déploiement granulaire
- ✅ Isolation des données
- ✅ Équipes autonomes
- ✅ Résilience améliorée

### Conséquences négatives

- ❌ Complexité opérationnelle accrue
- ❌ Latence réseau entre services
- ❌ Gestion des transactions distribuées
- ❌ Courbe d'apprentissage

---

# 10. Exigences de qualité

## 10.1. Arbre de qualité

```
Qualité
├── Performance
│   ├── Latency : < 500ms pour 95% des requêtes
│   ├── Throughput : > 1000 requêtes/seconde
│   └── Scalabilité : Scaling horizontal par service
├── Disponibilité
│   ├── Uptime : 99.9% de disponibilité
│   ├── Circuit Breaker : Protection contre les pannes
│   └── Health Checks : Monitoring continu
├── Maintenabilité
│   ├── Modularité : Services faiblement couplés
│   ├── Testabilité : Tests unitaires et d'intégration
│   └── Documentation : APIs OpenAPI
├── Observabilité
│   ├── Monitoring : Métriques Prometheus
│   ├── Logging : Logs structurés centralisés
│   └── Tracing : Corrélation des requêtes
└── Sécurité
    ├── Authentification : API Gateway
    ├── Autorisation : Contrôle d'accès RBAC
    └── Chiffrement : HTTPS/TLS
```

## 10.2. Scénarios de qualité

### 10.2.1. Performance

**Scénario** : Lors d'un pic de trafic (Black Friday), le système doit maintenir un temps de réponse < 2 secondes pour 95% des requêtes de consultation produits.

**Mesure** :

- Métriques Prometheus : `http_request_duration_seconds`
- Alertes Grafana si P95 > 2s
- Scaling automatique des services critiques

### 10.2.2. Disponibilité

**Scénario** : En cas de panne du service d'inventaire, les autres fonctionnalités (ventes en cours, rapports) restent accessibles.

**Mesure** :

- Circuit breaker sur inventory-service
- Fallback : affichage des données cached
- Health checks et restart automatique

### 10.2.3. Scalabilité

**Scénario** : Pendant les heures de pointe, seul le service de transactions nécessite une montée en charge.

**Mesure** :

- Scaling horizontal d'inventory-service uniquement
- Load balancing automatique via Eureka
- Métriques de charge par service

### 10.2.4. Maintenabilité

**Scénario** : Ajout d'une nouvelle fonctionnalité de fidélité client sans impacter les services existants.

**Mesure** :

- Nouveau microservice `loyalty-service`
- APIs découplées avec versioning
- Tests d'intégration automatisés

---

# 11. Risques et dette technique

## 11.1. Risques identifiés

### 11.1.1. Risques techniques

| Risque | Probabilité | Impact | Mitigation |
|--------|-------------|--------|------------|
| **Panne en cascade** | Moyenne | Élevé | Circuit breakers, timeouts, fallbacks |
| **Latence réseau** | Élevée | Moyen | Cache distribué, optimisation requêtes |
| **Consistency** données | Moyenne | Élevé | Eventual consistency, saga pattern |
| **Service discovery failure** | Faible | Élevé | Eureka cluster, fallback DNS |

### 11.1.2. Risques opérationnels

| Risque | Probabilité | Impact | Mitigation |
|--------|-------------|--------|------------|
| **Complexité déploiement** | Élevée | Moyen | Docker Compose, scripts automatisation |
| **Monitoring insuffisant** | Moyenne | Élevé | Métriques complètes, alertes proactives |
| **Gestion des logs** | Moyenne | Moyen | Logging centralisé, structured logs |

## 11.2. Dette technique

### 11.2.1. Dette actuelle

1. **Tests d'intégration** : Couverture insuffisante des scénarios inter-services
2. **Gestion des transactions** : Pas de saga pattern pour les transactions distribuées
3. **Sécurité** : Authentification basique, pas de JWT/OAuth2
4. **Documentation** : APIs pas toutes documentées avec OpenAPI

### 11.2.2. Plan de réduction

1. **Phase 1** (court terme) :
   - Implémentation des tests d'intégration contract-based
   - Documentation OpenAPI pour tous les services
   - Métriques business dans Grafana

2. **Phase 2** (moyen terme) :
   - Migration vers JWT/OAuth2
   - Implémentation saga pattern
   - Load testing et optimization

3. **Phase 3** (long terme) :
   - Migration vers Kubernetes
   - Service mesh (Istio)
   - Observability distribuée (Jaeger)

---

# 12. Glossaire

| Terme | Définition |
|-------|------------|
| **API Gateway** | Point d'entrée unique qui route les requêtes vers les microservices appropriés |
| **Circuit Breaker** | Pattern qui empêche les appels vers un service défaillant |
| **Database per Service** | Pattern où chaque microservice possède sa propre base de données |
| **Discovery Service** | Service qui maintient un registre des microservices disponibles |
| **Eureka** | Serveur de découverte de services développé par Netflix |
| **Eventual Consistency** | Modèle de cohérence où les données deviennent cohérentes après un délai |
| **Fallback** | Mécanisme de réponse dégradée en cas de panne d'un service |
| **Load Balancer** | Composant qui distribue les requêtes entre plusieurs instances |
| **Microservice** | Architecture où l'application est découpée en services petits et autonomes |
| **Resilience4j** | Bibliothèque Java pour implémenter la tolérance aux pannes |
| **Saga Pattern** | Pattern pour gérer les transactions distribuées |
| **Service Mesh** | Infrastructure dédiée pour gérer la communication inter-services |
| **Thymeleaf** | Moteur de templates Java pour les applications web |

---

# 13. Références

## 13.1. Documentation technique

- [Arc42 Template](https://arc42.org) - Structure de documentation d'architecture
- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud) - Écosystème microservices Spring
- [Docker Compose Reference](https://docs.docker.com/compose/) - Orchestration de conteneurs
- [Prometheus Documentation](https://prometheus.io/docs/) - Monitoring et métriques

## 13.2. Patterns et bonnes pratiques

- [Microservices Patterns](https://microservices.io/patterns/) - Chris Richardson
- [Building Microservices](https://www.oreilly.com/library/view/building-microservices/9781491950340/) - Sam Newman
- [Circuit Breaker Pattern](https://martinfowler.com/bliki/CircuitBreaker.html) - Martin Fowler

## 13.3. Technologies utilisées

- [Spring Boot 3.5](https://spring.io/projects/spring-boot) - Framework de développement
- [Netflix Eureka](https://github.com/Netflix/eureka) - Service Discovery
- [PostgreSQL 14](https://www.postgresql.org/docs/14/) - Base de données relationnelle
- [Redis 7](https://redis.io/docs/) - Cache en mémoire
- [Grafana](https://grafana.com/docs/) - Visualisation de métriques

## 13.4. ADRs du projet

- `docs/ADR-Lab5/007-architecture-microservices.md` - Décision de migration
- `docs/ADR-Lab5/008-infrastructure-containerisation.md` - Choix de conteneurisation
- `docs/ADR-Lab5/006-monitoring-observabilite.md` - Stratégie de monitoring

---

**Document version** : 1.0  
**Date de création** : Laboratoire 5 - 2024  
**Auteur** : Vu Minh Vu-Le  
**Status** : En cours de développement
