# Diagrammes Architecture Microservices

Ce dossier contient les diagrammes PlantUML documentant l'architecture microservices du système de gestion de magasin.

## Diagrammes principaux (Architecture actuelle)

### 1. Vue d'ensemble de l'architecture
- **Fichier**: `microservices-architecture-overview.puml`
- **Description**: Vue d'ensemble complète de l'architecture microservices avec tous les services, bases de données, et composants d'infrastructure
- **Contenu**: 
  - 7 microservices (Discovery, Gateway, Frontend, Inventory, Transaction, Store, Personnel)
  - Cluster PostgreSQL avec bases de données séparées
  - Redis pour le cache distribué
  - Stack de monitoring (Prometheus/Grafana)
  - Relations entre tous les composants

### 2. Diagramme de déploiement
- **Fichier**: `microservices-deployment.puml`
- **Description**: Architecture de déploiement avec conteneurs Docker et configuration des ports
- **Contenu**:
  - Conteneurs Docker pour chaque service
  - Configuration des ports et health checks
  - Réseau Docker et communication inter-services
  - Volumes persistants et configuration

### 3. Détail des services
- **Fichier**: `microservices-services-detail.puml`
- **Description**: Architecture détaillée de chaque service avec ses composants internes
- **Contenu**:
  - Structure interne de chaque microservice
  - Couches application (Controller, Service, Repository)
  - Interfaces exposées (REST API, Web Interface)
  - Dépendances entre services

### 4. Flux de données
- **Fichier**: `microservices-data-flow.puml`
- **Description**: Diagramme de séquence montrant les flux de données typiques
- **Contenu**:
  - Séquences de démarrage du système
  - Flux de consultation produits
  - Processus de vente complète
  - Génération de rapports
  - Surveillance système

### 5. Architecture de monitoring
- **Fichier**: `microservices-monitoring.puml`
- **Description**: Architecture complète du monitoring et de l'observabilité
- **Contenu**:
  - Collecte de métriques (Actuator, Micrometer, Prometheus)
  - Visualisation (Grafana, Dashboards)
  - Système d'alertes
  - Configuration et types de métriques

### 6. Architecture de base de données
- **Fichier**: `microservices-database-design.puml`
- **Description**: Modèle de données distribuées avec base de données par service
- **Contenu**:
  - Schéma de chaque base de données
  - Relations entre entités
  - Références cross-services
  - Stratégies de consistance

### 7. Architecture de sécurité
- **Fichier**: `microservices-security.puml`
- **Description**: Architecture de sécurité distribuée
- **Contenu**:
  - Authentification et autorisation
  - JWT, API Keys, Sessions
  - Sécurité par couche
  - Monitoring de sécurité

## Diagrammes de contexte

### 8. Contexte microservices
- **Fichier**: `context-diagram.puml`
- **Description**: Diagramme de contexte mis à jour pour l'architecture microservices
- **Contenu**:
  - Acteurs externes et leurs interactions
  - Points d'entrée du système
  - Systèmes externes et intégrations
  - Flux de données principaux

### 9. Contexte ARC42
- **Fichier**: `arc42-03_context.puml`
- **Description**: Contexte selon la structure ARC42 pour la documentation d'architecture
- **Contenu**:
  - Périmètre du système
  - Interfaces externes
  - Contraintes techniques
  - Stack technologique

## Diagrammes legacy (Architecture précédente)

Ces diagrammes documentent l'architecture précédente et peuvent être supprimés après validation :

- `dcl.puml` - Diagramme de classes DDD (architecture monolithique)
- `di.puml` - Diagramme d'implémentation DDD (architecture monolithique)
- `cu/` - Diagrammes de cas d'usage (peuvent être conservés)
- `dd/` - Diagrammes de domaine (peuvent être conservés)
- `dss/` - Diagrammes de séquence système (peuvent être conservés)

## Utilisation

### Génération des images
Pour générer les images des diagrammes :

```bash
# Avec PlantUML CLI
java -jar plantuml.jar *.puml

# Avec VS Code extension PlantUML
# Utiliser la commande "PlantUML: Preview Current Diagram"
```

### Formats supportés
- PNG (par défaut)
- SVG (vectoriel, recommandé pour la documentation)
- PDF (pour impression)

### Intégration documentation
Ces diagrammes peuvent être intégrés dans :
- README.md du projet
- Documentation technique
- Présentations architecturales
- Revues de code

## Maintenance

### Mise à jour des diagrammes
Lors de modifications de l'architecture :

1. **Identifier** les diagrammes impactés
2. **Mettre à jour** le contenu PlantUML
3. **Régénérer** les images
4. **Valider** la cohérence avec l'implémentation
5. **Documenter** les changements

### Cohérence avec le code
- Vérifier que les ports correspondent à la configuration
- Valider que les noms de services sont corrects
- S'assurer que les flux de données sont exacts
- Maintenir la cohérence avec les ADRs

## Recommandations

### Lisibilité
- Utiliser des couleurs cohérentes
- Ajouter des notes explicatives
- Grouper logiquement les composants
- Éviter la surcharge d'informations

### Maintenance
- Réviser les diagrammes à chaque sprint
- Automatiser la génération si possible
- Versionner avec le code source
- Documenter les décisions architecturales

### Évolution
- Prévoir la scalabilité des diagrammes
- Adapter le niveau de détail au public
- Maintenir plusieurs vues (overview, détail)
- Intégrer dans le processus de développement
