# ADR 002 – Migration vers l'architecture Spring Boot

## Status

Accepted

## Context

L'application initiale était prévue comme une application console Java simple avec interface CLI. Cependant, les besoins ont évolué pour inclure une interface web moderne, des APIs REST pour l'intégration, et une architecture plus robuste supportant plusieurs magasins. L'application doit également supporter une interface utilisateur moderne pour les employés et les clients, tout en maintenant la facilité de déploiement avec Docker.

## Decision

L'application a été migrée vers une architecture **Spring Boot** complète avec :

- **Spring Boot Web** pour l'interface web et les APIs REST
- **Spring Data JPA** pour la persistance avec Hibernate
- **Thymeleaf** pour le rendu des pages web côté serveur
- **Spring Security** pour l'authentification et l'autorisation
- **Swagger/OpenAPI** pour la documentation des APIs
- Architecture en couches suivant les principes DDD (Domain-Driven Design)

L'architecture suit le pattern en couches :

- **Presentation** : Contrôleurs web et API REST
- **Application** : Services applicatifs et logique de coordination
- **Domain** : Entités métier et logique business
- **Infrastructure** : Repositories et configuration

## Justification

Spring Boot offre un écosystème complet et mature pour le développement d'applications web modernes. Il simplifie la configuration, intègre naturellement avec Docker, et permet une évolutivité future. L'architecture DDD assure une séparation claire des responsabilités et facilite la maintenance.

## Consequences

* **Avantages :**
  - Interface web moderne accessible via navigateur
  - APIs REST pour intégration avec d'autres systèmes
  - Architecture modulaire et maintenable
  - Configuration simplifiée avec Spring Boot auto-configuration
  - Écosystème riche (sécurité, monitoring, tests)
  - Déploiement facile avec Docker

- **Inconvénients :**
  - Plus de complexité par rapport à une application console simple
  - Empreinte mémoire plus importante
  - Courbe d'apprentissage pour Spring Boot

## Options envisagées

- Alternative 1 : Garder l'application console avec CLI simple
- Alternative 2 : Application desktop avec JavaFX
- Alternative 3 : Architecture microservices (trop complexe pour ce projet)
