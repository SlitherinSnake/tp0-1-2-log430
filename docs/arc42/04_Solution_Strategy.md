# 4. Stratégie de solution

Résumé des décisions clés qui structurent l’architecture du système.

## 4.1. Choix technologiques

| Catégorie         | Ancien          | Nouveau                                         |
| ----------------- | --------------- | ---------------------------------------------------------------- |
| Langage           | Java            | idem                                                 |
| Framework Web     | —               | **Spring Boot (MVC + REST)**                                   |
| Accès BD          | Hibernate (JPA) | **Spring Data JPA** (Hibernate)                      |
| Base de données   | SQLite locale   | **PostgreSQL**    |
| Sécurité          | —               | **Spring Security**, JWT                        |
| Documentation API | —               | **OpenAPI / Swagger-UI**                                         |
| Tests             | JUnit           | JUnit 5 + Spring Security Test + Testcontainers (PostgreSQL)     |
| Build             | Maven           | idem                           |
| CI/CD             | GitHub Actions  | idem     |
| Conteneurs        | Docker          | idem |
| UI                | CLI             | **Thymeleaf**                                    |
| Observabilité     | —               | Micrometer + Actuator + Prometheus/Grafana (Lab 4)               |

## 4.2. Architecture globale

• Architecture 3-tier : 
  1. IHM Web (Thymeleaf) ou client externe → 
  2. API REST stateless (Spring Boot, JWT) → 
  3. PostgreSQL central.

• Pattern MVC + services applicatifs.

• Couche sécurité transversale :
  – filtre JWT pour /api/**  
  – session-form pour les vues Thymeleaf.

• Isolation logique par modules : produits, ventes, retours, stock, auth.

• Déploiement multi-conteneurs : 
  nginx (static) ↔ spring-app ↔ Postgres.  
  Scalabilité horizontale via load-balancer (Lab 4).

• Synchronisation multi-magasins : (roadmap) replication logique ou bus d’événements.

## 4.3. Objectifs de qualité visés

| Attribut           | Motivation                           | Concrétisation                                             |
| ------------------ | ------------------------------------ | ---------------------------------------------------------- |
| **Sécurité**       | Protéger les données sensibles       | JWT, BCrypt, CORS strict, tests d’intrusion                |
| **Scalabilité**    | Périodes de pointe (soldes)          | Stateless API, conteneurs répliqués, cache Redis (à venir) |
| **Maintenabilité** | Équipe seule → évolution rapide      | Modularisation, ADR, tests, CI/CD                          |
| **Robustesse**     | Continuité de service multi-magasins | Observabilité, circuit-breaker (future)                    |
| **Simplicité**     | On garde un socle compréhensible     | Spring Boot opinionated, conventions                       |
| **Autonomie**      | Magasin offline (option)             | Mode dégradé / synchro différée (étude ultérieure)         |

## 4.4. Organisation

- Documentation basée sur arc42
- Décisions techniques tracées via ADR
- Projet réalisé tout seul

## 4.5. Vue des cas d’utilisation

Acteurs : Administrateur, Employé, Viewer, Client API
Cas principaux :

Se connecter / obtenir un JWT

Gérer les utilisateurs & rôles (Admin)

Rechercher un produit

Consulter le stock central

Ajouter un produit au panier

Valider une vente

Initier / valider un retour

Générer un rapport consolidé des ventes

![cu-menu](https://img.plantuml.biz/plantuml/svg/hLPBJXj14DrRyXrANc2Z14NyfGWG3Y3YX02n3MmYeMQcC9tqT4_wGq199EwGLMw0zxc1Ry99Kjrj3vu1hn9BbZMlgxfUlVgCjurbshfCr6kMMu6mozb0ion3I14IPY1jEii5Dyno9U7XxXVqReqTtKUB0Pg0PqvOlf2n0JDS6cYTuSLylLQlDM5pSnFQYKMd3UIQouJdpDruk5vZYLKQ6djPBjJjQDW008eO7x4uRgSPbznOpVmj3MyIeCBWbECtrD0Ic5Yd4GmwWVF1C1uLbZk2exJGFku0tbZK4kDrEOydaVB2ltxgDKrKc1oGe8rsdWkUHFb85kXv5GRmHYdxIRRwLxSP2cINy2BGwkxr-g_UdVNfyLWPEz3O-tX0kNptr036UZfxHMpvvUJUEM2E4fnKAOWBu0nI1Ob2Zqgff2HEGIOru3BASx9ptZxflpq_EDhl79PTcbJ7H7EIisNuGBgyxD7hx1-U79TSZYuiIgx1IR3g2qgWD4e-_QFtUo5A2O2D7miBPIRm6ZOsg5M4It5hQphp_N6_fMXWqfUXjLfwD3IehqT3QEz-w1pEFn5XVpJKbBweUiw17lqXjA3gtSzFUdlTtlEHgOrLwhWrJq9qCRb2xRzUXB6Px-gfJmYoM7BRKTBG_0HoOAnAla12vTLCDCfkIxEak2PpVCgd_LavMo93VG8vannrO_hYvPBRAHCy3MuORwwaKBYjpEAZqIglRPLGHtBB8w5eCklcrMS9-TeLuiSB99MzyUYtDiseo_9SwMVgHAqWOpTu8j7AJuXH58Sww57ISLEirsi__zjkQCAkLZu3wZmKOMADHoTJFPxf5gLdVKTaIgP8stIy5u4uR5KdTxm2dpqBon9cd4qsN-bmFAYUL2TiWJmoKk5Neg01DHoi0_5mzrkvQ-s7IlAl3ZD_br_mp4SrBuEiD9S8Wi6b6DtJTCJrFLymcmuzsIi-zgnXV-9OdjWcxFZt5Z6RVdFX3Rr2cBJcKSZelbW5bgh0SXLOgGAhLM2j2gnxPfCg5O7Bm58L24wJyXLEPM2f2YnNWPKgi5e5rgf02BoDCWt_3Fu2)
