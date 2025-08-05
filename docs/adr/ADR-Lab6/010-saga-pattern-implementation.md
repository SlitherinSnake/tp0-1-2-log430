# ADR 010 – Implémentation du Pattern Saga pour les Transactions Distribuées

## Status

Accepted

## Context

Avec l'architecture microservices mise en place au Lab5, nous avons identifié un problème critique : la gestion des **transactions distribuées**. Les opérations métier qui impliquent plusieurs services (comme une vente qui nécessite la mise à jour du stock ET l'enregistrement de la transaction) ne peuvent plus bénéficier des transactions ACID traditionnelles d'une base de données unique.

### Problèmes identifiés

- **Cohérence des données** : Risque d'incohérence entre les services en cas de panne partielle
- **Transactions atomiques** : Impossible d'utiliser les transactions ACID cross-services
- **Gestion des échecs** : Pas de mécanisme de rollback automatique
- **Complexité opérationnelle** : Gestion manuelle des états intermédiaires
- **Monitoring** : Difficulté à tracer les transactions distribuées

### Exemple de problème

Lors d'une vente :
1. **Transaction Service** enregistre la vente 
2. **Inventory Service** doit décrémenter le stock  (panne)
3. **Résultat** : Vente enregistrée mais stock non mis à jour = incohérence

## Decision

Implémentation du **Pattern Saga** avec un **orchestrateur centralisé** pour gérer les transactions distribuées.

### Architecture Saga

```text
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│  Client Request │───▶│ Saga Orchestrator│───▶│ Business Logic  │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                │
                                ▼
                    ┌──────────────────────┐
                    │   Saga State Store   │
                    │   (PostgreSQL)       │
                    └──────────────────────┘
                                │
                                ▼
                    ┌──────────────────────┐
                    │    Microservices     │
                    │ ┌─────────────────┐  │
                    │ │ Inventory       │  │
                    │ │ Transaction     │  │
                    │ │ Store           │  │
                    │ │ Personnel       │  │
                    │ └─────────────────┘  │
                    └──────────────────────┘
```

### Nouveau service : Saga Orchestrator

- **Port** : 8085
- **Responsabilité** : Orchestration des transactions distribuées
- **Base de données** : saga_db (état des sagas)
- **Pattern** : Orchestration centralisée

### Types de Saga implémentés

#### 1. Saga de Vente (SalesSaga)

**Étapes :**
1. Valider l'employé (Personnel Service)
2. Vérifier le stock (Inventory Service)
3. Réserver le stock (Inventory Service)
4. Créer la transaction (Transaction Service)
5. Confirmer la vente (Inventory Service)

**Compensations :**
1. Libérer le stock réservé
2. Annuler la transaction
3. Notifier l'échec

#### 2. Saga de Retour (ReturnSaga)

**Étapes :**
1. Valider la transaction originale (Transaction Service)
2. Créer la transaction de retour (Transaction Service)
3. Remettre en stock (Inventory Service)
4. Confirmer le retour

**Compensations :**
1. Annuler la remise en stock
2. Annuler la transaction de retour

## Justification

### Avantages du Pattern Saga

- **Cohérence à terme** : Garantit la cohérence finale des données
- **Résilience** : Gestion automatique des pannes et rollbacks
- **Traçabilité** : Historique complet des transactions distribuées
- **Scalabilité** : Pas de verrous distribués
- **Monitoring** : Visibilité sur l'état des transactions longues

### Choix de l'orchestration vs chorégraphie

**Orchestration choisie** car :
- **Contrôle centralisé** : Plus facile à déboguer et monitorer
- **Logique métier claire** : Workflow explicite
- **Gestion d'erreurs simplifiée** : Point unique de gestion
- **Évolutivité** : Facile d'ajouter de nouvelles étapes

## Implementation

### Modèle de données Saga

```sql
-- Table principale des sagas
CREATE TABLE sagas (
    id BIGSERIAL PRIMARY KEY,
    saga_type VARCHAR(50) NOT NULL,
    saga_status VARCHAR(20) NOT NULL,
    current_step INTEGER DEFAULT 0,
    total_steps INTEGER NOT NULL,
    payload JSONB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    error_message TEXT
);

-- Table des étapes de saga
CREATE TABLE saga_steps (
    id BIGSERIAL PRIMARY KEY,
    saga_id BIGINT REFERENCES sagas(id),
    step_number INTEGER NOT NULL,
    step_name VARCHAR(100) NOT NULL,
    step_status VARCHAR(20) NOT NULL,
    service_name VARCHAR(50) NOT NULL,
    request_payload JSONB,
    response_payload JSONB,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    error_message TEXT
);
```

### États des Sagas

```java
public enum SagaStatus {
    STARTED,      // Saga démarrée
    IN_PROGRESS,  // En cours d'exécution
    COMPLETED,    // Terminée avec succès
    COMPENSATING, // En cours de compensation
    COMPENSATED,  // Compensation terminée
    FAILED        // Échec définitif
}

public enum StepStatus {
    PENDING,      // En attente
    IN_PROGRESS,  // En cours
    COMPLETED,    // Terminée
    FAILED,       // Échouée
    COMPENSATED   // Compensée
}
```

### API du Saga Orchestrator

```java
@RestController
@RequestMapping("/api/saga")
public class SagaController {
    
    @PostMapping("/sales")
    public ResponseEntity<SagaResponse> startSalesSaga(@RequestBody SalesRequest request);
    
    @PostMapping("/returns")
    public ResponseEntity<SagaResponse> startReturnSaga(@RequestBody ReturnRequest request);
    
    @GetMapping("/{sagaId}")
    public ResponseEntity<SagaStatus> getSagaStatus(@PathVariable Long sagaId);
    
    @GetMapping("/{sagaId}/steps")
    public ResponseEntity<List<SagaStep>> getSagaSteps(@PathVariable Long sagaId);
}
```

### Configuration des Services

Chaque service métier expose des endpoints spécifiques pour les sagas :

```java
// Inventory Service
@PostMapping("/saga/reserve-stock")
@PostMapping("/saga/confirm-stock")
@PostMapping("/saga/release-stock")

// Transaction Service  
@PostMapping("/saga/create-transaction")
@PostMapping("/saga/cancel-transaction")

// Personnel Service
@GetMapping("/saga/validate-employee/{id}")
```

## Consequences

### Avantages

- **Cohérence des données** : Garantie de cohérence à terme
- **Résilience** : Gestion automatique des pannes
- **Traçabilité** : Historique complet des opérations
- **Monitoring** : Visibilité sur les transactions longues
- **Scalabilité** : Pas de verrous distribués
- **Maintenabilité** : Logique centralisée et claire

### Inconvénients

- **Complexité** : Ajout d'un service et d'une logique supplémentaires
- **Latence** : Overhead des appels d'orchestration
- **Point de défaillance** : L'orchestrateur devient critique
- **Cohérence à terme** : Pas de cohérence immédiate
- **Debugging** : Plus complexe à déboguer qu'une transaction locale

### Risques et mitigations

- **Panne de l'orchestrateur** : Haute disponibilité et backup
- **Timeout des étapes** : Configuration de timeouts appropriés
- **Idempotence** : Tous les appels doivent être idempotents
- **Monitoring** : Alertes sur les sagas en échec

## Patterns implémentés

### 1. Orchestration Pattern

- **Orchestrateur centralisé** : Contrôle du workflow
- **État persistant** : Sauvegarde de l'état entre les étapes
- **Compensation automatique** : Rollback en cas d'échec

### 2. Idempotency Pattern

- **Clés d'idempotence** : Prévention des doublons
- **Retry safe** : Opérations répétables sans effet de bord

### 3. Timeout Pattern

- **Timeouts configurables** : Éviter les blocages
- **Retry avec backoff** : Gestion des erreurs temporaires

## Monitoring et observabilité

### Métriques Saga

- **Durée des sagas** : Temps d'exécution par type
- **Taux de succès** : Pourcentage de sagas réussies
- **Taux de compensation** : Pourcentage de rollbacks
- **Étapes les plus lentes** : Identification des goulots

### Dashboards Grafana

- **Vue d'ensemble des sagas** : Statuts en temps réel
- **Performance par type** : Métriques par type de saga
- **Alertes** : Sagas en échec ou trop lentes

## Migration et déploiement

### Phase 1 : Infrastructure
- Déploiement du Saga Orchestrator Service
- Création de la base de données saga_db
- Configuration du monitoring

### Phase 2 : Adaptation des services
- Ajout des endpoints saga dans chaque service
- Implémentation de l'idempotence
- Tests d'intégration

### Phase 3 : Migration progressive
- Migration des transactions critiques vers les sagas
- Monitoring intensif
- Rollback possible vers l'ancien système

## Prochaines étapes

- **Event Sourcing** : Historique complet des événements
- **Saga Choreography** : Pour certains cas d'usage spécifiques
- **Distributed Tracing** : Traçabilité cross-services
- **Auto-scaling** : Mise à l'échelle de l'orchestrateur
