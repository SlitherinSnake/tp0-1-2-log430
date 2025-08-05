# 5. Vue des blocs de construction - Architecture Saga

## 5.1. Vue d'ensemble de l'architecture avec Saga

### Niveau 1 : Vue générale du système avec orchestration

L'architecture microservices est maintenant enrichie d'un **Saga Orchestrator Service** pour gérer les transactions distribuées.

```plantuml
@startuml building-blocks-saga-overview
package "Couche Présentation" {
  [Frontend Service]
  [API Gateway]
}

package "Couche Orchestration" {
  [Saga Orchestrator] #lightblue
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
[API Gateway] --> [Saga Orchestrator] : Transactions distribuées
[API Gateway] --> [Inventory Service] : Requêtes simples
[API Gateway] --> [Transaction Service] : Requêtes simples
[API Gateway] --> [Store Service] : Requêtes simples
[API Gateway] --> [Personnel Service] : Requêtes simples

[Saga Orchestrator] --> [Inventory Service] : Étapes saga
[Saga Orchestrator] --> [Transaction Service] : Étapes saga
[Saga Orchestrator] --> [Store Service] : Étapes saga
[Saga Orchestrator] --> [Personnel Service] : Étapes saga

[Saga Orchestrator] --> [PostgreSQL Cluster] : saga_db
[Inventory Service] --> [PostgreSQL Cluster]
[Transaction Service] --> [PostgreSQL Cluster]
[Store Service] --> [PostgreSQL Cluster]
[Personnel Service] --> [PostgreSQL Cluster]

[API Gateway] --> [Redis Cache]
[API Gateway] --> [Discovery Server]
[Saga Orchestrator] --> [Discovery Server]
@enduml
```

### Conteneurs mis à jour

| Service | Port | Responsabilité | Base de données | Nouveau |
|---------|------|----------------|-----------------|---------|
| **discovery-server** | 8761 | Service de découverte (Eureka) | - | Non |
| **api-gateway** | 8765 | Routage, sécurité, rate limiting | gateway_db | Non |
| **frontend-service** | 8080 | Interface web utilisateur | - | Non |
| **saga-orchestrator** | 8085 | **Orchestration transactions distribuées** | **saga_db** | **✅ Oui** |
| **inventory-service** | 8081 | Gestion produits et stock | inventory_db | Non |
| **transaction-service** | 8082 | Ventes et retours | transaction_db | Non |
| **store-service** | 8083 | Magasins et localisations | store_db | Non |
| **personnel-service** | 8084 | Employés et authentification | personnel_db | Non |

## 5.2. Niveau 2 : Saga Orchestrator Service (Boîte blanche)

### 5.2.1. Architecture interne du Saga Orchestrator

**Responsabilité** : Orchestration centralisée des transactions distribuées avec gestion des compensations

```plantuml
@startuml saga-orchestrator-detail
package "Saga Orchestrator Service" {
  
  package "API Layer" {
    component "Saga Controller" as SagaController
    component "Admin Controller" as AdminController
    component "Health Controller" as HealthController
  }
  
  package "Orchestration Layer" {
    component "Saga Manager" as SagaManager
    component "State Machine" as StateMachine
    component "Workflow Engine" as WorkflowEngine
  }
  
  package "Execution Layer" {
    component "Step Executor" as StepExecutor
    component "Compensator" as Compensator
    component "Retry Handler" as RetryHandler
  }
  
  package "Persistence Layer" {
    component "Saga Repository" as SagaRepository
    component "Event Store" as EventStore
    component "Metrics Collector" as MetricsCollector
  }
  
  package "Communication Layer" {
    component "HTTP Client" as HttpClient
    component "Circuit Breaker" as CircuitBreaker
    component "Load Balancer" as LoadBalancer
  }
  
  interface "Saga API" as SagaAPI
  interface "Admin API" as AdminAPI
  interface "Service Discovery" as ServiceDiscovery
  database "saga_db" as SagaDB
}

SagaAPI --> SagaController
AdminAPI --> AdminController
SagaController --> SagaManager
AdminController --> SagaManager

SagaManager --> StateMachine
SagaManager --> WorkflowEngine
SagaManager --> SagaRepository

WorkflowEngine --> StepExecutor
WorkflowEngine --> Compensator
StepExecutor --> RetryHandler

StepExecutor --> HttpClient
HttpClient --> CircuitBreaker
HttpClient --> LoadBalancer

SagaRepository --> SagaDB
EventStore --> SagaDB
MetricsCollector --> SagaDB

LoadBalancer --> ServiceDiscovery
@enduml
```

### 5.2.2. Modèle de domaine Saga

```plantuml
@startuml saga-domain-model
class Saga {
  - id : Long
  - sagaType : SagaType
  - status : SagaStatus
  - currentStep : Integer
  - totalSteps : Integer
  - payload : JsonNode
  - correlationId : UUID
  - createdAt : LocalDateTime
  - updatedAt : LocalDateTime
  - completedAt : LocalDateTime
  - errorMessage : String
  - retryCount : Integer
  - maxRetries : Integer
  --
  + start() : void
  + nextStep() : void
  + compensate() : void
  + complete() : void
  + fail(reason : String) : void
}

class SagaStep {
  - id : Long
  - sagaId : Long
  - stepNumber : Integer
  - stepName : String
  - status : StepStatus
  - serviceName : String
  - serviceUrl : String
  - httpMethod : HttpMethod
  - requestPayload : JsonNode
  - responsePayload : JsonNode
  - compensationUrl : String
  - startedAt : LocalDateTime
  - completedAt : LocalDateTime
  - errorMessage : String
  - retryCount : Integer
  - timeoutSeconds : Integer
  --
  + execute() : StepResult
  + compensate() : StepResult
  + retry() : void
}

class SagaEvent {
  - id : Long
  - sagaId : Long
  - eventType : EventType
  - eventData : JsonNode
  - createdAt : LocalDateTime
  - createdBy : String
}

enum SagaType {
  SALES_SAGA
  RETURN_SAGA
  INVENTORY_TRANSFER_SAGA
  EMPLOYEE_ONBOARDING_SAGA
}

enum SagaStatus {
  STARTED
  IN_PROGRESS
  COMPLETED
  COMPENSATING
  COMPENSATED
  FAILED
}

enum StepStatus {
  PENDING
  IN_PROGRESS
  COMPLETED
  FAILED
  COMPENSATED
  SKIPPED
}

enum EventType {
  SAGA_STARTED
  STEP_STARTED
  STEP_COMPLETED
  STEP_FAILED
  COMPENSATION_STARTED
  COMPENSATION_COMPLETED
  SAGA_COMPLETED
  SAGA_FAILED
}

Saga ||--o{ SagaStep
Saga ||--o{ SagaEvent
SagaStep --> StepStatus
Saga --> SagaStatus
Saga --> SagaType
SagaEvent --> EventType
@enduml
```

### 5.2.3. Définitions des Sagas

#### Sales Saga (Vente)

```plantuml
@startuml sales-saga-workflow
state "Sales Saga" as SalesSaga {
  [*] --> ValidateEmployee : Start
  ValidateEmployee --> ReserveStock : Employee Valid
  ReserveStock --> CreateTransaction : Stock Reserved
  CreateTransaction --> ConfirmStock : Transaction Created
  ConfirmStock --> [*] : Sale Completed
  
  ValidateEmployee --> CompensateEmployee : Employee Invalid
  ReserveStock --> CompensateReservation : Stock Unavailable
  CreateTransaction --> CompensateTransaction : Transaction Failed
  ConfirmStock --> CompensateConfirmation : Confirmation Failed
  
  CompensateEmployee --> [*] : Compensated
  CompensateReservation --> CompensateEmployee : Compensated
  CompensateTransaction --> CompensateReservation : Compensated
  CompensateConfirmation --> CompensateTransaction : Compensated
}

note right of ValidateEmployee : Personnel Service\nGET /saga/validate-employee/{id}
note right of ReserveStock : Inventory Service\nPOST /saga/reserve-stock
note right of CreateTransaction : Transaction Service\nPOST /saga/create-transaction
note right of ConfirmStock : Inventory Service\nPOST /saga/confirm-stock
@enduml
```

#### Return Saga (Retour)

```plantuml
@startuml return-saga-workflow
state "Return Saga" as ReturnSaga {
  [*] --> ValidateOriginalTransaction : Start
  ValidateOriginalTransaction --> CreateReturnTransaction : Transaction Valid
  CreateReturnTransaction --> RestoreStock : Return Created
  RestoreStock --> [*] : Return Completed
  
  ValidateOriginalTransaction --> [*] : Invalid Transaction
  CreateReturnTransaction --> CompensateReturn : Return Failed
  RestoreStock --> CompensateStock : Stock Restore Failed
  
  CompensateReturn --> [*] : Compensated
  CompensateStock --> CompensateReturn : Compensated
}

note right of ValidateOriginalTransaction : Transaction Service\nGET /saga/validate-transaction/{id}
note right of CreateReturnTransaction : Transaction Service\nPOST /saga/create-return
note right of RestoreStock : Inventory Service\nPOST /saga/restore-stock
@enduml
```

## 5.3. Services métier adaptés pour les Sagas

### 5.3.1. Inventory Service - Endpoints Saga

**Nouveaux endpoints pour l'orchestration :**

```java
@RestController
@RequestMapping("/saga")
public class InventorySagaController {
    
    @PostMapping("/reserve-stock")
    public SagaStepResult reserveStock(@RequestBody ReserveStockRequest request);
    
    @PostMapping("/confirm-stock")
    public SagaStepResult confirmStock(@RequestBody ConfirmStockRequest request);
    
    @PostMapping("/release-stock")
    public SagaStepResult releaseStock(@RequestBody ReleaseStockRequest request);
    
    @PostMapping("/restore-stock")
    public SagaStepResult restoreStock(@RequestBody RestoreStockRequest request);
}
```

**Modèle de données étendu :**

```plantuml
@startuml inventory-saga-model
class InventoryItem {
  - id : Long
  - nom : String
  - categorie : String
  - prix : Double
  - description : String
  - stockCentral : Integer
  - stockReserve : Integer
  - stockMinimum : Integer
  - dateDerniereMaj : LocalDate
  - isActive : boolean
  --
  + reserveStock(quantite : int, sagaId : String) : boolean
  + confirmReservation(sagaId : String) : boolean
  + releaseReservation(sagaId : String) : boolean
}

class StockReservation {
  - id : Long
  - inventoryItemId : Long
  - sagaId : String
  - quantityReserved : Integer
  - reservedAt : LocalDateTime
  - expiresAt : LocalDateTime
  - status : ReservationStatus
}

enum ReservationStatus {
  RESERVED
  CONFIRMED
  RELEASED
  EXPIRED
}

InventoryItem ||--o{ StockReservation
StockReservation --> ReservationStatus
@enduml
```

### 5.3.2. Transaction Service - Endpoints Saga

**Nouveaux endpoints pour l'orchestration :**

```java
@RestController
@RequestMapping("/saga")
public class TransactionSagaController {
    
    @PostMapping("/create-transaction")
    public SagaStepResult createTransaction(@RequestBody CreateTransactionRequest request);
    
    @PostMapping("/cancel-transaction")
    public SagaStepResult cancelTransaction(@RequestBody CancelTransactionRequest request);
    
    @PostMapping("/create-return")
    public SagaStepResult createReturn(@RequestBody CreateReturnRequest request);
    
    @GetMapping("/validate-transaction/{id}")
    public SagaStepResult validateTransaction(@PathVariable Long id);
}
```

### 5.3.3. Personnel Service - Endpoints Saga

**Nouveaux endpoints pour l'orchestration :**

```java
@RestController
@RequestMapping("/saga")
public class PersonnelSagaController {
    
    @GetMapping("/validate-employee/{id}")
    public SagaStepResult validateEmployee(@PathVariable Long id);
    
    @PostMapping("/invalidate-session")
    public SagaStepResult invalidateSession(@RequestBody InvalidateSessionRequest request);
}
```

## 5.4. Communication et patterns

### 5.4.1. Pattern de communication Saga

```plantuml
@startuml saga-communication-pattern
participant "API Gateway" as Gateway
participant "Saga Orchestrator" as Orchestrator
participant "Inventory Service" as Inventory
participant "Transaction Service" as Transaction
participant "saga_db" as SagaDB

Gateway -> Orchestrator : POST /api/saga/sales
activate Orchestrator

Orchestrator -> SagaDB : Créer saga + étapes
Orchestrator -> Orchestrator : Démarrer State Machine

== Étape 1: Réserver Stock ==
Orchestrator -> Inventory : POST /saga/reserve-stock
activate Inventory
Inventory -> Inventory : Réserver stock
Inventory -> Orchestrator : StepResult.success()
deactivate Inventory

Orchestrator -> SagaDB : Mettre à jour étape 1
Orchestrator -> Orchestrator : Transition vers étape 2

== Étape 2: Créer Transaction ==
Orchestrator -> Transaction : POST /saga/create-transaction
activate Transaction
Transaction -> Transaction : Créer transaction
Transaction -> Orchestrator : StepResult.success()
deactivate Transaction

Orchestrator -> SagaDB : Mettre à jour étape 2
Orchestrator -> Orchestrator : Saga terminée

Orchestrator -> Gateway : SagaResponse.completed()
deactivate Orchestrator
@enduml
```

### 5.4.2. Pattern de compensation

```plantuml
@startuml saga-compensation-pattern
participant "Saga Orchestrator" as Orchestrator
participant "Inventory Service" as Inventory
participant "Transaction Service" as Transaction
participant "saga_db" as SagaDB

Orchestrator -> Transaction : POST /saga/create-transaction
activate Transaction
Transaction -> Orchestrator : StepResult.failed("Insufficient funds")
deactivate Transaction

Orchestrator -> SagaDB : Marquer étape comme échouée
Orchestrator -> Orchestrator : Démarrer compensation

== Compensation: Libérer Stock ==
Orchestrator -> Inventory : POST /saga/release-stock
activate Inventory
Inventory -> Inventory : Libérer stock réservé
Inventory -> Orchestrator : StepResult.success()
deactivate Inventory

Orchestrator -> SagaDB : Marquer compensation terminée
Orchestrator -> Orchestrator : Saga compensée
@enduml
```

## 5.5. Persistance et données

### 5.5.1. Modèle de données saga_db

```sql
-- Schema principal pour les sagas
CREATE SCHEMA saga;

-- Table des sagas avec partitioning
CREATE TABLE saga.sagas (
    id BIGSERIAL PRIMARY KEY,
    saga_type VARCHAR(50) NOT NULL,
    saga_status VARCHAR(20) NOT NULL,
    current_step INTEGER DEFAULT 0,
    total_steps INTEGER NOT NULL,
    payload JSONB NOT NULL,
    correlation_id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    error_message TEXT,
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    timeout_at TIMESTAMP
) PARTITION BY RANGE (created_at);

-- Partitions mensuelles
CREATE TABLE saga.sagas_2024_01 PARTITION OF saga.sagas
    FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');

-- Table des étapes
CREATE TABLE saga.saga_steps (
    id BIGSERIAL PRIMARY KEY,
    saga_id BIGINT REFERENCES saga.sagas(id) ON DELETE CASCADE,
    step_number INTEGER NOT NULL,
    step_name VARCHAR(100) NOT NULL,
    step_status VARCHAR(20) NOT NULL,
    service_name VARCHAR(50) NOT NULL,
    service_url VARCHAR(200) NOT NULL,
    http_method VARCHAR(10) NOT NULL,
    request_payload JSONB,
    response_payload JSONB,
    compensation_url VARCHAR(200),
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    error_message TEXT,
    retry_count INTEGER DEFAULT 0,
    timeout_seconds INTEGER DEFAULT 30,
    UNIQUE(saga_id, step_number)
);

-- Table d'événements pour l'audit
CREATE TABLE saga.saga_events (
    id BIGSERIAL PRIMARY KEY,
    saga_id BIGINT REFERENCES saga.sagas(id),
    event_type VARCHAR(50) NOT NULL,
    event_data JSONB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) DEFAULT 'system'
);

-- Index pour les performances
CREATE INDEX idx_sagas_status_created ON saga.sagas(saga_status, created_at);
CREATE INDEX idx_sagas_correlation_id ON saga.sagas(correlation_id);
CREATE INDEX idx_saga_steps_saga_id ON saga.saga_steps(saga_id);
CREATE INDEX idx_saga_events_saga_id ON saga.saga_events(saga_id, created_at);
```

### 5.5.2. Distribution des données

```plantuml
@startuml data-distribution-saga
database "saga_db" as SagaDB {
  table "sagas" as SagasTable
  table "saga_steps" as StepsTable  
  table "saga_events" as EventsTable
}

database "inventory_db" as InventoryDB {
  table "inventory_items" as InventoryTable
  table "stock_reservations" as ReservationsTable
}

database "transaction_db" as TransactionDB {
  table "transactions" as TransactionsTable
  table "transaction_items" as TransactionItemsTable
}

[Saga Orchestrator] --> SagaDB
[Inventory Service] --> InventoryDB
[Transaction Service] --> TransactionDB

SagasTable ||--o{ StepsTable
SagasTable ||--o{ EventsTable
InventoryTable ||--o{ ReservationsTable
TransactionsTable ||--o{ TransactionItemsTable
@enduml
```

## 5.6. Monitoring et observabilité

### 5.6.1. Métriques Saga

```java
@Component
public class SagaMetrics {
    
    // Compteurs
    private final Counter sagasStarted;
    private final Counter sagasCompleted;
    private final Counter sagasFailed;
    private final Counter sagasCompensated;
    
    // Timers
    private final Timer sagaDuration;
    private final Timer stepDuration;
    
    // Gauges
    private final Gauge activeSagas;
    private final Gauge pendingSteps;
    
    public SagaMetrics(MeterRegistry registry) {
        this.sagasStarted = Counter.builder("saga.started")
            .tag("type", "all")
            .register(registry);
            
        this.sagaDuration = Timer.builder("saga.duration")
            .register(registry);
            
        this.activeSagas = Gauge.builder("saga.active.count")
            .register(registry, this, SagaMetrics::getActiveSagaCount);
    }
}
```

### 5.6.2. Health Checks

```java
@Component
public class SagaHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        try {
            long activeSagas = sagaRepository.countByStatus(SagaStatus.IN_PROGRESS);
            long failedSagas = sagaRepository.countByStatusAndCreatedAtAfter(
                SagaStatus.FAILED, 
                LocalDateTime.now().minusHours(1)
            );
            
            if (failedSagas > 10) {
                return Health.down()
                    .withDetail("failed_sagas_last_hour", failedSagas)
                    .build();
            }
            
            return Health.up()
                .withDetail("active_sagas", activeSagas)
                .withDetail("failed_sagas_last_hour", failedSagas)
                .build();
                
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
```

## 5.7. Configuration et déploiement

### 5.7.1. Configuration Docker Compose

```yaml
version: '3.8'
services:
  saga-orchestrator:
    build: ./saga-orchestrator-service
    ports:
      - "8085:8085"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/saga_db
      - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://discovery-server:8761/eureka
    depends_on:
      - postgres
      - discovery-server
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8085/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
```

### 5.7.2. Configuration des services

```yaml
# saga-orchestrator application.yml
server:
  port: 8085

spring:
  application:
    name: saga-orchestrator-service
  datasource:
    url: jdbc:postgresql://localhost:5432/saga_db
    username: ${DB_USERNAME:saga_user}
    password: ${DB_PASSWORD:saga_password}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka

saga:
  orchestrator:
    default-timeout: 30s
    max-retries: 3
    cleanup-after-days: 90
    
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
```

Cette architecture Saga enrichit significativement notre système microservices en apportant la cohérence transactionnelle distribuée tout en maintenant les avantages de l'architecture découplée.