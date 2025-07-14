# ADR 004 – Architecture Domain-Driven Design (DDD)

## Status

Accepted (Évolué vers microservices - voir ADR 007)

## Context

L'application de gestion de magasin a grandi en complexité avec l'ajout de multiples fonctionnalités : gestion d'inventaire, transactions, retours, multi-magasins, personnel. Cette complexité a conduit à une évolution vers une architecture microservices où chaque domaine DDD est devenu un service indépendant. Une architecture claire est nécessaire pour maintenir la cohérence du code et faciliter l'évolution future. L'équipe a besoin d'une approche qui sépare clairement la logique métier de l'infrastructure technique, préparant ainsi la migration vers les microservices.

## Decision

Adoption de l'architecture **Domain-Driven Design (DDD)** avec une organisation en couches :

### Structure des packages

- **`com.log430.tp4.domain`** : Entités métier et logique business pure
  - `inventory` : Gestion des produits et stock
  - `transaction` : Ventes, retours, et transactions
  - `store` : Magasins et localisations
  - `personnel` : Employés et rôles

- **`com.log430.tp4.application`** : Services applicatifs et orchestration
  - `service` : Coordination entre domaines

- **`com.log430.tp4.infrastructure`** : Accès aux données et configuration
  - `repository` : Repositories JPA
  - `config` : Configuration Spring

- **`com.log430.tp4.presentation`** : Interfaces utilisateur
  - `api` : Contrôleurs REST API
  - `web` : Contrôleurs web MVC
  - `dto` : Objets de transfert de données

## Justification

DDD apporte plusieurs avantages pour cette application :

- **Séparation claire** entre logique métier et technique
- **Ubiquitous Language** : vocabulaire partagé entre développeurs et métier
- **Bounded Contexts** : séparation des domaines (inventaire, ventes, personnel)
- **Rich Domain Model** : entités avec comportements métier
- **Testabilité** améliorée par l'isolation des domaines

## Consequences

- **Avantages :**
  - Code métier isolé de l'infrastructure
  - Facilité de test des règles business
  - Architecture évolutive et maintenable
  - Réutilisabilité des domaines
  - Communication claire avec les experts métier

- **Inconvénients :**
  - Courbe d'apprentissage pour l'équipe
  - Structure plus complexe pour un petit projet
  - Risque de sur-ingénierie

## Patterns DDD implémentés

- **Entities** : `InventoryItem`, `Transaction`, `Store`, `Personnel`
- **Value Objects** : `TypeTransaction`, `StatutTransaction`
- **Repositories** : Interface d'accès aux données
- **Domain Services** : `InventoryService` pour la logique complexe
- **Application Services** : Orchestration des cas d'usage

## Options envisagées

- Alternative 1 : Architecture MVC simple (moins de séparation)
- Alternative 2 : Clean Architecture (plus complexe)
- Alternative 3 : Layered Architecture classique (moins flexible)
