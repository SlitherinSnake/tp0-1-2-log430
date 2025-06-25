# 3. Contexte du système

La portée et le contexte du système, comme leur nom l'indique, délimitent votre système (c'est-à-dire votre portée) de tous ses partenaires de communication (systèmes voisins et utilisateurs, c'est-à-dire le contexte de votre système). Ils précisent ainsi les interfaces externes.

Si nécessaire, différenciez le contexte métier (entrées et sorties spécifiques au domaine) du contexte technique (canaux, protocoles, matériel).

## 3.1. Contexte métier

Notre application est un système de caisse pour un petit magasin de quartier. Il permet à un employé d'effectuer les opérations suivantes :

- de rechercher un produit (par identifiant, nom ou catégorie),
- d’enregistrer une vente (sélection des produits et calcul du total),
- de gérer les retours (annuler une vente),
- de consulter l’état du stock des produits.

Les utilisateurs utilisent un CLI pour intéragir avec l'application. 

## 3.2. Contexte technique

L’application s’exécute dans un environnement local, à l’intérieur d’une machine virtuelle. Elle suit une architecture client/serveur à deux niveaux (2-tier) :

- Le **client** est une application Java console.
- Le **serveur** est représenté par une base de données locale PostgreSQL, accédée directement via Hibernate.

La communication se fait entièrement en mémoire locale. Hibernate est l'ORM utilisé pour mapper les entités métiers vers ma base de données relationnelle (PostgreSQL).

## 3. Contexte du système

Le système couvre maintenant l’ensemble de la chaîne « vente en magasin ↔ logistique ↔ supervision » au sein d’un environnement Web centralisé.  
Il expose *deux* canaux d’accès : des vues serveur-side (Thymeleaf) **et** une API REST stateless protégée par JWT.

### 3.1. Contexte métier

| Acteur / Système voisin | Interaction principale | Entrées métier | Sorties métier |
|-------------------------|------------------------|----------------|----------------|
| **Employé de magasin**  | Réalise les opérations de caisse via l’IHM Web | Sélection d’articles, quantités, retours | Ticket de vente, confirmation de retour |
| **Administrateur**      | Gère les comptes utilisateurs, les rôles et le catalogue produits | Compte/rôle, fiche produit | Accusés de création / mise à jour |
| **Viewer (lecture seule)** | Consulte le stock et les rapports | — | Tableaux de bord, inventaires |
| **Centre logistique**   | Fournit le stock central et traite les demandes de réapprovisionnement | Demande d’approvisionnement | Accusé de réception, mise à jour de stock |
| **Maison mère / Direction** | Consulte les rapports consolidés (ventes multi-magasins) | Période, filtres | Rapports PDF/CSV, indicateurs clés |
| **Client API externe** *(future appli mobile, intégrations)* | Consomme l’API REST via JWT | Requêtes JSON (produits, ventes) | Réponses JSON |

> **Flux métier principaux**  
> 1. **Vente** : panier → validation → écriture en BD → impression ticket.  
> 2. **Retour** : saisie → contrôle éligibilité → réintégration stock.  
> 3. **Stock central** : consultation + déclenchement réapprovisionnement.  
> 4. **Reporting** : agrégation multi-magasins → export.  
> 5. **Administration** : création d’utilisateurs / attribution de rôles.

### 3.2. Contexte technique

| Composant | Rôle | Technologie / Protocole |
|-----------|------|-------------------------|
| **Navigateur Web** | Client des vues serveur-side | HTTPS + session cookie |
| **Spring Boot App** | Serveur Web & API REST | Java 21, Spring Boot 3, Thymeleaf, Spring MVC |
| **Filtre JWT (`AuthTokenFilter`)** | Authentifie les appels `/api/**` | HTTP Authorization : Bearer \<token\> |
| **Base de données PostgreSQL (conteneur)** | Stocke données métier centralisées | JDBC / Hibernate (JPA) |
| **Docker Compose** | Orchestration locale | Services : `app`, `postgres`, `pgadmin` |
| **GitHub Actions** | CI / CD | Maven build, tests JUnit 5 / Spring Security Test, Sonar / OWASP DC, docker build & push |
| **Actuator + Micrometer** | Observabilité (health, metrics) | Endpoints `/actuator/*`, Prometheus scrape |

![Contexte Technique](https://img.plantuml.biz/plantuml/svg/NP9FQy904CNl2_iT1hrKYdWH2XwAVuiLswOcw2MNDHdJnMHtsLqfKlhTEzaaLCnfv-KtCozlCtLUM7-fIixSMIeZhAZ0OkQ5Aag47yu0vY8x5rPVLBxGfRRGHpm5NTiSRQFLJtyyVkBibpEHUP9MbIdr5RjobbTITSLUuZVQhkejIedAmonwxO5mC3EICyx-RVHYOwKgQACcofXKFIHVrmfB52TuXDqgJaQRGp9ekqtZ-xOVdPDQKT-BjXLiTI5LGxTlxdXo4gQcQXD_lDCAcXxm7JeVlf6pN7Xn58xeI3jVM8m_jxHWgRCpscPuLDHW6mGCXy_TEfZ0_e9mm2Drh9EtBOnWdIHHFE0i14Lntk2SDO7TYPHJ2BuMQsFrUGB1SJikLNCKbXh9VysRm4P5ZItdYrIjvH6j4fwkQOeg3p_27m00)