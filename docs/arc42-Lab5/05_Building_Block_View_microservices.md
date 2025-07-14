# 5. Vue des blocs de construction

## 5.1. Vue d'ensemble de l'architecture microservices

### Niveau 1 : Vue générale du système

L'architecture microservices est organisée en trois couches principales :

1. **Couche présentation** : Interface utilisateur et API Gateway
2. **Couche services** : Services métier indépendants
3. **Couche données** : Bases de données distribuées et cache

```plantuml
@startuml building-blocks-overview
package "Couche Présentation" {
  [Frontend Service]
  [API Gateway]
}

package "Couche Services Métier" {
  [Inventory Service]
  [Transaction Service]
  [Store Service]
  [Personnel Service]
}

package "Couche Infrastructure" {
  [Discovery Server]
  [PostgreSQL Cluster]
  [Redis Cache]
  [Monitoring Stack]
}

[Frontend Service] --> [API Gateway]
[API Gateway] --> [Inventory Service]
[API Gateway] --> [Transaction Service]
[API Gateway] --> [Store Service]
[API Gateway] --> [Personnel Service]

[Inventory Service] --> [PostgreSQL Cluster]
[Transaction Service] --> [PostgreSQL Cluster]
[Store Service] --> [PostgreSQL Cluster]
[Personnel Service] --> [PostgreSQL Cluster]

[API Gateway] --> [Redis Cache]
[API Gateway] --> [Discovery Server]
@enduml
```

### Conteneurs principaux

| Service | Port | Responsabilité | Base de données |
|---------|------|----------------|-----------------|
| **discovery-server** | 8761 | Service de découverte (Eureka) | - |
| **api-gateway** | 8765 | Routage, sécurité, rate limiting | gateway_db |
| **frontend-service** | 8080 | Interface web utilisateur | - |
| **inventory-service** | 8081 | Gestion produits et stock | inventory_db |
| **transaction-service** | 8082 | Ventes et retours | transaction_db |
| **store-service** | 8083 | Magasins et localisations | store_db |
| **personnel-service** | 8084 | Employés et authentification | personnel_db |

## 5.2. Niveau 2 : Décomposition des services

### 5.2.1. API Gateway (Boîte blanche)

**Responsabilité** : Point d'entrée unique pour toutes les requêtes externes

**Composants internes :**

```plantuml
@startuml api-gateway-detail
package "API Gateway" {
  component "Gateway Core" as Core
  component "Authentication Filter" as Auth
  component "Rate Limiter" as RateLimit
  component "Circuit Breaker" as CircuitBreaker
  component "Load Balancer" as LoadBalancer
  component "CORS Handler" as CORS
  
  interface "Public API" as PublicAPI
  interface "Admin API" as AdminAPI
}

PublicAPI --> Core
AdminAPI --> Core
Core --> Auth
Core --> RateLimit
Core --> CircuitBreaker
Core --> LoadBalancer
Core --> CORS
@enduml
```

**Interfaces :**
- **Entrée** : Requêtes HTTP/HTTPS des clients
- **Sortie** : Routage vers les services métier
- **Dépendances** : Discovery Server, Redis Cache

### 5.2.2. Frontend Service (Boîte blanche)

**Responsabilité** : Interface utilisateur web avec rendu côté serveur

**Composants internes :**

```plantuml
@startuml frontend-service-detail
package "Frontend Service" {
  component "Web Controllers" as Controllers
  component "Thymeleaf Engine" as Thymeleaf
  component "WebClient" as WebClient
  component "Static Resources" as Static
  component "Session Manager" as Session
  
  interface "Web Interface" as WebInterface
}

WebInterface --> Controllers
Controllers --> Thymeleaf
Controllers --> WebClient
Controllers --> Static
Controllers --> Session
@enduml
```

**Interfaces :**
- **Entrée** : Requêtes HTTP des navigateurs
- **Sortie** : Pages HTML et appels API vers Gateway
- **Dépendances** : API Gateway

### 5.2.3. Inventory Service (Boîte blanche)

**Responsabilité** : Gestion des produits et du stock central

**Modèle de domaine :**

```plantuml
@startuml inventory-domain
class InventoryItem {
  - id : Long
  - nom : String
  - categorie : String
  - prix : Double
  - description : String
  - stockCentral : Integer
  - stockMinimum : Integer
  - dateDerniereMaj : LocalDate
  - isActive : boolean
  --
  + hasStock(quantite : int) : boolean
  + decreaseStock(quantite : int) : void
  + increaseStock(quantite : int) : void
  + needsRestock() : boolean
}

class Category {
  - id : Long
  - nom : String
  - description : String
  - isActive : boolean
}

class StockMovement {
  - id : Long
  - inventoryItemId : Long
  - movementType : String
  - quantity : Integer
  - previousStock : Integer
  - newStock : Integer
  - reason : String
  - timestamp : LocalDateTime
}

InventoryItem }o--|| Category
StockMovement }o--|| InventoryItem
@enduml
```

**Architecture en couches :**

| Couche | Composants | Responsabilité |
|--------|------------|----------------|
| **API** | `InventoryController` | Exposition des endpoints REST |
| **Application** | `InventoryService` | Logique applicative et orchestration |
| **Domain** | `InventoryItem`, `Category` | Entités métier et règles business |
| **Infrastructure** | `InventoryRepository` | Accès aux données |

### 5.2.4. Transaction Service (Boîte blanche)

**Responsabilité** : Gestion des ventes et retours

**Modèle de domaine :**

```plantuml
@startuml transaction-domain
class Transaction {
  - id : Long
  - typeTransaction : TypeTransaction
  - dateTransaction : LocalDate
  - montantTotal : Double
  - personnelId : Long
  - storeId : Long
  - transactionOriginaleId : Long
  - statut : StatutTransaction
  --
  + addItem(item : TransactionItem) : void
  + calculateTotal() : void
  + complete() : void
  + cancel() : void
}

class TransactionItem {
  - id : Long
  - transactionId : Long
  - inventoryItemId : Long
  - quantite : Integer
  - prixUnitaire : Double
  - sousTotal : Double
}

enum TypeTransaction {
  VENTE
  RETOUR
}

enum StatutTransaction {
  EN_COURS
  COMPLETEE
  ANNULEE
}

Transaction ||--o{ TransactionItem
Transaction --> TypeTransaction
Transaction --> StatutTransaction
@enduml
```

### 5.2.5. Store Service (Boîte blanche)

**Responsabilité** : Gestion des magasins et de leurs configurations

**Modèle de domaine :**

```plantuml
@startuml store-domain
class Store {
  - id : Long
  - nom : String
  - adresse : String
  - ville : String
  - codePostal : String
  - telephone : String
  - email : String
  - managerId : Long
  - isActive : boolean
}

class StoreInventory {
  - id : Long
  - storeId : Long
  - inventoryItemId : Long
  - stockLocal : Integer
  - stockMinimum : Integer
  - lastRestock : LocalDate
}

class StoreConfiguration {
  - id : Long
  - storeId : Long
  - configKey : String
  - configValue : String
}

Store ||--o{ StoreInventory
Store ||--o{ StoreConfiguration
@enduml
```

### 5.2.6. Personnel Service (Boîte blanche)

**Responsabilité** : Gestion des employés et authentification

**Modèle de domaine :**

```plantuml
@startuml personnel-domain
class Personnel {
  - id : Long
  - nom : String
  - prenom : String
  - email : String
  - telephone : String
  - role : Role
  - storeId : Long
  - dateEmbauche : LocalDate
  - isActive : boolean
}

enum Role {
  ADMIN
  EMPLOYEE
  VIEWER
}

class UserSession {
  - id : Long
  - personnelId : Long
  - sessionToken : String
  - loginTime : LocalDateTime
  - logoutTime : LocalDateTime
  - isActive : boolean
}

Personnel --> Role
Personnel ||--o{ UserSession
@enduml
```

## 5.3. Architecture technique commune

### Pattern par service

Chaque service métier suit la même architecture en couches :

```plantuml
@startuml service-architecture-pattern
package "Service Pattern" {
  package "Presentation Layer" {
    component "REST Controller" as Controller
    component "DTOs" as DTO
  }
  
  package "Application Layer" {
    component "Service Layer" as Service
    component "Validation" as Validation
  }
  
  package "Domain Layer" {
    component "Entities" as Entities
    component "Business Logic" as BusinessLogic
  }
  
  package "Infrastructure Layer" {
    component "JPA Repository" as Repository
    component "Database Access" as DatabaseAccess
  }
}

Controller --> Service
Controller --> DTO
Service --> Validation
Service --> Entities
Service --> BusinessLogic
Service --> Repository
Repository --> DatabaseAccess
@enduml
```

### Configuration commune

**Spring Boot Starters utilisés :**
- `spring-boot-starter-web` : APIs REST
- `spring-boot-starter-data-jpa` : Persistance
- `spring-boot-starter-actuator` : Monitoring
- `spring-cloud-starter-netflix-eureka-client` : Service discovery
- `spring-boot-starter-cache` : Cache local

**Configuration Docker :**
- **Dockerfile** standardisé pour tous les services
- **Health checks** configurés
- **Variables d'environnement** pour la configuration
- **Multi-stage builds** pour optimiser la taille

## 5.4. Communication entre services

### Patterns de communication

```plantuml
@startuml communication-patterns
!define RECTANGLE class

RECTANGLE "Frontend Service" as Frontend
RECTANGLE "API Gateway" as Gateway
RECTANGLE "Business Services" as Services
RECTANGLE "Discovery Server" as Discovery
RECTANGLE "Database" as DB

Frontend --> Gateway : REST API
Gateway --> Services : Load Balanced REST
Services --> Discovery : Service Registration
Services --> DB : JPA/JDBC
Gateway --> Discovery : Service Discovery
@enduml
```

### Contrats d'interface

**REST APIs standardisées :**
- Format JSON pour tous les échanges
- Codes de statut HTTP standards
- Documentation OpenAPI/Swagger
- Versioning par URL (`/api/v1/`)

**Service Discovery :**
- Enregistrement automatique via Eureka
- Health checks périodiques
- Load balancing côté client
- Failover automatique

## 5.5. Données et persistance

### Modèle de données distribué

Chaque service possède sa propre base de données, respectant le principe "Database per Service" :

```plantuml
@startuml database-distribution
database "gateway_db" as GatewayDB
database "inventory_db" as InventoryDB
database "transaction_db" as TransactionDB
database "store_db" as StoreDB
database "personnel_db" as PersonnelDB

[API Gateway] --> GatewayDB
[Inventory Service] --> InventoryDB
[Transaction Service] --> TransactionDB
[Store Service] --> StoreDB
[Personnel Service] --> PersonnelDB
@enduml
```

### Stratégies de consistance

- **Eventual Consistency** : Cohérence à terme via événements
- **Saga Pattern** : Transactions distribuées (future implémentation)
- **API Calls** : Synchronisation via appels REST
- **Idempotency** : Opérations répétables sans effet de bord
