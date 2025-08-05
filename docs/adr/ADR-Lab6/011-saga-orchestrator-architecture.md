# ADR 011 – Architecture du Saga Orchestrator Service

## Status

Accepted

## Context

Suite à la décision d'implémenter le Pattern Saga (ADR 010), nous devons définir l'architecture détaillée du **Saga Orchestrator Service**. Ce service devient un composant critique de notre architecture microservices car il coordonne toutes les transactions distribuées.

### Exigences fonctionnelles

- **Orchestration** : Gérer le workflow des transactions distribuées
- **Persistance** : Sauvegarder l'état des sagas pour la reprise après panne
- **Compensation** : Exécuter les rollbacks automatiques
- **Monitoring** : Fournir la visibilité sur l'état des transactions
- **Performance** : Gérer un volume élevé de transactions concurrentes

### Exigences non-fonctionnelles

- **Disponibilité** : 99.9% (service critique)
- **Performance** : < 100ms par étape de saga
- **Scalabilité** : Support de 1000+ sagas concurrentes
- **Résilience** : Reprise automatique après panne
- **Observabilité** : Métriques et logs détaillés

## Decision

Architecture du Saga Orchestrator basée sur **Spring Boot** avec **State Machine** et **Event Sourcing**.

### Architecture générale

```text
┌─────────────────────────────────────────────────────────────┐
│                 Saga Orchestrator Service                   │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐  │
│  │   REST API      │  │   Admin API     │  │  Health     │  │
│  │   (Port 8085)   │  │   (Monitoring)  │  │  Checks     │  │
│  └─────────────────┘  └─────────────────┘  └─────────────┘  │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐  │
│  │ Saga Manager    │  │ Step Executor   │  │ Compensator │  │
│  │ (Orchestration) │  │ (HTTP Calls)    │  │ (Rollback)  │  │
│  └─────────────────┘  └─────────────────┘  └─────────────┘  │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐  │
│  │ State Machine   │  │ Event Store     │  │ Scheduler   │  │
│  │ (Workflow)      │  │ (Audit Trail)   │  │ (Retry)     │  │
│  └─────────────────┘  └─────────────────┘  └─────────────┘  │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐  │
│  │ JPA Repository  │  │ HTTP Client     │  │ Metrics     │  │
│  │ (Persistence)   │  │ (Service Calls) │  │ (Prometheus)│  │
│  └─────────────────┘  └─────────────────┘  └─────────────┘  │
└─────────────────────────────────────────────────────────────┘
                                │
                                ▼
                    ┌──────────────────────┐
                    │    PostgreSQL        │
                    │     saga_db          │
                    └──────────────────────┘
```

### Composants principaux

#### 1. Saga Manager (Orchestrateur principal)

```java
@Service
@Transactional
public class SagaManager {
    
    public SagaExecution startSaga(SagaDefinition definition, Object payload) {
        // Créer une nouvelle instance de saga
        // Persister l'état initial
        // Démarrer la première étape
    }
    
    public void continueExecution(Long sagaId, StepResult result) {
        // Mettre à jour l'état de l'étape courante
        // Déterminer la prochaine étape
        // Exécuter ou compenser selon le résultat
    }
    
    public void compensateSaga(Long sagaId, String reason) {
        // Démarrer le processus de compensation
        // Exécuter les étapes de rollback dans l'ordre inverse
    }
}
```

#### 2. State Machine (Machine à états)

```java
@Configuration
@EnableStateMachine
public class SagaStateMachineConfig {
    
    @Bean
    public StateMachine<SagaState, SagaEvent> buildMachine() {
        StateMachineBuilder.Builder<SagaState, SagaEvent> builder = 
            StateMachineBuilder.builder();
            
        builder.configureStates()
            .withStates()
            .initial(SagaState.STARTED)
            .states(EnumSet.allOf(SagaState.class));
            
        builder.configureTransitions()
            .withExternal()
            .source(SagaState.STARTED).target(SagaState.IN_PROGRESS)
            .event(SagaEvent.STEP_COMPLETED)
            .and()
            .withExternal()
            .source(SagaState.IN_PROGRESS).target(SagaState.COMPENSATING)
            .event(SagaEvent.STEP_FAILED);
            
        return builder.build();
    }
}
```

#### 3. Step Executor (Exécuteur d'étapes)

```java
@Component
public class StepExecutor {
    
    private final WebClient webClient;
    private final RetryTemplate retryTemplate;
    
    public CompletableFuture<StepResult> executeStep(SagaStep step) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return retryTemplate.execute(context -> {
                    return callService(step);
                });
            } catch (Exception e) {
                return StepResult.failed(e.getMessage());
            }
        });
    }
    
    private StepResult callService(SagaStep step) {
        WebClient.ResponseSpec response = webClient
            .method(step.getHttpMethod())
            .uri(step.getServiceUrl())
            .bodyValue(step.getPayload())
            .retrieve();
            
        return response.bodyToMono(StepResult.class).block();
    }
}
```

### Définitions des Sagas

#### Sales Saga Definition

```java
@Component
public class SalesSagaDefinition implements SagaDefinition {
    
    @Override
    public List<SagaStep> getSteps() {
        return Arrays.asList(
            SagaStep.builder()
                .name("validate-employee")
                .serviceUrl("http://personnel-service/saga/validate-employee")
                .compensationUrl("http://personnel-service/saga/invalidate-session")
                .timeout(Duration.ofSeconds(5))
                .build(),
                
            SagaStep.builder()
                .name("reserve-stock")
                .serviceUrl("http://inventory-service/saga/reserve-stock")
                .compensationUrl("http://inventory-service/saga/release-stock")
                .timeout(Duration.ofSeconds(10))
                .build(),
                
            SagaStep.builder()
                .name("create-transaction")
                .serviceUrl("http://transaction-service/saga/create-transaction")
                .compensationUrl("http://transaction-service/saga/cancel-transaction")
                .timeout(Duration.ofSeconds(5))
                .build(),
                
            SagaStep.builder()
                .name("confirm-stock")
                .serviceUrl("http://inventory-service/saga/confirm-stock")
                .compensationUrl("http://inventory-service/saga/restore-stock")
                .timeout(Duration.ofSeconds(5))
                .build()
        );
    }
}
```

### Modèle de données détaillé

```sql
-- Table principale des sagas avec partitioning par date
CREATE TABLE sagas (
    id BIGSERIAL PRIMARY KEY,
    saga_type VARCHAR(50) NOT NULL,
    saga_status VARCHAR(20) NOT NULL,
    current_step INTEGER DEFAULT 0,
    total_steps INTEGER NOT NULL,
    payload JSONB NOT NULL,
    correlation_id UUID UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    error_message TEXT,
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3
) PARTITION BY RANGE (created_at);

-- Partitions mensuelles pour les performances
CREATE TABLE sagas_2024_01 PARTITION OF sagas
    FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');

-- Table des étapes avec index sur saga_id et step_number
CREATE TABLE saga_steps (
    id BIGSERIAL PRIMARY KEY,
    saga_id BIGINT REFERENCES sagas(id) ON DELETE CASCADE,
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
    timeout_seconds INTEGER DEFAULT 30
);

-- Index pour les performances
CREATE INDEX idx_sagas_status_created ON sagas(saga_status, created_at);
CREATE INDEX idx_saga_steps_saga_id_step ON saga_steps(saga_id, step_number);
CREATE INDEX idx_sagas_correlation_id ON sagas(correlation_id);

-- Table d'audit pour l'event sourcing
CREATE TABLE saga_events (
    id BIGSERIAL PRIMARY KEY,
    saga_id BIGINT REFERENCES sagas(id),
    event_type VARCHAR(50) NOT NULL,
    event_data JSONB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100)
);
```

### Configuration et propriétés

```yaml
# application.yml
saga:
  orchestrator:
    # Configuration des timeouts
    default-timeout: 30s
    max-timeout: 300s
    
    # Configuration des retries
    max-retries: 3
    retry-delay: 1s
    retry-multiplier: 2.0
    
    # Configuration du pool de threads
    executor:
      core-pool-size: 10
      max-pool-size: 50
      queue-capacity: 1000
    
    # Configuration du monitoring
    metrics:
      enabled: true
      export-interval: 30s
    
    # Configuration de la persistence
    persistence:
      batch-size: 100
      cleanup-after-days: 90

# Configuration des services cibles
services:
  inventory:
    base-url: http://inventory-service
    timeout: 10s
  transaction:
    base-url: http://transaction-service
    timeout: 5s
  personnel:
    base-url: http://personnel-service
    timeout: 5s
  store:
    base-url: http://store-service
    timeout: 5s
```

## Justification

### Choix technologiques

#### Spring State Machine

- **Workflow explicite** : Définition claire des transitions
- **Persistance d'état** : Sauvegarde automatique des états
- **Event-driven** : Réaction aux événements métier
- **Extensibilité** : Facile d'ajouter de nouveaux états

#### Event Sourcing partiel

- **Audit trail** : Historique complet des événements
- **Debugging** : Reconstruction de l'état à tout moment
- **Compliance** : Traçabilité pour les audits
- **Analytics** : Analyse des patterns d'exécution

#### WebClient asynchrone

- **Performance** : Appels non-bloquants
- **Scalabilité** : Gestion de nombreuses requêtes concurrentes
- **Résilience** : Timeouts et circuit breakers intégrés

### Patterns de résilience

#### 1. Retry Pattern

```java
@Bean
public RetryTemplate retryTemplate() {
    return RetryTemplate.builder()
        .maxAttempts(3)
        .exponentialBackoff(1000, 2, 10000)
        .retryOn(ConnectException.class)
        .retryOn(ReadTimeoutException.class)
        .build();
}
```

#### 2. Circuit Breaker Pattern

```java
@Bean
public CircuitBreaker circuitBreaker() {
    return CircuitBreaker.ofDefaults("saga-orchestrator")
        .toBuilder()
        .failureRateThreshold(50)
        .waitDurationInOpenState(Duration.ofSeconds(30))
        .slidingWindowSize(10)
        .build();
}
```

#### 3. Timeout Pattern

```java
@Bean
public WebClient webClient() {
    return WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(
            HttpClient.create()
                .responseTimeout(Duration.ofSeconds(30))
        ))
        .build();
}
```

## Consequences

### Avantages

- **Centralisation** : Point unique de contrôle des transactions distribuées
- **Observabilité** : Visibilité complète sur l'état des sagas
- **Résilience** : Gestion automatique des pannes et retries
- **Performance** : Exécution asynchrone et parallèle
- **Maintenabilité** : Code structuré et testable
- **Évolutivité** : Facile d'ajouter de nouveaux types de sagas

### Inconvénients

- **Complexité** : Service supplémentaire à maintenir
- **Point de défaillance** : Criticité élevée du service
- **Latence** : Overhead des appels d'orchestration
- **Ressources** : Consommation mémoire et CPU
- **Dépendances** : Couplage avec tous les services métier

### Risques et mitigations

- **Panne de l'orchestrateur** : Déploiement en haute disponibilité
- **Surcharge** : Monitoring et auto-scaling
- **Corruption de données** : Backups réguliers et tests de récupération
- **Deadlocks** : Timeouts appropriés et détection de cycles

## Monitoring et métriques

### Métriques clés

```java
@Component
public class SagaMetrics {
    
    private final Counter sagasStarted;
    private final Counter sagasCompleted;
    private final Counter sagasFailed;
    private final Timer sagaDuration;
    private final Gauge activeSagas;
    
    public SagaMetrics(MeterRegistry meterRegistry) {
        this.sagasStarted = Counter.builder("sagas.started")
            .tag("type", "all")
            .register(meterRegistry);
            
        this.sagaDuration = Timer.builder("sagas.duration")
            .register(meterRegistry);
            
        this.activeSagas = Gauge.builder("sagas.active")
            .register(meterRegistry, this, SagaMetrics::getActiveSagaCount);
    }
}
```

### Dashboards Grafana

- **Vue d'ensemble** : Sagas actives, taux de succès, durée moyenne
- **Performance** : Latence par étape, throughput
- **Erreurs** : Taux d'échec par type, causes principales
- **Ressources** : CPU, mémoire, connexions DB

## Tests et validation

### Tests unitaires

- **State Machine** : Transitions d'états
- **Step Executor** : Appels de services
- **Compensation** : Logique de rollback

### Tests d'intégration

- **End-to-end** : Sagas complètes avec services réels
- **Failure scenarios** : Pannes simulées
- **Performance** : Tests de charge

### Tests de chaos

- **Network partitions** : Isolation de services
- **Service failures** : Pannes aléatoires
- **Database failures** : Perte de connexion

## Déploiement et opérations

### Configuration Docker

```dockerfile
FROM openjdk:17-jre-slim

COPY target/saga-orchestrator-service.jar app.jar

EXPOSE 8085

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s \
  CMD curl -f http://localhost:8085/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Configuration Kubernetes

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: saga-orchestrator
spec:
  replicas: 2
  selector:
    matchLabels:
      app: saga-orchestrator
  template:
    spec:
      containers:
      - name: saga-orchestrator
        image: saga-orchestrator:latest
        ports:
        - containerPort: 8085
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8085
          initialDelaySeconds: 60
          periodSeconds: 30
```

## Évolution future

### Phase 2 : Optimisations

- **Saga Choreography** : Pour certains cas d'usage
- **Event Streaming** : Kafka pour les événements
- **Caching** : Redis pour les états fréquents

### Phase 3 : Avancé

- **Machine Learning** : Prédiction des échecs
- **Auto-healing** : Récupération automatique
- **Multi-tenant** : Support de plusieurs environnements
