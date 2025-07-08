# 5. Vue des blocs de construction

## 5.1. Boîte blanche – Vue d'ensemble du système

### Diagramme

### 2.1. Vue logique

#### Diagramme de classe représentant les entités et composants principaux du système

![dcl](https://img.plantuml.biz/plantuml/svg/lLPBRjim4DqBq1q6tiHEOHJP6aWG8CwYGDBcqy-QaSO6KOfK9AfjK7JR4_G0NTRxti0twKaw_4cYOZj9MtHZmvpXcpTlFlHnhLcbcqAaYUPQ8CmvMrQiA11oX4omkaQOmZbRiffBU8StC7xR80YrvBSDJj8aJVRWyF0GJYK1h5Srb1MN6I-PmDeOZ8V5WTEY5EeEuNEQ067o76R0fNQ_f2hevxMcoyjWHu9SS2PrR9cQpsUmHFqY7qyw47lqKXNshFFtdYtOUD9PltJiGegFP7VRa6uSgx-dTkcH8bkFs4MbyeRhnnBBcCQbgZWEpgaid-Xe8HJJRGgcPjZ3-nF-9o7W9kC5HRNdRToGimtTnUWb_9PAjQLBSehbp3CuKnaJSpfmfa99JQ2lbMOYodDdJlC0Q2qHfBMVzr3xUGpfxYpIKuNUgSuLQjLKs-Nnzg4-3whWxWrbk1Vu_oLwhLNsVkTK_ljm9rJBYlfdPxWv5apcY4Fb_VdJfBySW6wiZJVkACwR6kEU42eCVaCcoOfx-u2kVLHLlddfN2cnRL4xg5FZuQ-u1-0A1TDSoTgy73UCtWs__VVyvb-le5glcE1a-c26fWRKKDcsgjCaF0-ZWn5fTWIZ_H64MSV50ZDDxyxHPh-swQbwfY6EN5ntNV3r2c7z7He9XUkdD74cSzTY45PeDTAEOnCWFBB1S12UUvfuIqlZeAERAPcHw0CU-zDfl2p6cIfALNEZsgIbH3Z6iqq_U5jOUYZeEOov6UzuE0V0FQO1EVQ_3tt6bn9j9MssprsLsiJ8aSGitNNFF83rDUq5u3V2wX57Vi3PK0brOIPaRQQKcfjc84s2fTy9qTQO0QbGht_aBLY8KL8drYQDd573HgWHmWP-3iXwGURtnSmsHkuN00sGokW1xYGQTb-qR3R6sEfn7Z2SDl9kgjNKVBiTyElhDt2ZQNz6Gs9cUaOwbgQKrVgd-OVcTHc_AaspI1IJRbfRyOYwGTimU_HjuvWEAiemUMboZ3At_of_0m00)

### Modèle de domaine – *Magasin Web DDD (Spring Boot)*

| Classe             | Attributs clés                                                                                   | Description                                                                                     |
|--------------------|--------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------|
| `Personnel`        | `id: Long`, `nom: String`, `identifiant: String`, `username: String`, `password: String`, `roles: Set<Role>` | Entité unifiée représentant à la fois un employé et un utilisateur du système avec authentification. |
| `InventoryItem`    | `id: Long`, `nom: String`, `categorie: String`, `prix: Double`, `stockCentral: Integer`, `description: String` | Article unifié combinant les informations produit et la gestion du stock central.              |
| `Transaction`      | `id: Long`, `typeTransaction: TypeTransaction`, `dateTransaction: LocalDate`, `montantTotal: Double`, `personnelId: Long`, `storeId: Long` | Transaction unifiée représentant les ventes et retours selon le type de transaction.           |
| `TransactionItem`  | `id: Long`, `inventoryItemId: Long`, `quantite: Integer`, `prixUnitaire: Double`, `sousTotal: Double` | Ligne de transaction (vente/retour) d'un produit avec quantité et prix.                        |
| `Store`            | `id: Long`, `nom: String`, `quartier: String`, `isActive: boolean`                               | Point de vente physique, associé aux stocks et aux transactions.                               |
| `StoreInventory`   | `id: Long`, `storeId: Long`, `inventoryItemId: Long`, `quantite: Integer`                        | Stock local d'un produit dans un magasin donné.                                                |
| `Role`             | `id: Long`, `name: ERole { ROLE_ADMIN, ROLE_EMPLOYEE, ROLE_VIEWER }`                            | Rôle attribué à un personnel ; détermine ses privilèges dans le système.                       |

**Types d'énumération :**

- `TypeTransaction`: `VENTE`, `RETOUR`
- `StatutTransaction`: `EN_COURS`, `COMPLETEE`, `ANNULEE`
- `ERole`: `ROLE_ADMIN`, `ROLE_EMPLOYEE`, `ROLE_VIEWER`

---

### Architecture Web MVC – *Spring Boot DDD*

| Composant                  | Rôle                                                                                     |
|----------------------------|------------------------------------------------------------------------------------------|
| `TransactionController`    | API REST pour créer des transactions (ventes) et lister toutes les transactions.         |
| `InventoryController`      | API REST pour gérer l'inventaire : CRUD des articles, consultation du stock, etc.       |
| `WebController`            | Contrôleur web MVC pour les vues Thymeleaf (dashboard, produits, panier, etc.).         |
| `LogController`            | API REST pour consulter les logs de l'application.                                       |

| Service                    | Rôle                                                                                     |
|----------------------------|------------------------------------------------------------------------------------------|
| `InventoryService`         | Service applicatif pour la gestion de l'inventaire, calculs de stock, cache, etc.       |

| Repository                 | Rôle                                                                                     |
|----------------------------|------------------------------------------------------------------------------------------|
| `TransactionRepository`    | Accès aux transactions, requêtes par personnel, type, etc.                              |
| `InventoryItemRepository`  | Accès aux articles d'inventaire, recherche par catégorie, stock critique.               |
| `PersonnelRepository`      | Accès aux personnels/utilisateurs du système.                                           |
| `StoreRepository`          | Accès aux magasins.                                                                      |
| `StoreInventoryRepository` | Accès aux stocks locaux par magasin.                                                    |
| `RoleRepository`           | Accès aux rôles du système.                                                             |

### Motivation

L'architecture suit les principes du Domain-Driven Design (DDD) avec une séparation claire entre les couches domaine, application, infrastructure et présentation. L'utilisation d'entités unifiées (Personnel au lieu de User/Employee séparés) simplifie le modèle tout en conservant la flexibilité.

### Blocs principaux (black box)

| Bloc         | Rôle                                              |
|--------------|---------------------------------------------------|
| Presentation | Contrôleurs REST API et Web MVC avec vues Thymeleaf |
| Application  | Services applicatifs coordonnant la logique métier |
| Domain       | Entités métier et logique de domaine              |
| Infrastructure | Repositories, configuration, accès aux données   |
| PostgreSQL   | Base de données relationnelle centralisée        |

## 5.2. Architecture DDD - Couches

| Couche        | Description                                      | Composants |
|---------------|--------------------------------------------------|------------|
| Presentation  | Interface utilisateur et API                    | `@RestController`, `@Controller`, DTOs |
| Application   | Coordination et orchestration                    | `@Service` (InventoryService) |
| Domain        | Logique métier et entités                       | Entités JPA, enums, logique domaine |
| Infrastructure| Accès aux données et configuration              | `@Repository`, `@Configuration` |

### Vue d'implémentation – Organisation des modules (Spring Boot DDD)

L'application suit une architecture **DDD (Domain-Driven Design)**, avec une séparation claire des responsabilités par couches :

- **Présentation (`presentation.api`, `presentation.web`)** : Contrôleurs REST et Web MVC
- **Application (`application.service`)** : Services applicatifs orchestrant la logique métier
- **Domaine (`domain.*`)** : Entités métier organisées par agrégats (transaction, inventory, personnel, store)
- **Infrastructure (`infrastructure.*`)** : Repositories, configuration, accès aux données

![di-updated](https://img.plantuml.biz/plantuml/svg/dLVTRjem5BwFb7Umk5Zt8z2axceILJlef4wgYc1brsxYKGyJHxOpnAPAlUe3R5Tx1Ext1hm9JpB73WcEJyWeDq2-ppxdV3x-3fnBXONA5YmC55MCe6b6KBnz_4CNATki5oHHM56UeEtZRtGZPZYXqZw8YKGB7cTClx-ydWxG6pGXuZkDYFuq9YcNL76nUXK6ON22-lq-6k5eZcUQhh-4GMg_eKx45pt6P-zEt_PKUjgBUA84Puo83leP1WX534k9HaB7ecfGeXKqrKaI21WJnJC18HF5e_b0hnEOWIjncd81HXjYUN_7iOWXy29Jzz3p8lkBb3QjcbAotCSUSjsQD99MQ4UW8kNY3E1II_3Wxg8yFEW4ggEjdG04s1E0a4lTNtm5Bhh6Conf0a6cc3QyA1R2RxLoO9wSEQaSr4TtFM6GD3L1wZ9unTVutYvscVUug1zM1TR2LS2jkwZs17MxNVI1yGZ9pJhA15MRDNg1i6xE73hoTRXzpVQhthUbSdJyj5ItYZBfNetFV4wIZvIf-fsvrBMjHfmcIYzilJ95pEBkI2TmhigGAAnZN-cp0O7YybZyO2njOL_fp0dmXTnDYWm61gsW0HUo_kmc9Oar2-Vn42jybL15CQC_Mbq2fxJ9JO_pgOVAmyU46VkNoEoy-Sj89YJQFlr2Es6jlNiEgdlep3Cfs8SD5VIj_qYZT2-wIyKR7MP_mCvgW-GOckU040dmF8W6-9lMipRBh9oUlO8qBpcG1LGQ1i2qWxTi8ZOvTXCVpE90cm1RjpNLCvOqzU5ar_DvvuszpdFRlU4iV4wh1Yta9cfBuVlKfa6wcnFL1Km5JIAQulntuBEcXEj59der1m2mFiC1IdsSMzBU7Ab5WLqUZ1O6dfKRdZUeO5hJFB4PkZy20Ia0WZUwd79C7PtN7KFDttDfq7iprNMZghnLp-oAgifoNzsTZfAbk7cKSvB4ve_3Fm00)
