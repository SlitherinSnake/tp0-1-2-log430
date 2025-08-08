# 5. Vue des Blocs de Construction - Architecture Événementielle

Cette section décrit la structure statique de l'architecture événementielle, ses composants principaux et leurs interfaces.

## 5.1. Vue d'Ensemble du Système

```mermaid
graph TB
    subgraph "Niveau 1 - Système Complet"
        SYS[Système de Gestion Magasin<br/>Architecture Événementielle]
    end
    
    subgraph "Acteurs Externes"
        USER[Utilisateurs]
        BANK[Système Bancaire]
        SUPPLIER[Fournisseurs]
        ANALYTICS[Systèmes Analytics]
    end
    
    USER --> SYS
    SYS --> BANK
    SYS --> SUPPLIER
    SYS --> ANALYTICS
```

## 5.2. Niveau 1 - Contenants Principaux

```mermaid
graph TB
    subgraph "Frontend & Gateway"
        FE[Frontend Service]
        GW[API Gateway]
        DISC[Discovery Server]
    end
    
    subgraph "Event-Driven Core"
        EB[Event Bus]
        ES[Event Store]
        AS[Audit Service]
    end
    
    subgraph "Business Services"
        TS[Transaction Service]
        PS[Payment Service]
        IS[Inventory Service]
        SS[Store Service]
    end
    
    subgraph "Legacy Support"
        SO[Saga Orchestrator]
    end
    
    subgraph "Infrastructure"
        DB[(Databases)]
        CACHE[(Cache)]
        QUEUE[(Message Queue)]
    end
    
    subgraph "Observability"
        MON[Monitoring Stack]
    end
    
    FE --> GW
    GW --> DISC
    
    TS --> EB
    PS --> EB
    IS --> EB
    SS --> EB
    SO --> EB
    
    EB --> ES
    EB --> AS
    
    ES --> DB
    AS --> DB
    TS --> DB
    PS --> DB
    IS --> DB
    SS --> DB
    
    GW --> CACHE
    EB --> QUEUE
    
    TS --> MON
    PS --> MON
    IS --> MON
    SS --> MON
    ES --> MON
    AS --> MON
```

## 5.3. Niveau 2 - Services Métier Détaillés

### 5.3.1. Transaction Service (Étendu)

```mermaid
graph TB
    subgraph "Transaction Service"
        subgraph "API Layer"
            TC[Transaction Controller]
            TCC[Transaction Command Controller]
            TQC[Transaction Query Controller]
        end
        
        subgraph "Application Layer"
            TCS[Transaction Command Service]
            TQS[Transaction Query Service]
            TSS[Transaction Saga Service]
            TCMP[Transaction Compensation Service]
        end
        
        subgraph "Domain Layer"
            TA[Transaction Aggregate]
            TR[Transaction Repository]
            TE[Transaction Events]
        end
        
        subgraph "Infrastructure Layer"
            TDB[Transaction Database]
            TEMP[Transaction Event Producer]
            TCONS[Transaction Event Consumer]
            TRM[Transaction Read Models]
        end
    end
    
    subgraph "External"
        EB[Event Bus]
        ES[Event Store]
    end
    
    TC --> TCS
    TCC --> TCS
    TQC --> TQS
    
    TCS --> TA
    TQS --> TRM
    TSS --> TCMP
    
    TA --> TR
    TA --> TE
    
    TR --> TDB
    TE --> TEMP
    TEMP --> EB
    EB --> TCONS
    TCONS --> TSS
    
    TCS --> ES
```

**Responsabilités** :

- **Command Side** : Création, modification, annulation des transactions
- **Query Side** : Consultation optimisée des données transactionnelles
- **Event Publishing** : `TransactionCreated`, `TransactionCompleted`, `TransactionCancelled`
- **Event Consumption** : `PaymentProcessed`, `PaymentFailed`, `PaymentRefunded`, `OrderFulfilled`
- **Saga Coordination** : Logique de compensation pour échecs
- **CQRS** : Séparation lecture/écriture avec projections

### 5.3.2. Payment Service (Nouveau)

```mermaid
graph TB
    subgraph "Payment Service"
        subgraph "API Layer"
            PC[Payment Controller]
            PRC[Payment Refund Controller]
        end
        
        subgraph "Application Layer"
            PS[Payment Service]
            PGS[Payment Gateway Service]
            PSS[Payment Saga Service]
        end
        
        subgraph "Domain Layer"
            PA[Payment Aggregate]
            PR[Payment Repository]
            PE[Payment Events]
            PG[Payment Gateway Interface]
        end
        
        subgraph "Infrastructure Layer"
            PDB[Payment Database]
            PGIM[Payment Gateway Mock]
            PEPR[Payment Event Producer]
            PECO[Payment Event Consumer]
        end
    end
    
    subgraph "External"
        EB[Event Bus]
        BANK[Banking System]
    end
    
    PC --> PS
    PRC --> PS
    
    PS --> PA
    PS --> PGS
    
    PA --> PR
    PA --> PE
    
    PGS --> PG
    PG --> PGIM
    PGIM --> BANK
    
    PR --> PDB
    PE --> PEPR
    PEPR --> EB
    EB --> PECO
    PECO --> PSS
```

**Responsabilités** :

- **Payment Processing** : Traitement des paiements via gateway externe
- **Refund Management** : Gestion des remboursements
- **Event Publishing** : `PaymentProcessed`, `PaymentFailed`, `PaymentRefunded`
- **Event Consumption** : `TransactionCreated`, `InventoryUnavailable`
- **Compensation Logic** : Remboursements automatiques sur échec saga
- **Gateway Integration** : Simulation système bancaire

### 5.3.3. Inventory Service (Étendu)

```mermaid
graph TB
    subgraph "Inventory Service"
        subgraph "API Layer"
            IC[Inventory Controller]
            IRC[Inventory Reservation Controller]
        end
        
        subgraph "Application Layer"
            IS[Inventory Service]
            ISS[Inventory Saga Service]
            EDIS[Event-Driven Inventory Service]
        end
        
        subgraph "Domain Layer"
            IA[Inventory Aggregate]
            IIT[Inventory Item]
            SR[Stock Reservation]
            IE[Inventory Events]
        end
        
        subgraph "Infrastructure Layer"
            IDB[Inventory Database]
            IRM[Inventory Read Models]
            IEPR[Inventory Event Producer]
            IECO[Inventory Event Consumer]
        end
    end
    
    subgraph "External"
        EB[Event Bus]
    end
    
    IC --> IS
    IRC --> ISS
    
    IS --> IA
    ISS --> EDIS
    
    IA --> IIT
    IA --> SR
    IA --> IE
    
    IIT --> IDB
    SR --> IDB
    IE --> IEPR
    
    IEPR --> EB
    EB --> IECO
    IECO --> ISS
    
    IS --> IRM
```

**Responsabilités** :

- **Stock Management** : Gestion des niveaux de stock
- **Reservation Logic** : Réservation/libération de stock événementielle
- **Event Publishing** : `InventoryReserved`, `InventoryUnavailable`, `InventoryReleased`
- **Event Consumption** : `PaymentProcessed`
- **CQRS Read Models** : Projections optimisées pour requêtes
- **Saga Participation** : Compensation par libération de réservations

### 5.3.4. Store Service (Étendu)

```mermaid
graph TB
    subgraph "Store Service"
        subgraph "API Layer"
            SC[Store Controller]
            OFC[Order Fulfillment Controller]
        end
        
        subgraph "Application Layer"
            SS[Store Service]
            OFS[Order Fulfillment Service]
            SSS[Store Saga Service]
        end
        
        subgraph "Domain Layer"
            SA[Store Aggregate]
            OF[Order Fulfillment]
            SE[Store Events]
        end
        
        subgraph "Infrastructure Layer"
            SDB[Store Database]
            SEPR[Store Event Producer]
            SECO[Store Event Consumer]
        end
    end
    
    subgraph "External"
        EB[Event Bus]
        LOGISTICS[Logistics Systems]
    end
    
    SC --> SS
    OFC --> OFS
    
    SS --> SA
    OFS --> OF
    SSS --> SA
    
    SA --> SE
    OF --> LOGISTICS
    
    SA --> SDB
    SE --> SEPR
    
    SEPR --> EB
    EB --> SECO
    SECO --> SSS
```

**Responsabilités** :

- **Store Management** : Gestion des informations magasins
- **Order Fulfillment** : Traitement et préparation des commandes
- **Event Publishing** : `OrderFulfilled`, `OrderDelivered`
- **Event Consumption** : `InventoryReserved`
- **Logistics Integration** : Interface avec systèmes de livraison

## 5.4. Niveau 2 - Infrastructure Événementielle

### 5.4.1. Event Store Service

```mermaid
graph TB
    subgraph "Event Store Service"
        subgraph "API Layer"
            ESC[Event Store Controller]
            ERC[Event Replay Controller]
            SRC[State Reconstruction Controller]
        end
        
        subgraph "Application Layer"
            ESS[Event Store Service]
            ERS[Event Replay Service]
            SRS[State Reconstruction Service]
        end
        
        subgraph "Domain Layer"
            EV[Event]
            EA[Event Aggregate]
            EH[Event Handler Interface]
            ES[Event Store Interface]
        end
        
        subgraph "Infrastructure Layer"
            PGES[PostgreSQL Event Store]
            ER[Event Repository]
            SS[Snapshot Service]
        end
    end
    
    subgraph "External"
        EB[Event Bus]
        DB[(PostgreSQL)]
    end
    
    ESC --> ESS
    ERC --> ERS
    SRC --> SRS
    
    ESS --> ES
    ERS --> ES
    SRS --> ES
    
    ES --> PGES
    PGES --> ER
    PGES --> SS
    
    ER --> DB
    SS --> DB
    
    ESS --> EB
```

**Responsabilités** :

- **Event Persistence** : Stockage immuable des événements
- **Event Retrieval** : Récupération par agrégat, type, période
- **Event Replay** : Reconstruction d'état historique
- **Concurrency Control** : Gestion optimiste des versions
- **Snapshots** : Optimisation performance par snapshots
- **APIs REST** : Interface pour services métier

### 5.4.2. Audit Service

```mermaid
graph TB
    subgraph "Audit Service"
        subgraph "API Layer"
            AC[Audit Controller]
            CC[Compliance Controller]
        end
        
        subgraph "Application Layer"
            AS[Audit Service]
            CS[Compliance Service]
        end
        
        subgraph "Domain Layer"
            AL[Audit Log]
            CT[Compliance Tags]
            AR[Audit Repository]
        end
        
        subgraph "Infrastructure Layer"
            ADB[Audit Database]
            AECO[Audit Event Consumer]
            ALG[Audit Logger]
        end
    end
    
    subgraph "External"
        EB[Event Bus]
        EXT[External Compliance Systems]
    end
    
    AC --> AS
    CC --> CS
    
    AS --> AL
    CS --> CT
    
    AL --> AR
    AR --> ADB
    
    EB --> AECO
    AECO --> AS
    
    ALG --> EXT
```

**Responsabilités** :

- **Comprehensive Audit** : Consommation de tous événements métier
- **Compliance Tagging** : Classification automatique (PCI-DSS, GDPR)
- **Structured Logging** : Logs JSON avec contexte complet
- **Compliance Reporting** : Rapports réglementaires
- **Critical Event Alerting** : Notifications temps réel
- **Long-term Retention** : Archivage conforme

### 5.4.3. Event Bus (RabbitMQ)

```mermaid
graph TB
    subgraph "RabbitMQ Event Bus"
        subgraph "Exchanges"
            BE[business.events<br/>Topic Exchange]
            DE[dlx.events<br/>Dead Letter Exchange]
        end
        
        subgraph "Queues"
            TQ[transaction.events.queue]
            PQ[payment.events.queue]
            IQ[inventory.events.queue]
            SQ[store.events.queue]
            AQ[audit.events.queue]
            ESQ[event-store.events.queue]
            DLQ[dead.letter.queue]
        end
        
        subgraph "Routing"
            RK[Routing Keys<br/>service.eventType]
        end
    end
    
    subgraph "Publishers"
        TS[Transaction Service]
        PS[Payment Service]
        IS[Inventory Service]
        SS[Store Service]
    end
    
    subgraph "Consumers"
        TSC[Transaction Consumers]
        PSC[Payment Consumers]
        ISC[Inventory Consumers]
        SSC[Store Consumers]
        ASC[Audit Consumers]
        ESC[Event Store Consumers]
    end
    
    TS --> BE
    PS --> BE
    IS --> BE
    SS --> BE
    
    BE --> TQ
    BE --> PQ
    BE --> IQ
    BE --> SQ
    BE --> AQ
    BE --> ESQ
    
    TQ --> TSC
    PQ --> PSC
    IQ --> ISC
    SQ --> SSC
    AQ --> ASC
    ESQ --> ESC
    
    DE --> DLQ
```

**Configuration** :

- **Exchange** : `business.events` (topic exchange)
- **Routing Keys** : Pattern `service.eventType`
- **Queues** : Durables avec TTL configuré
- **Dead Letter Handling** : DLX pour messages échoués
- **Clustering** : Haute disponibilité multi-nœuds

## 5.5. Interfaces et Contrats

### 5.5.1. Event Schema Standard

```json
{
  "eventId": "uuid",
  "eventType": "string",
  "aggregateId": "string", 
  "aggregateType": "string",
  "timestamp": "ISO-8601",
  "version": "integer",
  "correlationId": "uuid",
  "causationId": "uuid",
  "payload": {
    "// Données spécifiques à l'événement"
  },
  "metadata": {
    "userId": "string",
    "serviceName": "string",
    "sourceIp": "string"
  }
}
```

### 5.5.2. APIs REST Principales

#### Event Store Service APIs

```yaml
/api/events:
  POST: # Sauvegarder un événement
    summary: "Persist a domain event"
    requestBody:
      $ref: '#/components/schemas/Event'
    responses:
      201: 
        description: "Event saved successfully"

/api/events/{aggregateId}:
  GET: # Récupérer événements d'un agrégat
    summary: "Get events for aggregate"
    parameters:
      - name: aggregateId
        in: path
        required: true
        schema:
          type: string
      - name: fromVersion
        in: query
        schema:
          type: integer
    responses:
      200:
        description: "Events retrieved"
        content:
          application/json:
            schema:
              type: array
              items:
                $ref: '#/components/schemas/Event'

/api/replay/aggregate/{aggregateId}:
  POST: # Replay événements
    summary: "Replay events for aggregate"
    responses:
      200:
        description: "Replay completed"
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ReplayResult'
```

#### Audit Service APIs

```yaml
/api/audit/trail/{correlationId}:
  GET: # Récupérer audit trail complet
    summary: "Get complete audit trail"
    parameters:
      - name: correlationId
        in: path
        required: true
        schema:
          type: string
    responses:
      200:
        description: "Audit trail retrieved"

/api/audit/compliance:
  GET: # Récupérer logs de compliance
    summary: "Get compliance logs"
    parameters:
      - name: since
        in: query
        schema:
          type: string
          format: date-time
      - name: tags
        in: query
        schema:
          type: array
          items:
            type: string
    responses:
      200:
        description: "Compliance logs retrieved"
```

### 5.5.3. Event Types Catalog

#### Transaction Events

```typescript
interface TransactionCreated {
  transactionId: string;
  customerId: string;
  storeId: string;
  items: TransactionItem[];
  totalAmount: number;
  currency: string;
  timestamp: Date;
}

interface TransactionCompleted {
  transactionId: string;
  completedAt: Date;
  finalAmount: number;
}

interface TransactionCancelled {
  transactionId: string;
  cancellationReason: string;
  cancelledAt: Date;
}
```

#### Payment Events

```typescript
interface PaymentProcessed {
  paymentId: string;
  transactionId: string;
  customerId: string;
  amount: number;
  paymentMethod: string;
  paymentReference: string;
  processedAt: Date;
}

interface PaymentFailed {
  paymentId: string;
  transactionId: string;
  failureReason: string;
  errorCode: string;
  failedAt: Date;
}

interface PaymentRefunded {
  paymentId: string;
  transactionId: string;
  refundAmount: number;
  refundReason: string;
  refundedAt: Date;
}
```

#### Inventory Events

```typescript
interface InventoryReserved {
  transactionId: string;
  reservations: {
    inventoryItemId: number;
    quantityReserved: number;
    storeId: string;
  }[];
  reservedAt: Date;
}

interface InventoryUnavailable {
  transactionId: string;
  unavailableItems: {
    inventoryItemId: number;
    requestedQuantity: number;
    availableQuantity: number;
    itemName: string;
  }[];
  reason: string;
}

interface InventoryReleased {
  transactionId: string;
  releasedItems: {
    inventoryItemId: number;
    quantityReleased: number;
  }[];
  reason: string;
  releasedAt: Date;
}
```

## 5.6. Patrons de Conception Utilisés

### 5.6.1. Event Sourcing Pattern

- **Event Store** : Source de vérité basée sur les événements
- **Aggregate Root** : Entités produisant des événements
- **Event Handler** : Traitement des événements pour reconstruction d'état

### 5.6.2. CQRS Pattern

- **Command Side** : Services métier pour opérations d'écriture
- **Query Side** : Read models optimisés
- **Projections** : Synchronisation asynchrone via événements

### 5.6.3. Saga Pattern (Chorégraphie)

- **Event-Driven Coordination** : Coordination via événements
- **Compensation Logic** : Logique de rollback par service
- **State Tracking** : Suivi d'état distribué

### 5.6.4. Publisher-Subscriber Pattern

- **Event Publishers** : Services métier publiant des événements
- **Event Subscribers** : Consommateurs spécialisés
- **Message Broker** : RabbitMQ comme médiateur

### 5.6.5. Repository Pattern

- **Event Repository** : Persistance des événements
- **Read Model Repository** : Accès aux projections
- **Audit Repository** : Stockage des logs d'audit

Cette architecture en blocs modulaires permet une évolution indépendante des composants tout en maintenant la cohérence globale du système événementiel.
