# 8. Concepts transversaux

Cette section décrit les principes et règles techniques appliqués à TOUTES les couches de l’application.

## 8.1. Modèle architectural

- **Pattern MVC enrichi Spring Boot** :  
  - Vues serveur-side : Thymeleaf + session.  
  - API REST stateless : contrôleurs `/api/**` renvoyant JSON.  
- **Services** intermédiaires appliquant la logique métier, appelés par les contrôleurs.  
- **Repositories** Spring Data JPA encapsulant la persistance (équivalent DAO).  
- **DTO + Mapper** : isolent les entités JPA du contrat REST, portent les règles de validation.

## 8.2. Sécurité et authentification  🆕

- Double chaîne **Spring Security** :  
  1. Form-login (session cookie) pour les vues Thymeleaf.  
  2. **JWT Bearer** pour l’API (filtre `AuthTokenFilter`).  
- Rôles persistés : `ROLE_ADMIN`, `ROLE_EMPLOYEE`, `ROLE_VIEWER`.  
- Hashage des mots de passe avec **BCrypt**.  
- **CORS centralisé** : origines whitelists, headers contrôlés.  
- Exceptions homogènes via `GlobalExceptionHandler` + `ErrorResponse`.

## 8.3. ORM et persistance  (mis à jour)

- **Spring Data JPA + Hibernate** sur **PostgreSQL 15** (conteneur).  
- Migrations futures : Flyway (roadmap).  
- Requêtes typées via `@Query` ou méthodes dérivées (`findByNomContainingIgnoreCase`).

## 8.4. Transactions

- Chaque vente, retour, demande de stock est annotée `@Transactional`.  
- Politique : **READ_COMMITTED** (MVCC PostgreSQL).  
- Rollback automatique sur exception pour garantir l’intégrité (stock / vente / retour).

## 8.5. Observabilité  🆕

- **Spring Boot Actuator** exposé (`/actuator/health`, `/metrics`).  
- **Micrometer** prêt pour Prometheus / Grafana (Lab 4 : load balancing & monitoring).  
- Logs structurés (JSON) côté conteneur, regroupés via Docker std-out.

## 8.6. Documentation API  🆕

- Spécification **OpenAPI v3** générée automatiquement ; accessible via **Swagger-UI**.  
- Schéma de sécurité `bearerAuth` documenté.

## 8.7. Intégration continue  (étendue)

- **GitHub Actions** :  
  - Build Maven, tests JUnit 5 + Spring Security Test.  
  - Analyse SCA : OWASP Dependency-Check.  
  - Build & push image Docker `pos-web:<sha>` sur Docker Hub.

## 8.8. Conteneurisation / Déploiement  (mis à jour)

- **Docker Compose** :  
  - `app` (Spring Boot, port 8080)  
  - `postgres` (port 5432)  
- Réseau bridge unique ; variables d’environnement (JWT secret, CORS, DB URL) injectées via `.env`.

