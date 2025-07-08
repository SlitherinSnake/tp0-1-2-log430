# ADR 006 – Monitoring et Observabilité

## Status

Accepted

## Context

L'application de gestion de magasin nécessite un monitoring en temps réel pour :

- Surveiller les performances de l'application
- Détecter les problèmes avant qu'ils affectent les utilisateurs
- Analyser l'utilisation et optimiser les performances
- Assurer la traçabilité des opérations critiques (ventes, modifications de stock)

## Decision

Implémentation d'une solution de **monitoring et observabilité** complète :

### Métriques avec Prometheus

- **Spring Boot Actuator** pour exposer les métriques
- **Micrometer** comme facade de métriques
- **Endpoint Prometheus** (`/actuator/prometheus`)
- Métriques custom pour le business (ventes, stock)

### Logging structuré

- **SLF4J + Logback** pour la journalisation
- **Logs applicatifs** dans `/logs/spring.log`
- **API d'accès aux logs** via `/api/logs`
- Niveaux de log configurables par environnement

### Monitoring avec Docker

- **Prometheus** pour la collecte de métriques
- **Configuration** via `prometheus.yml`
- Intégration avec le docker-compose existant

## Justification

Le monitoring est essentiel pour une application de production :

- **Proactivité** : détection précoce des problèmes
- **Performance** : identification des goulots d'étranglement
- **Business Intelligence** : métriques métier pour la prise de décision
- **Debugging** : traçabilité complète des opérations
- **SLA** : mesure de la disponibilité et performance

## Consequences

- **Avantages :**
  - Visibilité complète sur l'état de l'application
  - Détection rapide des anomalies
  - Données pour l'optimisation des performances
  - Traçabilité des opérations critiques
  - Facilite le debugging en production

- **Inconvénients :**
  - Overhead minimal sur les performances
  - Complexité additionnelle du déploiement
  - Stockage des métriques et logs

## Métriques surveillées

- **Système** : CPU, mémoire, I/O disque
- **JVM** : Heap, GC, threads
- **HTTP** : Latence, codes de réponse, throughput
- **Base de données** : Pool de connexions, temps de requête
- **Business** : Nombre de ventes, modifications de stock

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
