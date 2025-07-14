# ADR 006 – Monitoring et Observabilité

## Status

Accepted (Évolué vers microservices - voir ADR 007)

## Context

L'application de gestion de magasin, maintenant distribuée en microservices, nécessite un monitoring en temps réel pour :

- Surveiller les performances de chaque microservice individuellement
- Détecter les problèmes de communication inter-services
- Monitorer la santé de l'infrastructure distribuée (API Gateway, Discovery Server)
- Analyser l'utilisation et optimiser les performances de l'architecture microservices
- Assurer la traçabilité des opérations critiques distribuées (ventes, modifications de stock)
- Surveiller les métriques des Golden Signals (latence, traffic, erreurs, saturation)

## Decision

Implémentation d'une solution de **monitoring et observabilité** complète pour l'architecture microservices :

### Métriques avec Prometheus

- **Spring Boot Actuator** pour exposer les métriques de chaque service
- **Micrometer** comme facade de métriques
- **Endpoint Prometheus** (`/actuator/prometheus`) sur tous les microservices
- **Surveillance centralisée** de tous les services :
  - `discovery-server` (port 8761)
  - `api-gateway` (port 8765)
  - `frontend-service` (port 8080)
  - `inventory-service` (port 8081)
  - `transaction-service` (port 8082)
  - `store-service` (port 8083)
  - `personnel-service` (port 8084)
- Métriques custom pour le business (ventes, stock)

### Visualisation avec Grafana

- **Grafana** pour la visualisation des métriques
- **Dashboard Golden Signals** pour surveiller les 4 signaux critiques :
  - **Latence** : Temps de réponse des services
  - **Traffic** : Volume de requêtes
  - **Erreurs** : Taux d'erreur des services
  - **Saturation** : Utilisation des ressources
- **Alertes configurables** via `alert_rules.yml`

### Logging structuré

- **SLF4J + Logback** pour la journalisation de chaque service
- **Logs distribués** avec corrélation entre services
- **API d'accès aux logs** via l'API Gateway
- Niveaux de log configurables par environnement

### Architecture de monitoring

- **Prometheus** pour la collecte de métriques (port 9090)
- **Grafana** pour la visualisation (port 3000)
- **Configuration centralisée** via `prometheus.yml`
- **Intégration Docker Compose** pour déploiement simplifié
- **Health checks** pour tous les services

## Justification

Le monitoring est essentiel pour une architecture microservices distribuée :

- **Observabilité distribuée** : visibilité sur l'état de chaque service
- **Détection proactive** : identification des problèmes avant qu'ils se propagent
- **Performance** : identification des goulots d'étranglement inter-services
- **Business Intelligence** : métriques métier agrégées depuis plusieurs services
- **Debugging distribué** : traçabilité complète des opérations cross-services
- **SLA** : mesure de la disponibilité et performance de l'architecture
- **Scalabilité** : métriques pour les décisions d'auto-scaling

## Consequences

- **Avantages :**
  - Visibilité complète sur l'état de l'architecture microservices
  - Détection rapide des anomalies dans les services distribués
  - Données pour l'optimisation des performances inter-services
  - Traçabilité des opérations critiques distribuées
  - Facilite le debugging dans l'environnement microservices
  - Dashboard centralisé pour tous les services
  - Alertes proactives sur les métriques critiques

- **Inconvénients :**
  - Overhead minimal sur les performances de chaque service
  - Complexité additionnelle de l'infrastructure distribuée
  - Stockage distribué des métriques et logs
  - Gestion des corrélations entre services

## Métriques surveillées

### Métriques techniques par service

- **Système** : CPU, mémoire, I/O disque
- **JVM** : Heap, GC, threads
- **HTTP** : Latence, codes de réponse, throughput
- **Base de données** : Pool de connexions, temps de requête

### Métriques business agrégées

- **Ventes** : Nombre de transactions, montant total
- **Inventaire** : Niveaux de stock, ruptures
- **Personnel** : Activité par employé
- **Magasins** : Performance par localisation

### Golden Signals

- **Latency** : Temps de réponse des services (P50, P95, P99)
- **Traffic** : Volume de requêtes par service
- **Errors** : Taux d'erreur (4xx, 5xx)
- **Saturation** : Utilisation des ressources (CPU, mémoire, DB)

## Configuration

```yaml
# Activation des endpoints Actuator
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
  prometheus:
    metrics:
      export:
        enabled: true
```

## Options envisagées

- Alternative 1 : Logging simple uniquement
- Alternative 2 : Solution complète ELK Stack (trop complexe)
- Alternative 3 : Monitoring cloud (AWS CloudWatch, etc.)
