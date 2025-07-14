# ADR 008 – Infrastructure et déploiement containerisé

## Status

Accepted

## Context

L'architecture microservices nécessite une approche robuste pour le déploiement et la gestion de l'infrastructure. Avec 7 services indépendants, une base de données PostgreSQL, Redis, et les outils de monitoring (Prometheus et Grafana), la complexité de déploiement et de configuration a significativement augmenté.

Les défis identifiés :

- **Déploiement multi-services** : coordination du démarrage de 7+ services
- **Dépendances inter-services** : certains services dépendent d'autres pour fonctionner
- **Configuration environnementale** : gestion des configurations par environnement
- **Monitoring centralisé** : surveillance de l'ensemble de l'architecture
- **Persistance des données** : gestion des volumes et backup
- **Networking** : communication sécurisée entre services

## Decision

Implémentation d'une **infrastructure containerisée** avec Docker et Docker Compose :

### Containerisation

- **Docker** pour la containerisation de chaque service
- **Multi-stage builds** pour optimiser la taille des images
- **Images basées sur OpenJDK 21** pour tous les services Java
- **Dockerfile standardisé** pour tous les microservices

### Orchestration

- **Docker Compose** pour l'orchestration des services
- **Health checks** pour tous les services critiques
- **Restart policies** pour la résilience
- **Depends_on** avec conditions pour gérer les dépendances

### Configuration

- **Profiles Spring** pour différencier les environnements (`docker`, `local`)
- **Variables d'environnement** pour la configuration des services
- **Secrets** gérés via Docker Compose
- **Configuration centralisée** via fichiers de configuration

### Networking

- **Réseau Docker** par défaut pour la communication inter-services
- **DNS automatique** via les noms de services
- **Ports exposés** seulement pour les services publics
- **Isolation** des services internes

### Services d'infrastructure

```yaml
# Services principaux
- discovery-server:8761    # Service de découverte
- api-gateway:8765        # Point d'entrée principal
- frontend-service:8080   # Interface utilisateur

# Services métier
- inventory-service:8081  # Gestion inventaire
- transaction-service:8082 # Gestion transactions  
- store-service:8083      # Gestion magasins
- personnel-service:8084  # Gestion personnel

# Infrastructure
- postgres:5432           # Base de données
- redis:6379             # Cache distribué
- prometheus:9090        # Monitoring
- grafana:3000           # Visualisation
```

### Volumes et persistance

- **postgres_data** : Persistance des données PostgreSQL
- **prometheus_data** : Stockage des métriques Prometheus
- **grafana_data** : Configuration et dashboards Grafana
- **Scripts d'initialisation** : `init-db.sql` pour la création des bases

## Justification

L'infrastructure containerisée apporte plusieurs avantages :

### Avantages techniques

- **Reproductibilité** : environnements identiques sur tous les postes
- **Isolation** : chaque service dans son propre conteneur
- **Scalabilité** : possibilité de scaler individuellement chaque service
- **Portabilité** : déploiement sur différents environnements
- **Versionning** : gestion des versions d'images Docker

### Avantages opérationnels

- **Déploiement simplifié** : une seule commande pour démarrer l'ensemble
- **Monitoring intégré** : Prometheus et Grafana inclus
- **Gestion des dépendances** : démarrage ordonné des services
- **Health checks** : surveillance automatique de la santé des services
- **Rollback facile** : retour à une version précédente simplifié

## Consequences

### Avantages

- **Simplicité de déploiement** : `docker-compose up` pour tout démarrer
- **Isolation des services** : pas de conflits entre services
- **Monitoring prêt à l'emploi** : Prometheus et Grafana préconfigurés
- **Environnements reproductibles** : développement et production identiques
- **Gestion automatique des dépendances** : démarrage ordonné
- **Résilience** : restart automatique en cas de panne

### Inconvénients

- **Consommation mémoire** : overhead des conteneurs
- **Courbe d'apprentissage** : connaissance Docker nécessaire
- **Debugging complexe** : logs distribués entre conteneurs
- **Persistence** : gestion des volumes et backup
- **Networking** : configuration réseau plus complexe

### Risques et mitigations

- **Perte de données** : Volumes persistants pour PostgreSQL
- **Performance** : Configuration optimisée des conteneurs
- **Sécurité** : Isolation réseau et gestion des secrets
- **Monitoring** : Health checks et alertes Prometheus

## Configuration Docker Compose

### Structure des services

```yaml
services:
  # Service discovery
  discovery-server:
    build: ./discovery-server
    ports: ["8761:8761"]
    healthcheck: [curl, health endpoint]
    
  # API Gateway avec dépendances
  api-gateway:
    build: ./api-gateway
    depends_on:
      discovery-server: {condition: service_healthy}
      postgres: {condition: service_healthy}
      redis: {condition: service_started}
    
  # Services métier avec isolation DB
  inventory-service:
    environment:
      - POSTGRES_URL=jdbc:postgresql://postgres:5432/inventory_db
```

### Scripts de déploiement

- **build-all.cmd** : Construction de toutes les images
- **clean-all.cmd** : Nettoyage des conteneurs et volumes
- **setup-monitoring.cmd** : Configuration du monitoring

### Monitoring et observabilité

- **Prometheus** : Configuration avec découverte automatique des services
- **Grafana** : Dashboards préconfigurés pour les Golden Signals
- **Alert rules** : Alertes configurées pour les métriques critiques
- **Health checks** : Surveillance de la santé de chaque service

## Migration et déploiement

### Environnements supportés

- **Développement local** : Docker Compose sur poste développeur
- **Staging** : Même configuration pour les tests
- **Production** : Base pour migration vers Kubernetes

### Processus de déploiement

1. **Build** : Construction des images Docker
2. **Database** : Initialisation des bases de données
3. **Infrastructure** : Démarrage des services d'infrastructure
4. **Services** : Démarrage des microservices
5. **Monitoring** : Configuration du monitoring

### Commandes principales

```bash
# Démarrage complet
docker-compose up -d

# Arrêt propre
docker-compose down

# Reconstruction
docker-compose build

# Logs centralisés
docker-compose logs -f [service]
```

## Options envisagées

- **Alternative 1** : Déploiement manuel sur VMs (complexité opérationnelle)
- **Alternative 2** : Kubernetes (complexité excessive pour la taille du projet)
- **Alternative 3** : Docker Swarm (fonctionnalités limitées)
- **Alternative 4** : Solutions cloud managées (dépendance externe)

## Prochaines étapes

- **Kubernetes** : Migration vers Kubernetes pour la production
- **CI/CD** : Intégration continue et déploiement automatique
- **Secrets management** : Gestion sécurisée des secrets
- **Backup automatique** : Stratégie de sauvegarde des données
- **Blue-green deployment** : Déploiement sans interruption
