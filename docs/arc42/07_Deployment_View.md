# 7. Vue de déploiement

Cette section décrit l'infrastructure technique nécessaire à l'exécution du système, ainsi que la répartition des composants logiciels sur cette infrastructure.

Le système est déployé dans un environnement conteneurisé avec Docker Compose, offrant une architecture web moderne et scalable.

## 7.1. Architecture de déploiement globale

Le système repose sur une architecture web conteneurisée avec séparation claire entre l'application et la base de données.

### Composants de déploiement

- **Application Spring Boot** : conteneur `app` exposant l'interface web et l'API REST
- **Base de données PostgreSQL** : conteneur `postgres` pour la persistance
- **Interface d'administration** : accès optionnel via pgAdmin pour la gestion de la base

## 7.2. Infrastructure Docker Compose

```yaml
services:
  app:
    image: pos-web:latest
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=postgres
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/pos_db
    depends_on:
      - postgres

  postgres:
    image: postgres:15
    environment:
      - POSTGRES_DB=pos_db
      - POSTGRES_USER=pos_user
      - POSTGRES_PASSWORD=pos_password
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"
```

## 7.3. Flux de communication

1. **Utilisateur → Application Web** : HTTP/HTTPS sur port 8080
2. **Application → Base de données** : JDBC/PostgreSQL sur port 5432 (réseau interne Docker)
3. **API externe → Application** : REST/JSON avec authentification JWT

## 7.4. Sécurité du déploiement

- **Isolation des conteneurs** : chaque service dans son propre conteneur
- **Réseau privé** : communication base de données via réseau Docker interne
- **Variables d'environnement** : configuration sensible externalisée
- **Volumes persistants** : données PostgreSQL conservées entre redémarrages

## 7.5. Scalabilité

L'architecture permet facilement :

- **Réplication horizontale** : déploiement de plusieurs instances de l'application
- **Load balancing** : ajout d'un reverse proxy (nginx/HAProxy)
- **Cache distribué** : intégration possible de Redis
- **Base de données** : configuration en cluster PostgreSQL pour haute disponibilité

## 7.6. Monitoring et observabilité

- **Health checks** : endpoint `/actuator/health` pour vérifier l'état de l'application
- **Métriques** : endpoint `/actuator/metrics` pour Prometheus/Grafana
- **Logs** : collecte via Docker logs, agrégation possible avec ELK stack
- **Traces** : préparation pour distributed tracing (Jaeger/Zipkin)