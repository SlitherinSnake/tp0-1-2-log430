# 6. Vue d'exécution

## 6.1. Scénarios d'exécution principaux

Cette section décrit les scénarios d'exécution les plus importants de l'architecture microservices.

### 6.1.1. Démarrage du système

Le démarrage de l'architecture microservices suit un ordre précis géré par Docker Compose avec des health checks.

```plantuml
@startuml system-startup
participant "Docker Compose" as Docker
participant "PostgreSQL" as DB
participant "Redis" as Redis
participant "Discovery Server" as Discovery
participant "API Gateway" as Gateway
participant "Business Services" as Services
participant "Frontend Service" as Frontend

Docker -> DB : Démarrage PostgreSQL
activate DB
Docker -> Redis : Démarrage Redis Cache
activate Redis

Docker -> Discovery : Démarrage Eureka Server
activate Discovery
Discovery -> Discovery : Initialisation registre

DB -> Docker : Health check OK
Redis -> Docker : Health check OK
Discovery -> Docker : Health check OK

Docker -> Services : Démarrage services métier
activate Services
Services -> Discovery : Enregistrement services
Services -> DB : Connexion bases dédiées

Docker -> Gateway : Démarrage API Gateway
activate Gateway
Gateway -> Discovery : Enregistrement gateway
Gateway -> Redis : Configuration cache
Gateway -> Services : Test connectivité

Docker -> Frontend : Démarrage Frontend Service
activate Frontend
Frontend -> Discovery : Enregistrement frontend
Frontend -> Gateway : Test connectivité

note over Docker, Frontend : Tous les services sont opérationnels
@enduml
```

**Ordre de démarrage :**
1. **Infrastructure** : PostgreSQL et Redis
2. **Service Discovery** : Eureka Server
3. **Services métier** : Inventory, Transaction, Store, Personnel
4. **API Gateway** : Routage et sécurité
5. **Frontend Service** : Interface utilisateur

### 6.1.2. Consultation de produits (Client)

Scénario typique d'un client consultant le catalogue produits.

```plantuml
@startuml product-consultation
actor "Client" as Client
participant "Frontend Service" as Frontend
participant "API Gateway" as Gateway
participant "Redis Cache" as Redis
participant "Inventory Service" as Inventory
participant "PostgreSQL" as DB

Client -> Frontend : GET /products
activate Frontend

Frontend -> Gateway : GET /api/inventory/products
activate Gateway

Gateway -> Redis : Vérifier cache
Redis -> Gateway : Cache miss

Gateway -> Inventory : GET /products
activate Inventory

Inventory -> DB : SELECT * FROM inventory_items WHERE is_active = true
activate DB
DB -> Inventory : Liste des produits
deactivate DB

Inventory -> Gateway : JSON Response
deactivate Inventory

Gateway -> Redis : Mise en cache (TTL: 5min)
Gateway -> Frontend : JSON Response
deactivate Gateway

Frontend -> Frontend : Rendu Thymeleaf
Frontend -> Client : Page HTML avec produits
deactivate Frontend

note over Client, DB : Le cache accélère les requêtes suivantes
@enduml
```

### 6.1.3. Enregistrement d'une vente (Employé)

Processus complet d'enregistrement d'une vente avec vérification du stock.

```plantuml
@startuml sale-recording
actor "Employé" as Employee
participant "Frontend Service" as Frontend
participant "API Gateway" as Gateway
participant "Personnel Service" as Personnel
participant "Transaction Service" as Transaction
participant "Inventory Service" as Inventory
participant "PostgreSQL" as DB

Employee -> Frontend : POST /admin/sales
activate Frontend

Frontend -> Gateway : POST /api/transactions
activate Gateway

Gateway -> Gateway : Validation JWT Token
Gateway -> Personnel : GET /personnel/{employeeId}
activate Personnel

Personnel -> DB : Vérifier employé actif
activate DB
DB -> Personnel : Données employé
deactivate DB
Personnel -> Gateway : Employé validé
deactivate Personnel

Gateway -> Transaction : POST /transactions
activate Transaction

Transaction -> DB : BEGIN TRANSACTION
activate DB

== Vérification du stock ==
Transaction -> Gateway : GET /api/inventory/stock/{productId}
Gateway -> Inventory : GET /stock/{productId}
activate Inventory

Inventory -> DB : SELECT stock_central FROM inventory_items WHERE id = ?
DB -> Inventory : Stock disponible
Inventory -> Gateway : Stock response
deactivate Inventory
Gateway -> Transaction : Stock disponible

== Mise à jour du stock ==
Transaction -> Gateway : PUT /api/inventory/stock/{productId}
Gateway -> Inventory : PUT /stock/{productId}
activate Inventory

Inventory -> DB : UPDATE inventory_items SET stock_central = stock_central - ?
DB -> Inventory : Stock mis à jour
Inventory -> Gateway : Confirmation
deactivate Inventory
Gateway -> Transaction : Stock confirmé

== Finalisation de la transaction ==
Transaction -> DB : INSERT INTO transactions + transaction_items
Transaction -> DB : COMMIT TRANSACTION
deactivate DB

Transaction -> Gateway : Transaction créée
deactivate Transaction

Gateway -> Frontend : Success response
deactivate Gateway

Frontend -> Employee : Page de confirmation
deactivate Frontend

note over Employee, DB : Transaction atomique avec vérification de stock
@enduml
```

### 6.1.4. Génération de rapport (Administrateur)

Processus de génération d'un rapport consolidé multi-services.

```plantuml
@startuml report-generation
actor "Administrateur" as Admin
participant "Frontend Service" as Frontend
participant "API Gateway" as Gateway
participant "Transaction Service" as Transaction
participant "Inventory Service" as Inventory
participant "Store Service" as Store
participant "PostgreSQL" as DB

Admin -> Frontend : GET /admin/reports
activate Frontend

Frontend -> Gateway : GET /api/reports/dashboard
activate Gateway

== Collecte des données de ventes ==
Gateway -> Transaction : GET /reports/sales?period=monthly
activate Transaction
Transaction -> DB : SELECT avec agrégations par magasin
activate DB
DB -> Transaction : Données de ventes
deactivate DB
Transaction -> Gateway : Statistiques ventes
deactivate Transaction

== Collecte des données d'inventaire ==
Gateway -> Inventory : GET /reports/stock-summary
activate Inventory
Inventory -> DB : SELECT stock levels, low stock items
activate DB
DB -> Inventory : Résumé du stock
deactivate DB
Inventory -> Gateway : Statistiques stock
deactivate Inventory

== Collecte des données de magasins ==
Gateway -> Store : GET /stores/performance
activate Store
Store -> DB : SELECT performance par magasin
activate DB
DB -> Store : Données de performance
deactivate DB
Store -> Gateway : Performance magasins
deactivate Store

Gateway -> Gateway : Agrégation des données
Gateway -> Frontend : Rapport consolidé JSON
deactivate Gateway

Frontend -> Frontend : Génération graphiques
Frontend -> Admin : Dashboard avec visualisations
deactivate Frontend

note over Admin, DB : Données agrégées de multiple sources
@enduml
```

### 6.1.5. Gestion des pannes (Circuit Breaker)

Comportement du système en cas de panne d'un service.

```plantuml
@startuml circuit-breaker
participant "API Gateway" as Gateway
participant "Circuit Breaker" as CB
participant "Inventory Service" as Inventory
participant "Cache" as Cache

Gateway -> CB : Appel vers Inventory Service
activate CB

CB -> Inventory : GET /products
activate Inventory
Inventory -> Inventory : Service indisponible
Inventory --> CB : Timeout/Error
deactivate Inventory

CB -> CB : Incrémenter compteur d'erreurs
CB -> CB : Seuil atteint - Circuit OUVERT

CB -> Cache : Récupérer données en cache
activate Cache
Cache -> CB : Données cached (possiblement obsolètes)
deactivate Cache

CB -> Gateway : Réponse dégradée avec données cached
deactivate CB

note over Gateway, Cache : Le système continue de fonctionner en mode dégradé

loop Vérification périodique
    CB -> Inventory : Health check
    alt Service récupéré
        Inventory -> CB : OK
        CB -> CB : Circuit FERMÉ
    else Service toujours en panne
        Inventory --> CB : Erreur
        CB -> CB : Circuit reste OUVERT
    end
end
@enduml
```

## 6.2. Patterns d'exécution

### 6.2.1. Service Discovery

```plantuml
@startuml service-discovery
participant "New Service Instance" as NewService
participant "Discovery Server" as Discovery
participant "API Gateway" as Gateway
participant "Other Services" as Services

NewService -> Discovery : POST /eureka/apps/{service-name}
activate Discovery
Discovery -> Discovery : Enregistrer instance
Discovery -> NewService : Confirmation
deactivate Discovery

loop Health Check (30s interval)
    Discovery -> NewService : GET /actuator/health
    NewService -> Discovery : 200 OK / Status
end

Gateway -> Discovery : GET /eureka/apps
activate Discovery
Discovery -> Gateway : Liste des services disponibles
deactivate Discovery

Gateway -> Gateway : Mise à jour du routage

Services -> Discovery : GET /eureka/apps/{target-service}
activate Discovery
Discovery -> Services : Instances disponibles
deactivate Discovery

note over NewService, Services : Découverte automatique des services
@enduml
```

### 6.2.2. Load Balancing

```plantuml
@startuml load-balancing
participant "API Gateway" as Gateway
participant "Load Balancer" as LB
participant "Service Instance 1" as S1
participant "Service Instance 2" as S2
participant "Service Instance 3" as S3

Gateway -> LB : Requête vers service
activate LB

LB -> LB : Round-robin algorithm
LB -> S1 : Forwarding request
activate S1
S1 -> LB : Response
deactivate S1
LB -> Gateway : Response

Gateway -> LB : Nouvelle requête
LB -> LB : Algorithme round-robin
LB -> S2 : Forwarding request
activate S2
S2 -> LB : Response
deactivate S2
LB -> Gateway : Response

Gateway -> LB : Troisième requête
LB -> S3 : Forwarding request
activate S3
S3 -> LB : Response
deactivate S3
LB -> Gateway : Response

deactivate LB

note over Gateway, S3 : Distribution équitable de la charge
@enduml
```

## 6.3. Monitoring et observabilité

### 6.3.1. Collecte de métriques

```plantuml
@startuml metrics-collection
participant "Business Services" as Services
participant "Actuator" as Actuator
participant "Prometheus" as Prometheus
participant "Grafana" as Grafana

loop Collecte périodique (15s)
    Prometheus -> Services : GET /actuator/prometheus
    activate Services
    Services -> Actuator : Génération métriques
    activate Actuator
    Actuator -> Services : Métriques formatées
    deactivate Actuator
    Services -> Prometheus : Métriques Prometheus format
    deactivate Services
    
    Prometheus -> Prometheus : Stockage métriques
end

Grafana -> Prometheus : Query métriques
activate Prometheus
Prometheus -> Grafana : Données temporelles
deactivate Prometheus

Grafana -> Grafana : Génération dashboards
@enduml
```

## 6.4. Gestion des erreurs

### Stratégies de résilience

1. **Circuit Breaker** : Protection contre les pannes en cascade
2. **Retry** : Tentatives automatiques en cas d'erreur temporaire
3. **Timeout** : Limitation du temps d'attente
4. **Fallback** : Réponses dégradées avec données cached
5. **Health Checks** : Surveillance proactive des services

### Codes d'erreur standardisés

| Code | Signification | Action |
|------|---------------|--------|
| 200 | Success | Traitement normal |
| 400 | Bad Request | Validation des données |
| 401 | Unauthorized | Redirection vers authentification |
| 403 | Forbidden | Vérification des permissions |
| 404 | Not Found | Ressource inexistante |
| 500 | Internal Error | Logging et fallback |
| 503 | Service Unavailable | Circuit breaker activé |
