# ADR 007 – Migration vers l'architecture Microservices

## Status

Accepted

## Context

L'application de gestion de magasin était initialement conçue comme une application monolithique Spring Boot avec une architecture DDD. Cependant, avec la croissance des fonctionnalités et la nécessité de développer en équipe, l'architecture monolithique présente des limitations :

- **Couplage fort** entre les domaines métier
- **Déploiement monolithique** : une modification mineure nécessite un redéploiement complet
- **Scalabilité limitée** : impossible de scaler indépendamment chaque domaine
- **Technologie unique** : tous les services doivent utiliser la même stack technologique
- **Base de données partagée** : risque de conflits et de couplage de données
- **Développement en équipe** : difficile de travailler en parallèle sur différents domaines

## Decision

Migration vers une **architecture microservices** basée sur les domaines DDD identifiés :

### Services métier

- **inventory-service** (port 8081) : Gestion des produits et du stock
- **transaction-service** (port 8082) : Gestion des ventes et retours
- **store-service** (port 8083) : Gestion des magasins et localisations
- **personnel-service** (port 8084) : Gestion des employés et rôles

### Services d'infrastructure

- **discovery-server** (port 8761) : Service de découverte avec Netflix Eureka
- **api-gateway** (port 8765) : Routage, authentification et limitation de débit
- **frontend-service** (port 8080) : Interface utilisateur web

### Technologies utilisées

- **Spring Boot** pour tous les microservices
- **Spring Cloud Netflix Eureka** pour la découverte de services
- **Spring Cloud Gateway** pour l'API Gateway
- **PostgreSQL** avec bases de données séparées par service
- **Redis** pour le cache distribué
- **Docker & Docker Compose** pour le déploiement

### Architecture de données

Chaque service possède sa propre base de données :

```text
├── gateway_db      # API Gateway
├── inventory_db    # Service inventaire
├── transaction_db  # Service transactions
├── store_db        # Service magasins
└── personnel_db    # Service personnel
```

### Communication inter-services

- **Communication synchrone** : REST API via l'API Gateway
- **Circuit breaker** : Resilience4j pour la tolérance aux pannes
- **Load balancing** : Spring Cloud LoadBalancer
- **Service discovery** : Eureka pour la localisation des services

## Justification

L'architecture microservices apporte plusieurs avantages pour notre contexte :

### Avantages techniques

- **Séparation des responsabilités** : chaque service a un périmètre métier clair
- **Scalabilité indépendante** : possibilité de scaler chaque service selon ses besoins
- **Déploiement indépendant** : réduction des risques de déploiement
- **Isolation des pannes** : une panne dans un service n'affecte pas les autres
- **Flexibilité technologique** : possibilité d'utiliser des technologies différentes par service

### Avantages organisationnels

- **Équipes autonomes** : chaque équipe peut développer son service indépendamment
- **Développement parallèle** : réduction des conflits de code
- **Spécialisation** : les équipes peuvent se spécialiser sur un domaine métier
- **Time-to-market** : déploiements plus fréquents et plus rapides

## Consequences

### Avantages

- **Modularité** : architecture plus modulaire et maintenable
- **Scalabilité** : possibilité de scaler indépendamment chaque service
- **Résilience** : isolation des pannes et circuit breakers
- **Développement parallèle** : équipes peuvent travailler indépendamment
- **Déploiement continu** : déploiements plus fréquents et moins risqués
- **Monitoring granulaire** : observabilité fine de chaque service

### Inconvénients

- **Complexité opérationnelle** : gestion de plusieurs services
- **Latence réseau** : communication inter-services via HTTP
- **Transactions distribuées** : complexité de gestion des transactions ACID
- **Débogage complexe** : traçabilité des erreurs cross-services
- **Consistency** : gestion de la cohérence des données distribuées
- **Overhead** : infrastructure supplémentaire (discovery, gateway, monitoring)

### Risques et mitigations

- **Latence** : Optimisation des appels inter-services et mise en cache
- **Consistency** : Implémentation de patterns Saga si nécessaire
- **Monitoring** : Mise en place d'observabilité distribuée (Prometheus + Grafana)
- **Security** : Centralisation de l'authentification via l'API Gateway
- **Testing** : Tests d'intégration et contract testing

## Patterns implémentés

### Service Discovery

- **Eureka Server** : Registre centralisé des services
- **Client-side discovery** : Chaque service s'enregistre automatiquement

### API Gateway

- **Routing** : Routage des requêtes vers les services appropriés
- **Authentication** : Authentification centralisée
- **Rate limiting** : Limitation du débit des requêtes
- **CORS** : Gestion des requêtes cross-origin

### Database per Service

- **Isolation des données** : Chaque service possède sa propre base de données
- **Autonomie** : Pas de dépendance sur le schéma d'autres services

### Circuit Breaker

- **Resilience4j** : Protection contre les pannes en cascade
- **Fallback** : Réponses dégradées en cas de panne

## Migration

La migration s'est faite progressivement :

1. **Extraction des services** : Séparation des domaines DDD en services
2. **Infrastructure** : Mise en place d'Eureka et de l'API Gateway
3. **Séparation des données** : Migration vers des bases de données séparées
4. **Communication** : Refactoring des appels locaux en appels REST
5. **Monitoring** : Adaptation du monitoring pour l'architecture distribuée

## Options envisagées

- **Alternative 1** : Garder l'architecture monolithique (limitée pour la scalabilité)
- **Alternative 2** : Architecture modulaire (moins de séparation)
- **Alternative 3** : Architecture event-driven (complexité supplémentaire)
- **Alternative 4** : Architecture serverless (pas adaptée au contexte)

## Prochaines étapes

- **Event-driven architecture** : Implémentation d'événements asynchrones
- **Distributed tracing** : Traçabilité des requêtes cross-services
- **Service mesh** : Gestion avancée de la communication inter-services
- **Auto-scaling** : Mise à l'échelle automatique selon la charge
