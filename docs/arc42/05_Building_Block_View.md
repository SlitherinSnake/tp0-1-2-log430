# 5. Vue des blocs de construction

## 5.1. Boîte blanche – Vue d’ensemble du système

### Diagramme
### 2.1. Vue logique
#### Diagramme de classe représentant les entités et composants principaux du système :
![dcl](https://img.plantuml.biz/plantuml/svg/lLPBRjim4DqBq1q6tiHEOHJP6aWG8CwYGDBcqy-QaSO6KOfK9AfjK7JR4_G0NTRxti0twKaw_4cYOZj9MtHZmvpXcpTlFlHnhLcbcqAaYUPQ8CmvMrQiA11oX4omkaQOmZbRiffBU8StC7xR80YrvBSDJj8aJVRWyF0GJYK1h5Srb1MN6I-PmDeOZ8V5WTEY5EeEuNEQ067o76R0fNQ_f2hevxMcoyjWHu9SS2PrR9cQpsUmHFqY7qyw47lqKXNshFFtdYtOUD9PltJiGegFP7VRa6uSgx-dTkcH8bkFs4MbyeRhnnBBcCQbgZWEpgaid-Xe8HJJRGgcPjZ3-nF-9o7W9kC5HRNdRToGimtTnUWb_9PAjQLBSehbp3CuKnaJSpfmfa99JQ2lbMOYodDdJlC0Q2qHfBMVzr3xUGpfxYpIKuNUgSuLQjLKs-Nnzg4-3whWxWrbk1Vu_oLwhLNsVkTK_ljm9rJBYlfdPxWv5apcY4Fb_VdJfBySW6wiZJVkACwR6kEU42eCVaCcoOfx-u2kVLHLlddfN2cnRL4xg5FZuQ-u1-0A1TDSoTgy73UCtWs__VVyvb-le5glcE1a-c26fWRKKDcsgjCaF0-ZWn5fTWIZ_H64MSV50ZDDxyxHPh-swQbwfY6EN5ntNV3r2c7z7He9XUkdD74cSzTY45PeDTAEOnCWFBB1S12UUvfuIqlZeAERAPcHw0CU-zDfl2p6cIfALNEZsgIbH3Z6iqq_U5jOUYZeEOov6UzuE0V0FQO1EVQ_3tt6bn9j9MssprsLsiJ8aSGitNNFF83rDUq5u3V2wX57Vi3PK0brOIPaRQQKcfjc84s2fTy9qTQO0QbGht_aBLY8KL8drYQDd573HgWHmWP-3iXwGURtnSmsHkuN00sGokW1xYGQTb-qR3R6sEfn7Z2SDl9kgjNKVBiTyElhDt2ZQNz6Gs9cUaOwbgQKrVgd-OVcTHc_AaspI1IJRbfRyOYwGTimU_HjuvWEAiemUMboZ3At_of_0m00)

###  Modèle de domaine – *Magasin Web (Spring Boot)*

| Classe             | Attributs clés                                                                                   | Description                                                                                     |
|--------------------|--------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------|
| `Employe`          | `id: int`, `nom: String`, `identifiant: String`                                                  | Représente un employé pouvant effectuer des ventes et des retours.                             |
| `Produit`          | `id: int`, `nom: String`, `categorie: String`, `prix: float`, `quantite: int`                    | Article disponible en stock, vendable ou retournable.                                          |
| `Vente`            | `id: int`, `dateVente: LocalDate`, `montantTotal: double`, `employe`, `magasin`, `venteProduits`| Vente complète validée par un employé, contenant plusieurs produits.                           |
| `Retour`           | `id: int`, `dateRetour: LocalDate`, `vente`, `employe`, `retourProduits`                         | Retour effectué après une vente existante, validé par un employé.                              |
| `VenteProduit`     | `id: int`, `vente`, `produit`, `quantite: int`                                                   | Ligne de vente d’un produit avec quantité.                                                     |
| `RetourProduit`    | `id: int`, `retour`, `produit`, `quantite: int`                                                  | Ligne de retour d’un produit avec quantité.                                                    |
| `Magasin`          | `id: int`, `nom: String`, `quartier: String`, `ventes`                                           | Point de vente physique, associé aux stocks et aux ventes.                                     |
| `StockMagasin`     | `id: int`, `produit`, `magasin`, `quantite: int`                                                 | Stock local d’un produit dans un magasin donné.                                                |
| `StockCentral`     | `id: int`, `produit`, `magasin`, `quantiteDemandee: int`, `dateDemande: LocalDate`              | Demande de réapprovisionnement effectuée par un magasin.                                       |
| `User`            | `id: long`, `username: String`, `password: String`, `roles: Set<Role>` | Compte de connexion stocké en base, utilisé par Spring Security. |
| `Role`            | `id: int`, `name: enum ERole { ROLE_ADMIN, ROLE_EMPLOYEE, ROLE_VIEWER }` | Rôle attribué à un utilisateur ; détermine ses privilèges. |


---

### Architecture Web MVC – *Spring Boot*

| Composant                  | Rôle                                                                                     |
|----------------------------|------------------------------------------------------------------------------------------|
| `VenteController`          | Gère le panier, les ajouts/retraits de produits, la validation de vente (via AJAX).     |
| `RetourController`         | Permet de sélectionner une vente passée et de retourner un ou plusieurs produits.       |
| `ProduitController`        | Gère l’affichage, l’ajout, la suppression, la modification et la recherche de produits. |
| `DashboardController`      | Affiche les indicateurs (ruptures, surstocks, CA, tendances hebdomadaires).             |
| `RapportController`        | Génère les rapports consolidés : par magasin, par produit, stock global.                |
| `StockCentralController`   | Permet de consulter le stock central et d’envoyer des demandes de réapprovisionnement.  |
| `AuthController` (REST)       | Endpoints `/api/v1/auth/login`, `/signup` : émet un JWT ou crée un compte.                       |
| `AuthViewController` (Web)    | Affiche la page `/login`, fournit `/api/auth/token` pour convertir la session en JWT.            |


| Service                    | Rôle                                                                                     |
|----------------------------|------------------------------------------------------------------------------------------|
| `RapportService`           | Calcule les ventes par magasin, identifie les produits les plus vendus, retourne le stock actuel. |
| `StockCentralService`      | Gère les demandes de réapprovisionnement, filtre les doublons, affiche l’historique.    |
| `UserDetailsServiceImpl`      | Charge un `User` + rôles pour Spring Security.                                                   |
| `JwtUtils`                    | Génère, signe et valide les tokens JWT.                                                          |
| `AuthTokenFilter`             | Filtre HTTP : extrait le JWT, authentifie la requête API.                                        |

| Repository                 | Rôle                                                                                     |
|----------------------------|------------------------------------------------------------------------------------------|
| `ProduitRepository`        | Accès aux produits, requêtes de filtrage par nom/catégorie, stock critique.             |
| `VenteRepository`          | Accès aux ventes, calculs de chiffre d’affaires, produits les plus vendus.              |
| `RetourRepository`         | Accès aux retours, quantités déjà retournées par vente/produit.                         |
| `EmployeRepository`        | Accès aux employés.                                                                      |
| `MagasinRepository`        | Accès aux magasins.                                                                      |
| `StockMagasinRepository`   | Accès aux stocks locaux, recherche par magasin et produit.                              |
| `StockCentralRepository`   | Accès aux demandes de réapprovisionnement central.    |
| `UserRepository`              | CRUD et recherche par `username`.                                                                |
| `RoleRepository`              | CRUD et recherche par `ERole`.                                                                   |

### Motivation
J'ai pris l'architecture MVC, car je suis familier avec. Aussi elle offre une séparation claire entre l’interface utilisateur, la logique métier et la gestion des interactions utilisateurs.

### Blocs principaux (black box)

| Bloc         | Rôle                                              |
|--------------|---------------------------------------------------|
| View         | Affichage console et saisie utilisateur (CLI)     |
| Controller   | Gère les actions de l’utilisateur et appelle les services/DAO |
| Model/DAO    | Contient les entités JPA et l’accès aux données via Hibernate |
| PostgreSQL       | Base de données locale relationnelle              |
| Security   | Chaîne Spring Security : session (form login) + filtre JWT pour `/api/**`.             |
| API REST   | Contrôleurs JSON stateless ; documentés via Swagger/OpenAPI.                           |

## 5.2. Niveau 2 <- à rectifier plus tard

| Sous-bloc     | Description                                      |
|---------------|--------------------------------------------------|
| EmployeDAO    | L'employer qui fait les ventes, retours et consulation du stock        |
| ProduitDAO    | Accès aux produits : recherche, liste, stock     |
| VenteDAO      | Enregistrement d’une vente                       |
| RetourDAO     | Gestion des retours et ajustement du stock       |
| UserDAO       | Accès aux comptes utilisateurs ; vérification d’unicité du login.               |
| RoleDAO       | Accès aux rôles et initialisation des valeurs `ROLE_ADMIN`, etc.                |
| HibernateUtil | Initialise les sessions Hibernate                |

### Vue d’implémentation – Organisation des modules (Spring Boot Web)
L’application suit une architecture **MVC enrichie**, avec une séparation claire des responsabilités :

- **Contrôleurs (`controller`)** : Gèrent les interactions utilisateur (formulaires, AJAX, vues Thymeleaf).
- **Services (`service`)** : Appliquent les règles d’affaires (calculs, regroupements, filtrage, validations métier).
- **Repositories (`repository`)** : Abstraction de la base de données via Spring Data JPA (équivalent DAO).
- **Modèles (`model`)** : Entités JPA mappées aux tables de la base de données (`Produit`, `Vente`, `Retour`, etc.).
- **Vues (`.html`)** : Templates Thymeleaf côté client (non inclus dans ce diagramme technique mais couplés aux contrôleurs).


![di](https://img.plantuml.biz/plantuml/svg/dLVTRjem5BwFb7Umk5Zt8z2axceILJlef4wgYc1brsxYKGyJHxOpnAPAlUe3R5Tx1Ext1hm9JpB73WcEJyWeDq2-ppxdV3x-3fnBXONA5YmC55MCe6b6KBnz_4CNATki5oHHM56UeEtZRtGZPZYXqZw8YKGB7cTClx-ydWxG6pGXuZkDYFuq9YcNL76nUXK6ON22-lq-6k5eZcUQhh-4GMg_eKx45pt6P-zEt_PKUjgBUA84Puo83leP1WX534k9HaB7ecfGeXKqrKaI21WJnJC18HF5e_b0hnEOWIjncd81HXjYUN_7iOWXy29Jzz3p8lkBb3QjcbAotCSUSjsQD99MQ4UW8kNY3E1II_3Wxg8yFEW4ggEjdG04s1E0a4lTNtm5Bhh6Conf0a6cc3QyA1R2RxLoO9wSEQaSr4TtFM6GD3L1wZ9unTVutYvscVUug1zM1TR2LS2jkwZs17MxNVI1yGZ9pJhA15MRDNg1i6xE73hoTRXzpVQhthUbSdJyj5ItYZBfNetFV4wIZvIf-fsvrBMjHfmcIYzilJ95pEBkI2TmhigGAAnZN-cp0O7YybZyO2njOL_fp0dmXTnDYWm61gsW0HUo_kmc9Oar2-Vn42jybL15CQC_Mbq2fxJ9JO_pgOVAmyU46VkNoEoy-Sj89YJQFlr2Es6jlNiEgdlep3Cfs8SD5VIj_qYZT2-wIyKR7MP_mCvgW-GOckU040dmF8W6-9lMipRBh9oUlO8qBpcG1LGQ1i2qWxTi8ZOvTXCVpE90cm1RjpNLCvOqzU5ar_DvvuszpdFRlU4iV4wh1Yta9cfBuVlKfa6wcnFL1Km5JIAQulntuBEcXEj59der1m2mFiC1IdsSMzBU7Ab5WLqUZ1O6dfKRdZUeO5hJFB4PkZy20Ia0WZUwd79C7PtN7KFDttDfq7iprNMZghnLp-oAgifoNzsTZfAbk7cKSvB4ve_3Fm00)

## 5.3. Niveau 3

Ce niveau de détail n’est pas nécessaire dans notre contexte. Notre application repose sur une architecture à deux couches (client-console et base de données). Aucun sous-bloc supplémentaire n’est requis à ce stade.
