# 6. Vue d'exécution - Architecture Saga

## 6.1. Scénarios d'exécution avec orchestration Saga

Cette section décrit les nouveaux scénarios d'exécution intégrant le Saga Orchestrator pour les transactions distribuées.

### 6.1.1. Démarrage du système avec Saga Orchestrator

Le démarrage inclut maintenant le Saga Orchestrator dans la séquence de démarrage.

```plantuml
@startuml system-startup-saga
participant "Docker Compose" as Docker
participant "PostgreSQL" as DB
participant "Redis" as Redis
participant "Discovery Server" as Discovery
participant "Saga Orchestrator" as SagaOrchestrator
participant "API Gateway" as Gateway
participant "Business Services" as Services
participant "Frontend Service" as Frontend

Docker -> DB : Démarrage PostgreSQL (toutes les DB)
activate DB
Docker -> Redis : Démarrage Redis Cache
activate Redis

Docker -> Discovery : Démarrage Eureka Server
activate Discovery
Discovery -> Discovery : Initialisation registre

DB -> Docker : Health check OK (saga_db incluse)
Redis -> Docker : Health check OK
Discovery -> Docker : Health check OK

Docker -> Services : Démarrage services métier
activate Services
Services -> Discovery : Enregistrement services
Services -> DB : Connexion bases dédiées

Docker -> SagaOrchestrator : Démarrage Saga Orchestrator
activate SagaOrchestrator
SagaOrchestrator -> Discovery : Enregistrement orchestrator
SagaOrchestrator -> DB : Connexion saga_db
SagaOrchestrator -> SagaOrchestrator : Initialisation State Machines
SagaOrchestrator -> Services : Test connectivité services

Docker -> Gateway : Démarrage API Gateway
activate Gateway
Gateway -> Discovery : Enregistrement gateway
Gateway -> Redis : Configuration cache
Gateway -> Services : Test connectivité
Gateway -> SagaOrchestrator : Test connectivité saga

Docker -> Frontend : Démarrage Frontend Service
activate Frontend
Frontend -> Discovery : Enregistrement frontend
Frontend -> Gateway : Test connectivité

note over Docker, Frontend : Système opérationnel avec orchestration Saga
@enduml
```

**Ordre de démarrage mis à jour :**
1. **Infrastructure** : PostgreSQL (incluant saga_db) et Redis
2. **Service Discovery** : Eureka Server
3. **Services métier** : Inventory, Transaction, Store, Personnel
4. **Saga Orchestrator** : Orchestration des transactions distribuées
5. **API Gateway** : Routage et sécurité
6. **Frontend Service** : Interface utilisateur

### 6.1.2. Vente avec orchestration Saga (Succès)

Processus complet d'une vente orchestrée par le Saga Orchestrator.

```plantuml
@startuml sales-saga-success
actor "Employé" as Employee
participant "Frontend Service" as Frontend
participant "API Gateway" as Gateway
participant "Saga Orchestrator" as SagaOrchestrator
participant "Personnel Service" as Personnel
participant "Inventory Service" as Inventory
participant "Transaction Service" as Transaction
participant "saga_db" as SagaDB

Employee -> Frontend : POST /admin/sales (Nouvelle vente)
activate Frontend

Frontend -> Gateway : POST /api/saga/sales
activate Gateway

Gateway -> SagaOrchestrator : POST /api/saga/sales
activate SagaOrchestrator

== Initialisation de la Saga ==
SagaOrchestrator -> SagaDB : Créer saga SALES_SAGA
activate SagaDB
SagaDB -> SagaOrchestrator : Saga ID: 12345
deactivate SagaDB

SagaOrchestrator -> SagaOrchestrator : Initialiser State Machine
SagaOrchestrator -> Gateway : Saga démarrée (ID: 12345)

== Étape 1: Validation Employé ==
SagaOrchestrator -> SagaDB : Créer étape "validate-employee"
SagaOrchestrator -> Personnel : GET /saga/validate-employee/{employeeId}
activate Personnel

Personnel -> Personnel : Vérifier employé actif et permissions
Personnel -> SagaOrchestrator : StepResult.success(employeeData)
deactivate Personnel

SagaOrchestrator -> SagaDB : Marquer étape 1 COMPLETED
SagaOrchestrator -> SagaOrchestrator : Transition vers étape 2

== Étape 2: Réservation Stock ==
SagaOrchestrator -> SagaDB : Créer étape "reserve-stock"
SagaOrchestrator -> Inventory : POST /saga/reserve-stock
activate Inventory

Inventory -> Inventory : Vérifier stock disponible
Inventory -> Inventory : Créer réservation temporaire
Inventory -> SagaOrchestrator : StepResult.success(reservationId)
deactivate Inventory

SagaOrchestrator -> SagaDB : Marquer étape 2 COMPLETED
SagaOrchestrator -> SagaOrchestrator : Transition vers étape 3

== Étape 3: Création Transaction ==
SagaOrchestrator -> SagaDB : Créer étape "create-transaction"
SagaOrchestrator -> Transaction : POST /saga/create-transaction
activate Transaction

Transaction -> Transaction : Créer transaction avec statut PENDING
Transaction -> SagaOrchestrator : StepResult.success(transactionId)
deactivate Transaction

SagaOrchestrator -> SagaDB : Marquer étape 3 COMPLETED
SagaOrchestrator -> SagaOrchestrator : Transition vers étape 4

== Étape 4: Confirmation Stock ==
SagaOrchestrator -> SagaDB : Créer étape "confirm-stock"
SagaOrchestrator -> Inventory : POST /saga/confirm-stock
activate Inventory

Inventory -> Inventory : Confirmer réservation et décrémenter stock
Inventory -> SagaOrchestrator : StepResult.success()
deactivate Inventory

SagaOrchestrator -> SagaDB : Marquer étape 4 COMPLETED
SagaOrchestrator -> SagaDB : Marquer saga COMPLETED

== Finalisation ==
SagaOrchestrator -> Gateway : SagaResponse.completed(transactionId)
deactivate SagaOrchestrator

Gateway -> Frontend : Success response
deactivate Gateway

Frontend -> Employee : Page de confirmation vente
deactivate Frontend

note over Employee, SagaDB : Transaction distribuée réussie avec cohérence garantie
@enduml
```

### 6.1.3. Vente avec orchestration Saga (Échec et compensation)

Processus d'une vente qui échoue et déclenche la compensation automatique.

```plantuml
@startuml sales-saga-failure
participant "API Gateway" as Gateway
participant "Saga Orchestrator" as SagaOrchestrator
participant "Personnel Service" as Personnel
participant "Inventory Service" as Inventory
participant "Transaction Service" as Transaction
participant "saga_db" as SagaDB

Gateway -> SagaOrchestrator : POST /api/saga/sales
activate SagaOrchestrator

== Initialisation de la Saga ==
SagaOrchestrator -> SagaDB : Créer saga SALES_SAGA (ID: 12346)
SagaOrchestrator -> SagaOrchestrator : Initialiser State Machine

== Étape 1: Validation Employé (Succès) ==
SagaOrchestrator -> Personnel : GET /saga/validate-employee/{employeeId}
activate Personnel
Personnel -> SagaOrchestrator : StepResult.success()
deactivate Personnel
SagaOrchestrator -> SagaDB : Marquer étape 1 COMPLETED

== Étape 2: Réservation Stock (Succès) ==
SagaOrchestrator -> Inventory : POST /saga/reserve-stock
activate Inventory
Inventory -> SagaOrchestrator : StepResult.success(reservationId: R789)
deactivate Inventory
SagaOrchestrator -> SagaDB : Marquer étape 2 COMPLETED

== Étape 3: Création Transaction (Échec) ==
SagaOrchestrator -> Transaction : POST /saga/create-transaction
activate Transaction
Transaction -> Transaction : Vérification des fonds insuffisants
Transaction -> SagaOrchestrator : StepResult.failed("Insufficient funds")
deactivate Transaction

SagaOrchestrator -> SagaDB : Marquer étape 3 FAILED
SagaOrchestrator -> SagaDB : Changer statut saga vers COMPENSATING
SagaOrchestrator -> SagaOrchestrator : Démarrer processus de compensation

== Compensation Étape 2: Libération Stock ==
SagaOrchestrator -> SagaDB : Créer étape compensation "release-stock"
SagaOrchestrator -> Inventory : POST /saga/release-stock
activate Inventory
note right : Payload: {reservationId: "R789"}

Inventory -> Inventory : Libérer réservation R789
Inventory -> Inventory : Remettre stock disponible
Inventory -> SagaOrchestrator : StepResult.success()
deactivate Inventory

SagaOrchestrator -> SagaDB : Marquer compensation 2 COMPLETED

== Compensation Étape 1: Invalidation Session ==
SagaOrchestrator -> Personnel : POST /saga/invalidate-session
activate Personnel
Personnel -> SagaOrchestrator : StepResult.success()
deactivate Personnel

SagaOrchestrator -> SagaDB : Marquer compensation 1 COMPLETED
SagaOrchestrator -> SagaDB : Changer statut saga vers COMPENSATED

== Finalisation ==
SagaOrchestrator -> Gateway : SagaResponse.failed("Insufficient funds")
deactivate SagaOrchestrator

note over Gateway, SagaDB : Saga compensée - système cohérent malgré l'échec
@enduml
```

### 6.1.4. Retour de marchandise avec Saga

Processus de retour orchestré par le Saga Orchestrator.

```plantuml
@startuml return-saga-process
actor "Employé" as Employee
participant "Frontend Service" as Frontend
participant "API Gateway" as Gateway
participant "Saga Orchestrator" as SagaOrchestrator
participant "Transaction Service" as Transaction
participant "Inventory Service" as Inventory
participant "saga_db" as SagaDB

Employee -> Frontend : POST /admin/returns (Retour marchandise)
activate Frontend

Frontend -> Gateway : POST /api/saga/returns
activate Gateway

Gateway -> SagaOrchestrator : POST /api/saga/returns
activate SagaOrchestrator

== Initialisation Return Saga ==
SagaOrchestrator -> SagaDB : Créer saga RETURN_SAGA
SagaOrchestrator -> SagaOrchestrator : Initialiser Return State Machine

== Étape 1: Validation Transaction Originale ==
SagaOrchestrator -> SagaDB : Créer étape "validate-original-transaction"
SagaOrchestrator -> Transaction : GET /saga/validate-transaction/{originalTransactionId}
activate Transaction

Transaction -> Transaction : Vérifier transaction existe et éligible au retour
Transaction -> Transaction : Vérifier délai de retour respecté
Transaction -> SagaOrchestrator : StepResult.success(transactionDetails)
deactivate Transaction

SagaOrchestrator -> SagaDB : Marquer étape 1 COMPLETED

== Étape 2: Création Transaction Retour ==
SagaOrchestrator -> SagaDB : Créer étape "create-return-transaction"
SagaOrchestrator -> Transaction : POST /saga/create-return
activate Transaction

Transaction -> Transaction : Créer transaction de type RETOUR
Transaction -> Transaction : Lier à la transaction originale
Transaction -> SagaOrchestrator : StepResult.success(returnTransactionId)
deactivate Transaction

SagaOrchestrator -> SagaDB : Marquer étape 2 COMPLETED

== Étape 3: Remise en Stock ==
SagaOrchestrator -> SagaDB : Créer étape "restore-stock"
SagaOrchestrator -> Inventory : POST /saga/restore-stock
activate Inventory

Inventory -> Inventory : Incrémenter stock des articles retournés
Inventory -> Inventory : Créer mouvement de stock RETOUR
Inventory -> SagaOrchestrator : StepResult.success()
deactivate Inventory

SagaOrchestrator -> SagaDB : Marquer étape 3 COMPLETED
SagaOrchestrator -> SagaDB : Marquer saga COMPLETED

== Finalisation ==
SagaOrchestrator -> Gateway : SagaResponse.completed(returnTransactionId)
deactivate SagaOrchestrator

Gateway -> Frontend : Success response
deactivate Gateway

Frontend -> Employee : Page de confirmation retour
deactivate Frontend

note over Employee, SagaDB : Retour traité avec cohérence transactionnelle
@enduml
```

### 6.1.5. Monitoring et observabilité des Sagas

Processus de monitoring en temps réel des sagas en cours.

```plantuml
@startuml saga-monitoring
participant "Grafana Dashboard" as Grafana
participant "Prometheus" as Prometheus
participant "Saga Orchestrator" as SagaOrchestrator
participant "Admin API" as AdminAPI
participant "saga_db" as SagaDB

== Collecte de métriques ==
loop Collecte périodique (15s)
    Prometheus -> SagaOrchestrator : GET /actuator/prometheus
    activate SagaOrchestrator
    
    SagaOrchestrator -> SagaDB : Requêtes métriques
    activate SagaDB
    SagaDB -> SagaOrchestrator : Données agrégées
    deactivate SagaDB
    
    SagaOrchestrator -> Prometheus : Métriques formatées
    deactivate SagaOrchestrator
    
    note right of Prometheus
      Métriques collectées:
      - saga_started_total
      - saga_completed_total
      - saga_failed_total
      - saga_compensated_total
      - saga_duration_seconds
      - saga_active_count
      - saga_step_duration_seconds
    end note
end

== Visualisation temps réel ==
Grafana -> Prometheus : Query métriques saga
activate Prometheus
Prometheus -> Grafana : Données temporelles
deactivate Prometheus

Grafana -> Grafana : Génération dashboards
note right of Grafana
  Dashboards:
  - Vue d'ensemble des sagas
  - Performance par type
  - Taux d'échec et compensation
  - Durée moyenne par étape
  - Alertes sur anomalies
end note

== Monitoring détaillé ==
Grafana -> AdminAPI : GET /api/admin/sagas/active
activate AdminAPI

AdminAPI -> SagaDB : SELECT sagas actives avec détails
activate SagaDB
SagaDB -> AdminAPI : Liste des sagas en cours
deactivate SagaDB

AdminAPI -> Grafana : Détails sagas actives
deactivate AdminAPI

== Alerting ==
alt Saga bloquée > 5 minutes
    Prometheus -> Prometheus : Déclencher alerte
    Prometheus -> Grafana : Notification alerte
    Grafana -> Grafana : Envoyer notification équipe
end

alt Taux d'échec > 10%
    Prometheus -> Prometheus : Déclencher alerte critique
    Prometheus -> Grafana : Notification critique
    Grafana -> Grafana : Escalade vers on-call
end
@enduml
```

## 6.2. Patterns d'exécution Saga

### 6.2.1. Pattern de retry avec backoff exponentiel

```plantuml
@startuml saga-retry-pattern
participant "Saga Orchestrator" as SagaOrchestrator
participant "Target Service" as TargetService
participant "Retry Handler" as RetryHandler
participant "saga_db" as SagaDB

SagaOrchestrator -> TargetService : Appel service (tentative 1)
activate TargetService
TargetService -> SagaOrchestrator : Timeout/Error
deactivate TargetService

SagaOrchestrator -> RetryHandler : Gérer retry
activate RetryHandler

RetryHandler -> SagaDB : Incrémenter retry_count
RetryHandler -> RetryHandler : Calculer délai (1s * 2^retry_count)
RetryHandler -> RetryHandler : Attendre 2 secondes

RetryHandler -> TargetService : Appel service (tentative 2)
activate TargetService
TargetService -> RetryHandler : Timeout/Error
deactivate TargetService

RetryHandler -> SagaDB : Incrémenter retry_count
RetryHandler -> RetryHandler : Attendre 4 secondes

RetryHandler -> TargetService : Appel service (tentative 3)
activate TargetService
TargetService -> RetryHandler : Success
deactivate TargetService

RetryHandler -> SagaOrchestrator : StepResult.success()
deactivate RetryHandler

note over SagaOrchestrator, SagaDB : Retry réussi après 3 tentatives
@enduml
```

### 6.2.2. Pattern de timeout et circuit breaker

```plantuml
@startuml saga-circuit-breaker
participant "Saga Orchestrator" as SagaOrchestrator
participant "Circuit Breaker" as CircuitBreaker
participant "Target Service" as TargetService
participant "Fallback Handler" as FallbackHandler

SagaOrchestrator -> CircuitBreaker : Appel via circuit breaker
activate CircuitBreaker

CircuitBreaker -> CircuitBreaker : Vérifier état circuit (CLOSED)
CircuitBreaker -> TargetService : Forward request
activate TargetService

TargetService -> TargetService : Service surchargé
TargetService -> CircuitBreaker : Timeout (après 30s)
deactivate TargetService

CircuitBreaker -> CircuitBreaker : Incrémenter compteur d'erreurs
CircuitBreaker -> CircuitBreaker : Seuil atteint - Circuit OPEN

CircuitBreaker -> FallbackHandler : Déclencher fallback
activate FallbackHandler

FallbackHandler -> FallbackHandler : Stratégie de fallback
note right of FallbackHandler
  Options de fallback:
  - Réessayer plus tard
  - Utiliser données cached
  - Marquer étape comme skipped
  - Déclencher compensation
end note

FallbackHandler -> CircuitBreaker : Fallback response
deactivate FallbackHandler

CircuitBreaker -> SagaOrchestrator : StepResult.failed("Circuit breaker open")
deactivate CircuitBreaker

SagaOrchestrator -> SagaOrchestrator : Décider compensation ou retry
@enduml
```

### 6.2.3. Pattern de saga parallèle

```plantuml
@startuml saga-parallel-execution
participant "Saga Orchestrator" as SagaOrchestrator
participant "Inventory Service" as Inventory
participant "Store Service" as Store
participant "Personnel Service" as Personnel
participant "saga_db" as SagaDB

SagaOrchestrator -> SagaOrchestrator : Démarrer étapes parallèles

== Exécution parallèle ==
par
    SagaOrchestrator -> Inventory : POST /saga/check-stock
    activate Inventory
else
    SagaOrchestrator -> Store : GET /saga/store-info
    activate Store
else
    SagaOrchestrator -> Personnel : GET /saga/employee-schedule
    activate Personnel
end

== Attente des résultats ==
Inventory -> SagaOrchestrator : StepResult.success()
deactivate Inventory

Store -> SagaOrchestrator : StepResult.success()
deactivate Store

Personnel -> SagaOrchestrator : StepResult.success()
deactivate Personnel

== Synchronisation ==
SagaOrchestrator -> SagaDB : Marquer toutes les étapes COMPLETED
SagaOrchestrator -> SagaOrchestrator : Continuer vers étape suivante

note over SagaOrchestrator, SagaDB : Étapes parallèles terminées simultanément
@enduml
```

## 6.3. Gestion des erreurs et récupération

### 6.3.1. Récupération après panne de l'orchestrateur

```plantuml
@startuml saga-recovery-after-crash
participant "Saga Orchestrator" as SagaOrchestrator
participant "Recovery Service" as RecoveryService
participant "saga_db" as SagaDB
participant "Target Services" as Services

note over SagaOrchestrator : Orchestrateur redémarre après panne

SagaOrchestrator -> RecoveryService : Démarrage recovery process
activate RecoveryService

RecoveryService -> SagaDB : SELECT sagas WHERE status IN ('STARTED', 'IN_PROGRESS')
activate SagaDB
SagaDB -> RecoveryService : Liste des sagas interrompues
deactivate SagaDB

loop Pour chaque saga interrompue
    RecoveryService -> SagaDB : SELECT saga_steps WHERE saga_id = ? ORDER BY step_number
    activate SagaDB
    SagaDB -> RecoveryService : Étapes de la saga
    deactivate SagaDB
    
    RecoveryService -> RecoveryService : Analyser dernière étape complétée
    
    alt Étape en cours identifiée
        RecoveryService -> Services : Vérifier état de l'étape
        activate Services
        Services -> RecoveryService : État actuel
        deactivate Services
        
        alt Étape complétée côté service
            RecoveryService -> SagaDB : Marquer étape COMPLETED
            RecoveryService -> SagaOrchestrator : Continuer saga
        else Étape échouée
            RecoveryService -> SagaOrchestrator : Déclencher compensation
        else Étape en cours
            RecoveryService -> SagaOrchestrator : Reprendre exécution
        end
    else Saga dans état incohérent
        RecoveryService -> SagaOrchestrator : Déclencher compensation complète
    end
end

RecoveryService -> SagaOrchestrator : Recovery terminé
deactivate RecoveryService

note over SagaOrchestrator, SagaDB : Toutes les sagas interrompues récupérées
@enduml
```

### 6.3.2. Gestion des timeouts de saga

```plantuml
@startuml saga-timeout-handling
participant "Timeout Scheduler" as Scheduler
participant "Saga Orchestrator" as SagaOrchestrator
participant "saga_db" as SagaDB
participant "Alert System" as AlertSystem

loop Vérification périodique (1 minute)
    Scheduler -> SagaDB : SELECT sagas WHERE timeout_at < NOW() AND status = 'IN_PROGRESS'
    activate SagaDB
    SagaDB -> Scheduler : Sagas en timeout
    deactivate SagaDB
    
    loop Pour chaque saga en timeout
        Scheduler -> SagaOrchestrator : Traiter timeout saga
        activate SagaOrchestrator
        
        SagaOrchestrator -> SagaDB : Vérifier dernière activité
        activate SagaDB
        SagaDB -> SagaOrchestrator : Détails dernière étape
        deactivate SagaDB
        
        alt Timeout récupérable
            SagaOrchestrator -> SagaOrchestrator : Retry étape courante
            SagaOrchestrator -> SagaDB : Étendre timeout
        else Timeout définitif
            SagaOrchestrator -> SagaOrchestrator : Déclencher compensation
            SagaOrchestrator -> SagaDB : Marquer saga FAILED
            SagaOrchestrator -> AlertSystem : Envoyer alerte
            activate AlertSystem
            AlertSystem -> AlertSystem : Notifier équipe ops
            deactivate AlertSystem
        end
        
        deactivate SagaOrchestrator
    end
end

note over Scheduler, AlertSystem : Surveillance continue des timeouts
@enduml
```

## 6.4. Performance et scalabilité

### 6.4.1. Exécution concurrente de sagas

```plantuml
@startuml saga-concurrent-execution
participant "Load Balancer" as LB
participant "Saga Orchestrator 1" as Saga1
participant "Saga Orchestrator 2" as Saga2
participant "saga_db" as SagaDB
participant "Services" as Services

== Répartition de charge ==
LB -> Saga1 : Saga A (Sales)
activate Saga1
LB -> Saga2 : Saga B (Return)
activate Saga2
LB -> Saga1 : Saga C (Sales)
LB -> Saga2 : Saga D (Transfer)

== Exécution parallèle ==
par
    Saga1 -> SagaDB : Traiter Saga A
    Saga1 -> Services : Étapes Saga A
else
    Saga2 -> SagaDB : Traiter Saga B
    Saga2 -> Services : Étapes Saga B
else
    Saga1 -> SagaDB : Traiter Saga C
    Saga1 -> Services : Étapes Saga C
else
    Saga2 -> SagaDB : Traiter Saga D
    Saga2 -> Services : Étapes Saga D
end

== Synchronisation base de données ==
note over SagaDB
  Isolation des transactions:
  - Chaque saga dans sa propre transaction
  - Locks optimistes sur les étapes
  - Partitioning par date pour performance
end note

Saga1 -> LB : Sagas A et C terminées
deactivate Saga1
Saga2 -> LB : Sagas B et D terminées
deactivate Saga2

note over LB, Services : Exécution concurrente sans conflit
@enduml
```

### 6.4.2. Optimisation des performances

```plantuml
@startuml saga-performance-optimization
participant "Saga Orchestrator" as SagaOrchestrator
participant "Connection Pool" as ConnectionPool
participant "HTTP Client Pool" as HttpPool
participant "Cache Layer" as Cache
participant "Async Executor" as AsyncExecutor

== Optimisations de connectivité ==
SagaOrchestrator -> ConnectionPool : Obtenir connexion DB
activate ConnectionPool
ConnectionPool -> SagaOrchestrator : Connexion réutilisée
deactivate ConnectionPool

SagaOrchestrator -> HttpPool : Obtenir client HTTP
activate HttpPool
HttpPool -> SagaOrchestrator : Client avec keep-alive
deactivate HttpPool

== Optimisations de cache ==
SagaOrchestrator -> Cache : Vérifier définition saga cached
activate Cache
Cache -> SagaOrchestrator : Définition en cache
deactivate Cache

== Exécution asynchrone ==
SagaOrchestrator -> AsyncExecutor : Soumettre étapes parallèles
activate AsyncExecutor

par
    AsyncExecutor -> AsyncExecutor : Étape 1 (thread 1)
else
    AsyncExecutor -> AsyncExecutor : Étape 2 (thread 2)
else
    AsyncExecutor -> AsyncExecutor : Étape 3 (thread 3)
end

AsyncExecutor -> SagaOrchestrator : Toutes les étapes terminées
deactivate AsyncExecutor

== Optimisations de persistance ==
SagaOrchestrator -> SagaOrchestrator : Batch updates
note right of SagaOrchestrator
  Optimisations:
  - Batch insert des étapes
  - Prepared statements
  - Connection pooling
  - Async logging
  - Métriques en mémoire
end note

note over SagaOrchestrator, AsyncExecutor : Performance optimisée pour haute charge
@enduml
```

Cette vue d'exécution montre comment l'architecture Saga transforme les interactions système en apportant la cohérence transactionnelle distribuée tout en maintenant les performances et la résilience du système microservices.