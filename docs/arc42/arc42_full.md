# 00. Table of Contents

1. [Introduction and Goals](01_Introduction_and_Goals.md)  
2. [Architecture Constraints](02_Architecture_Constraints.md)  
3. [Context and Scope](03_Context_and_Scope.md)  
4. [Solution Strategy](04_Solution_Strategy.md)  
5. [Building Block View](05_Building_Block_View.md)  
6. [Runtime View](06_Runtime_View.md)  
7. [Deployment View](07_Deployment_View.md)  
8. [Crosscutting Concepts](08_Crosscutting_Concepts.md)  
9. [Architecture Decisions](09_Architecture_Decisions.md)  
10. [Quality Requirements](10_Quality_Requirements.md)  
11. [Risks and Technical Debt](11_Risks_and_Technical_Debt.md)  
12. [Glossary](12_Glossary.md)
# 1. Introduction et Objectifs

Ce document décrit les exigences et les objectifs qui guident le développement du système de caisse (POS) réalisé dans le cadre des laboratoires 0, 1 et 2 du cours LOG430.

## 1.1. Vue d’ensemble des laboratoires précédents
### Laboratoire 0 – Infrastructure

Le laboratoire 0 a permis de poser les bases techniques du projet. Les objectifs atteints incluent :

- Création d’un dépôt Git structuré et versionné  
- Mise en place d’un environnement de conteneurisation avec **Docker** et **Docker Compose**  
- Intégration d’une pipeline CI/CD via **GitHub Actions**, comprenant :
  - Lint automatique
  - Exécution des tests unitaires (JUnit)
  - Build de l’image Docker
  - Publication de l’image sur **Docker Hub**
- Développement d’un projet minimal (`Hello World`) en CLI ou Web
- Écriture de premiers tests unitaires avec JUnit
- Validation de l’exécution en environnement virtuel


Ce socle assure une exécution reproductible, automatisée et fiable pour les étapes suivantes du projet.

### Laboratoire 1 – Architecture 2-Tiers POS

Dans ce laboratoire, une application 2-tier a été développée :

- **Client** : une application console Java interactive (CLI)  
- **Base de données** : PostgreSQL via **Hibernate ORM** 

Fonctionnalités couvertes :

- Recherche de produits (par ID, nom ou catégorie)  
- Enregistrement de ventes  
- Gestion des retours partiels ou complets  
- Consultation du stock 

L’architecture est structurée selon un **modèle MVC** clair (Console – Controller – DAO) avec une bonne séparation des responsabilités.  
Des **tests JUnit** ont été développés et intégrés à la pipeline. Une **documentation complète en Arc42** a été produite, accompagnée de diagrammes **UML 4+1** et de **4 ADRs** justifiant les décisions techniques majeures.

## 1.2. Transition vers le Laboratoire 2 – Evolution d’Architecture

Le Laboratoire 2 marque un tournant : il introduit une **dimension multi-sites** et **centralisée**, visant à répondre aux besoins d'une entreprise disposant de :

- Plusieurs **magasins** répartis dans différents quartiers  
- Un **centre logistique** gérant le stock global  
- Une **maison mère** assurant la supervision, les décisions stratégiques et les rapports consolidés  

Les nouvelles exigences incluent :

- Synchronisation fiable des données entre les entités  
- Consultation centralisée des ventes et du stock  
- Production de rapports consolidés  
- Ouverture vers une interface **Web ou mobile**

Les **limites** de l’architecture 2-tier deviennent claires :

-  Couplage fort entre client et base de données  
-  Données cloisonnées et non synchronisées  
-  Impossible d’avoir une vue consolidée sans une architecture centralisée

Face à cela, le Lab 2 propose une **nouvelle architecture distribuée et évolutive**, inspirée des principes du **Domain-Driven Design (DDD)**.  
Dans mon cas, cette évolution s’est traduite par une refonte de l’application vers une **interface web** construite avec **Spring Boot MVC** et **Thymeleaf**.

## 1.3. Objectifs de qualité

1. **Simple** : l'aplication ne doit pas être complexe. 
2. **Robuste** : l'application doit fonctionner de façon stable même en cas de problèmes.
3. **Autonome** : le système doit fonctionner localement sans dépendre d'un réseau.

## 1.4. Parties prenantes

| Rôle                  | Nom               | Intérêt                                                              |
|-----------------------|-------------------|----------------------------------------------------------------------|
| Étudiant  | Vu Minh Vu-Le     | Conçoit, implémente et documente l’architecture du système          |
| Enseignant            | Fabio Petrillo    | Évalue la rigueur de l’architecture et de la documentation produite |
| Chargé de laboratoire | Hakim Ghlissi     | Fournit un encadrement technique et un accompagnement pédagogique   |

## 1.5. Éléments à conserver, modifier ou refactorer

Le tableau suivant synthétise les décisions relatives aux composants existants dans les laboratoires 0 et 1, en vue de leur réutilisation ou évolution dans le cadre du laboratoire 2. Cette transformation répond aux besoins d’une architecture distribuée, évolutive, orientée DDD et prête pour une interface Web.

| Élément                          | Action         | Justification                                                                 |
|----------------------------------|----------------|-------------------------------------------------------------------------------|
| Modèle MVC (Console/Controller)  | ✅ Conserver    | Structure claire, facilement migrée vers le Web (Spring MVC)                 |
| Couche DAO / ORM Hibernate       | ✅ Conserver    | Permet l’abstraction de la persistance, réutilisable avec JPA                |
| Interface Console (CLI)          | ❌ Remplacer    | Remplacée par une interface Web moderne (Spring Boot MVC + Thymeleaf)        |
| Application Java monolithique    | 🔄 Modifier     | Migrée vers un projet Spring Boot modulaire avec contrôleurs et services     |
| SQLite                           | 🔄 Modifier     | Remplacée par PostgreSQL pour supporter la centralisation et le multi-site   |
| Gestion simple du stock          | 🔄 Refactorer   | Introduction des entités Magasin, Stock central, Logistique séparée          |
| Domaines métier non structurés   | 🔄 Refactorer   | Application des principes DDD : séparation en sous-domaines fonctionnels     |
| Diagrammes UML / documentation   | ✅ Conserver    | Le format Arc42 est maintenu et enrichi avec les vues distribuées du Lab 2   |
| Pipeline CI/CD                   | ✅ Conserver    | Reprise des tests, lint, build, publication Docker, avec ajustement si besoin|

> Application du Domain-Driven Design (DDD) :  
> Le système est désormais structuré autour de trois sous-domaines stratégiques :
> - **Ventes en magasin** : opérations locales de caisse, panier et validation  
> - **Logistique** : gestion des stocks au centre logistique, réapprovisionnement  
> - **Supervision (Maison Mère)** : tableaux de bord, rapports consolidés, indicateurs clés

# 2. Contraintes architecturales
- Le système doit être développé en **Java**.
- Il doit fonctionner selon une architecture **client/serveur à deux niveaux (2-tier)**.
- La base de données doit être **locale**, sans serveur distant (exigence : **SQLite**).
- La couche de persistance doit être abstraite via un **ORM** (exigence : **Hibernate avec JPA**).
- L’interface utilisateur doit être en **ligne de commande en console(CLI)**.
- Le système doit être déployé dans une **machine virtuel**.
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
- Le **serveur** est représenté par une base de données locale SQLite, accédée directement via Hibernate.

La communication se fait entièrement en mémoire locale. Hibernate est l'ORM utilisé pour mapper les entités métiers vers ma base de données relationnelle (SQLite).

# 4. Stratégie de solution

Résumé des décisions clés qui structurent l’architecture du système.

## 4.1. Choix technologiques

- Java comme langage principal
- SQLite comme base de données locale
- Hibernate (JPA) pour l’abstraction de la persistance
- Maven pour le build
- JUnit pour les tests unitaires
- GitHub Actions pour le CI/CD
- Docker + Docker Compose pour le déploiement local

## 4.2. Architecture globale

- Pattern MVC (Model-View-Controller)
- Architecture 2-tier : console Java ↔ base de données SQLite
- DAO encapsulé dans le modèle, utilisant Hibernate

## 4.3. Objectifs de qualité visés

- Simple : l'aplication ne doit pas être complexe. 
- Robuste : l'application doit fonctionner de façon stable même en cas de problèmes.
- Autonome : le système doit fonctionner localement sans dépendre d'un réseau.

## 4.4. Organisation

- Documentation basée sur arc42
- Décisions techniques tracées via ADR
- Projet réalisé tout seul

## 4.5. Vue des cas d’utilisation

Acteur principal : Employé 

Cas d’usage principaux :
* Rechercher un produit
* Enregistrer une vente
* Gérer un retour
* Consulter l’état du stock

![cu-menu](https://img.plantuml.biz/plantuml/svg/bPFFQXin4CRl1h_3u8jjWW_DNmWu975A2VJGrabwwMMY7M-KQgLbD0erfT1to4l4p-4toPD4ygebNWU23mlYuvjlVZFIddf1bb2PRaUq688BZILZLH4K10ez5FqWscYFefs5vx-FC9rxMJurZfByIg-24Jsgi4IlhOV1TxeTTZlT3Yfn31-hshWv9O592TeoTkgTrRNHgicDVfZMh6q9vyx93pkuj48yGqLmTJUlo13EO0Ucn0zQaO_7ymluuhoKJDElduSz-9tY0Vem7ezXia7-2ZOUQtP5qEAJELK5Jt7O2FMDr1rn-Y3OVxOUnBZhoOSrvyHP7wUEnZYC5wT-WY8hZ2RRTxS0yl0GQscJv-pU1Lc30GnGezN4kS_UMi4D6bsi2gZ9pAxzhMYOn0NUnBcqMdI3qrXo-i6R32Oc_TzquxVBXSyrX-yICTQrOu41_OBZT0595BTQnJq9pBH5Czo0_BHSn3QSCNE6YjVb8jPoyMhTcUIes_cJOF8xXT7efBdkTM6tBUorXVssSD0M3jl2KMfyIhPO_OGl)
# 5. Vue des blocs de construction

## 5.1. Boîte blanche – Vue d’ensemble du système

### Diagramme
### 2.1. Vue logique
#### Diagramme de classe représentant les entités et composants principaux du système :
![dcl](https://img.plantuml.biz/plantuml/svg/lLRBRjim4BmBq3yiV8cTmYXoDP0WG9n3WQJ8eqZFZBGMY5Aa8r95YwB_qcFz7VwnBb-Ig3ZEeqKl0SnPpixExb8vrOOrfgr5dXbk1CAIiwfXTOrG8XI2QOqmXqjMCSqbVCPxcDwr249L_A75MPxbsG4S7n_3kII2xKR3kk6ou6icK5l0HZWUEA_NGdr7-95dG9oyX0LmQVmlgMhwUMleSXLn9CeLPzAaoDp-VGSLcW_bTDQJkAEFgdPdVNo8xCYcinxzsQkB9RvGtKDBld5i_bxQTM1Ai86mgqQLBJSl5LOmWvLgE8xEgItVw6Wb53DT2RPdEE1xY_wpc70NU4LPtNcNDzRiKlSv1WNVKQkUc9AISdbu0HUgO6991nwgbJH4-aaP9f8wzzQqZ8GEIIWTVZbW7TOnbhktIQyrUgyxDsXKspnjJy37_ZphWhytjk5HulzNwArHnPUzM_dlqvrHBnkQdxtfbbWpMIAEdG_dhxF-UaIwipS1tDaS__hUe626AwdjittFwD4EJ-z1U7Qt6sYs6oOuGL_jn6f00uthgSwp-3PFZYOa-0GcXnE8YuQh5HQ67lsJtN7Tn95rjXidFg-_BlXsWx3z1Qs4sZSpppnaxpeEuefzNj05R2D5OBOSdY9epxE0T3AEUhc5aWKsSgJZS3vFDtLQg7gjDBUkpJf9n6CZk_9ZjAEbLvg-HQas6vrkneXue3Gof_6FgIzu9T5rqbNxtdUfAum2oSorlnwKHxAXfuC4_5uuFzBCpmHRAR4lpARKTaNeqqy3c6SH6Kv2ih8B81VqzdVPaSKSQvf4RSie6GrimfemxD3dYLmK565P5sumoh1zjCkgeAzVRz5u-f9DtvdZoOZBo95dfoXBzt_J7m00)

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

| Service                    | Rôle                                                                                     |
|----------------------------|------------------------------------------------------------------------------------------|
| `RapportService`           | Calcule les ventes par magasin, identifie les produits les plus vendus, retourne le stock actuel. |
| `StockCentralService`      | Gère les demandes de réapprovisionnement, filtre les doublons, affiche l’historique.    |

| Repository                 | Rôle                                                                                     |
|----------------------------|------------------------------------------------------------------------------------------|
| `ProduitRepository`        | Accès aux produits, requêtes de filtrage par nom/catégorie, stock critique.             |
| `VenteRepository`          | Accès aux ventes, calculs de chiffre d’affaires, produits les plus vendus.              |
| `RetourRepository`         | Accès aux retours, quantités déjà retournées par vente/produit.                         |
| `EmployeRepository`        | Accès aux employés.                                                                      |
| `MagasinRepository`        | Accès aux magasins.                                                                      |
| `StockMagasinRepository`   | Accès aux stocks locaux, recherche par magasin et produit.                              |
| `StockCentralRepository`   | Accès aux demandes de réapprovisionnement central.   

### Motivation
J'ai pris l'architecture MVC, car je suis familier avec. Aussi elle offre une séparation claire entre l’interface utilisateur, la logique métier et la gestion des interactions utilisateurs.

### Blocs principaux (black box)

| Bloc         | Rôle                                              |
|--------------|---------------------------------------------------|
| View         | Affichage console et saisie utilisateur (CLI)     |
| Controller   | Gère les actions de l’utilisateur et appelle les services/DAO |
| Model/DAO    | Contient les entités JPA et l’accès aux données via Hibernate |
| PostgreSQL       | Base de données locale relationnelle              |

## 5.2. Niveau 2

| Sous-bloc     | Description                                      |
|---------------|--------------------------------------------------|
| EmployeDAO    | L'employer qui fait les ventes, retours et consulation du stock        |
| ProduitDAO    | Accès aux produits : recherche, liste, stock     |
| VenteDAO      | Enregistrement d’une vente                       |
| RetourDAO     | Gestion des retours et ajustement du stock       |
| HibernateUtil | Initialise les sessions Hibernate                |

### Vue d’implémentation – Organisation des modules (Spring Boot Web)
L’application suit une architecture **MVC enrichie**, avec une séparation claire des responsabilités :

- **Contrôleurs (`controller`)** : Gèrent les interactions utilisateur (formulaires, AJAX, vues Thymeleaf).
- **Services (`service`)** : Appliquent les règles d’affaires (calculs, regroupements, filtrage, validations métier).
- **Repositories (`repository`)** : Abstraction de la base de données via Spring Data JPA (équivalent DAO).
- **Modèles (`model`)** : Entités JPA mappées aux tables de la base de données (`Produit`, `Vente`, `Retour`, etc.).
- **Vues (`.html`)** : Templates Thymeleaf côté client (non inclus dans ce diagramme technique mais couplés aux contrôleurs).


![di](https://img.plantuml.biz/plantuml/svg/dPL1JiCm44NtaNo7KJU0K2i4R8ig5POLLIfrRn8hM7NYo7QGAeJK5GU05KTWpmrwavu4awHf6uyRODKc_zkPFtdZZBIXoXIf887XHZ2O5mpYpUg3fxbOVwOiCzHmcS5czGxtAg4PrzMDc6b8PLm8-tiwcO_X76PCFV68sNyXowNcHghb6Gb8S0B3uH2cD5hGnDhj1GdowWfwaKm7GYRNLnS3aryE8faP9OLWgWVF90281DKQfie-Yvjne-wakIsIOKB8Z2mKfioCZ1PZkqvHWQwaUIuL-hHRgXyV95NnllZoazANI5TjEOXJFw3k65vO8nxTNJMjnlgrUn6jVc52rIzCkRCqo2MwQ48Jgdc6IMMOBhqEkwyVpMkPOmABsWhOZkM4QVcTNQfbpkqw3vuIprOdggOWPA8S80qbwUilmGgbOVFw1ZKezO0u34A_V-E0WvDSMb6MVb56GnJVbk8CNxa_WbXh5FzYSwZ4J0Y7c0rXplQpTHfKxNIGHsrESQWBIKe2vBp0j_GPVLFaywDL7C44sReBgdyiyN4uspB_Grxea7U8k-TTk9xE7dIOVTQEuFjMt-56WX7BulAB-mq0)

## 5.3. Niveau 3

Ce niveau de détail n’est pas nécessaire dans notre contexte. Notre application repose sur une architecture à deux couches (client-console et base de données). Aucun sous-bloc supplémentaire n’est requis à ce stade.
# 6. Vue d’exécution

## 6.1. Scénario : Rechercher un produit

### Description
L’employé accède à la page “Rechercher un produit” via l’interface web. 
Il saisit un critère de recherche (ex. identifiant, nom, ou catégorie) dans un champ de formulaire. 

Le contrôleur `ProduitController` reçoit la requête HTTP avec le paramètre de recherche, 
et appelle le `ProduitRepository`, qui exécute une requête JPA personnalisée 
sur la base de données PostgreSQL. Les produits correspondants sont récupérés 
et envoyés à la vue `home.html`, où ils sont affichés sous forme de tableau. 

Cela permet à l’employé de consulter facilement les détails des produits 
et de filtrer selon les besoins du client.

### Diagramme de séquence
![dss-rechercher-produit](https://img.plantuml.biz/plantuml/svg/XLMxRXD15Eqj-H-ktE0W4WGb8a0m1YAPLsoHXcOoUsqFcfrPvc7YZfQQYewEc9e_s3_9bt3lUBqFAq1XoMlVnpddddltYG_2XPZhVY-ee16UJQTm_UKRdA5SekCFH0E5irbK0ORJmYcpWAVM1ZZ7YuD-hz-x1SV7nt0Y0qO7_BzKXT3yOxydPB0EndcXxHgRu97D2-k52PwhR4CB0g8urGGOl1OhjH2f8WSCPyjrZXh5_600miFxY7i9RxS8HzO4PxL6b-AQnvl3pv1WA8AuJk7ysEzb8eWBuP6Yh0yBXzDtam5Z9-3bro0AX7lm0AP2UOMGHP1EXVBAqNS4jzCDc5D570uVCMHA4bAMLnHM_e2xbQWUXY_6imEMPdBzvJkcb783Dgwef1PGYCLUQNgeapCielAWALMPW6uk9C9dl630t9DQDoeGWfUpsLkWXat_nvyYklNnqT5HmY2eqQTO_We8w0EWMJ4S1454kL3vM_DOn0fbiePm8Y-l0c4Tf4836Ahi3XYR4mMG8fIRXNKAAwzCi5F6OVdJahDqgss7Q-f3ngn5zgpxttkqo19rdYYHRieLwBmrHcZbcPH7RKrTc_6TQX15WLI0s27avIOiRPRcx6gta2emjqO6HTdPLXWqaP5N8Z1POucCKujb03llU75yoMD63m_wlKEOAvCzNPzcnEcWUNvjSquHoj36dIwCTJWYLmw9T2jgH4kIgDyOAtV__h_kYSd-NOlyGLNGPC2yRXYbN5gQJ81iRKIkI8OSQTNOc2maIIE9y4vFpn8AciEQpm2m5dEboKMUbKjZe_4r2yWBDvwCHpEu3HyCF3zxywhPWVENuxCnx0oCdK5TbXiPmtQOjSEuu8gSmBPBgyu39bkG9LB_Lj_3naKJvSF3wd8yubQjC8V1HcVImfAtaejzWr1QHzGAQp9cA-9bxRaMt_rLTUN6Hnt4VhxVqlJj039r813RPVLxozezNlUfzCbyhkHoUrWwTVXgjLUelbmrmS7IvdYq3BaULCjPqQtPtk0DyT7o47It6Q7CRLPUqQvXXLub1LSHkvInFcS4Q7TLwPmc6jYkIT6UxnBTS7fVnAshoitkl7PkP9tVYg_kJMjsf4LSKGE6y8JydzwMVm00)

## 6.2. Scénario : Enregistrer une vente

### Description
L’employé accède à la page de vente via l’interface web et ajoute des produits au panier 
à l’aide d’actions AJAX. À chaque ajout, le `VenteController` délègue au `VenteService` 
la validation de la quantité disponible via le `ProduitRepository`.

Si le stock est suffisant, le produit est ajouté à la vente en cours (stock décrémenté). 
Une fois tous les articles sélectionnés, l’employé valide la vente. Le contrôleur 
enregistre alors l’entité `Vente` dans la base de données via `VenteRepository`, 
avec ses lignes `VenteProduit`.

Une fois la transaction confirmée, un reçu de vente est affiché dynamiquement à l’écran 
dans la vue `facture.html`.

### Diagramme de séquence
![dss-enregistrerVente](https://img.plantuml.biz/plantuml/svg/XLNDRjD04BvRyZkCSag0f09kI2bDMvSMIXDYat90GbjxaYuuNdTt7IcgaBXovWMuGPyZRyAJC5x_coJaaltvPkQRcM_7HqepgPDbQ5kQwn3Xr7NXpxSVu4GI5rnfYHAI2665aKRekh7aqGAEXT1mZJSzsx8jvciXmLd6eLYZRSNaaFiyPf66pXLRyGNJcCWK3btlTht44Dcyrm6cO9Qq3MPfd1CHQId24AL1LTjjO1VbYljeaFcw1SkfJJ4MYXFNjS7MJbluYHH1mdKBNplTHgE5BiziAs2QtJ25v5WelP3elhlif8KR3c7pNRCOuIcyW97lRtuhO0cGLqvb7mvjAoS9_SEqLeHYAIf0sFo4WnpNVULur8Zqdc1LiGXzxdaJeEi2QrkrwtxfLA8MYQGkPwZ1hLw6m5Revst727m2g2RfPodfpoBHG7pYh3hRIViXlwD5nw0fSfbZPm_joTWjUH-m883xFCP5y0Jk4geyr_YrcT5XeO2qKbcml6tTBSPKj09EbhN-alMSHy7n-YAex0XTXwGMPJl9m7KkdHCF7i772CwcuxT5HLHwS7tkJ1tW0GpX9RcfxFh_1CvthM2Dp4e8lRrGWzB2_m8gcS-veiHi2-YtCx4bLqOzL1sPctQBWa0VxdgP_RvKtqzEHvxJID0bZPLUXZ0O35eqsYqkz6Awj7cmBGo9LyQ7HxkIQNj1AR58Wrn4mGw1Dv8kdisACnYz7dteDMNzdF1dF68X4T1SHFibFIDSGDv2bhsL_olQmDJ1AZUyD_izEiPg4fkXqZK6JUtMPbbTkvWT6-tMqELi8goYPrvwh2xQBR-uSfsfHt_UECjJmUDiyIak1TkKTJtA-4t3UIC_NqHpBfUcrZMQxGfcaoW7RnumRzg8XEBViWMQDksORW6Nw9jUhb0gcDCNAv5enbpUhgg6wUGmFf4CDx-ITBWLpKrZ7f7ApFVoBm00)

## 6.3. Scénario : Gérer un retour

### Description
L’employé accède à la page “Retourner un produit” via l’interface web. 
Il saisit l’identifiant d’une vente existante pour visualiser les produits achetés. 

Le contrôleur `RetourController` utilise `VenteRepository` pour retrouver la vente 
et affiche dynamiquement les lignes concernées. L’employé sélectionne les produits à retourner 
et saisit les quantités via un formulaire.

À la soumission, le contrôleur délègue au `RetourService` la validation des données, 
la mise à jour des stocks via `ProduitRepository` et l’enregistrement du retour dans 
`RetourRepository`. Une fois le traitement complété, un reçu de retour est affiché 
à l’employé via la vue Thymeleaf.

### Diagramme de séquence
![dss-gererRetour](https://img.plantuml.biz/plantuml/svg/ZLNBRjD05DrRyZzSTPLI6iHZXHHecvWsecr2RDeD4fhQD-c0xJ4puuZikcNDYXqxo7VuJ_ebtF7RRa0i8jipvynzd7CdHqepgTCej2tDTOWmSLsul_iEfzbMeeGq1ebQf186RY9vl88J8JHSuys-RTaMyxMGu4H9A3PeMmcTnNsUi5Z3tYLRynNJI5o2my2xtKGO8blkxm5JS9Ns2Oiyq5Z4MeemH9d3ci-TQ1VbclkOGylt7kuAOuqBJ8JYbEkcY5sjxJop1suMU-Yv547ATG_UMhMjW6bsmnGIM2YzakY-EzypZHkD8FkcMOBm55x2iUzdlnLaFo5aaB0LGe1LtqSZsoeR38UlJDk8m0o1CCHuKkbpwdYaYTadMDCsGfzvtXnekyBQLckxFhFOYn6MGaPfoBYiSXZUQcEGTjRFYB10_nOb_KosbFZQT7LtlefnQX64ztS_ffCs_1y9psTkdJ4zobXN1QjNGAKdSPSy3ausqs2GdpyDJ4_gRS8sGX3OTSwTiGUFu4CCRnQpYx86-he-SnOEy016yDgsMgJ3NhmoeLQCVZMxKovqN2wvQM282fB2EAhRddRJdvkCiss8lkOY9boBH4we1FoBPrJD98rhtW5yISdDN6VRxY7_gyoQXJp01vugXv4e64jSSQNfEYb6gLBcCKGiAST5aKmLnOZK316T476LM_sJyRcfMcdXVrOm-5hNGVGsfw_p-_das7EQgabWb-QXB9rC2yFXi9D1C_MvVTSuAEVi04A-Yb4L08hRGlR3JYzTP-7Hmvj1JIzUFfQvFF1M9_3iRVVyJcDz4I-vZ9YHj6kK5xGxRktcXgYBwsjQPbRhb6sxOhPkY80kXS9y8YtDQsuhYTclj3GCeIlhDVxE0L3TFhPrX76G_-dy0G00)

## 6.4. Scénario : Consulter le stock central (UC2)

### Description
L’employé accède à la page “Stock central” via l’interface web (navigateur). 
Le contrôleur `StockCentralController` traite la requête HTTP `GET /stock` 
et délègue au service `StockCentralService` la récupération des produits disponibles. 

Ce service interroge le `ProduitRepository`, qui exécute une requête JPA 
sur la base de données PostgreSQL pour obtenir la quantité actuelle des produits. 

Le contrôleur retourne ensuite la vue Thymeleaf `stock.html`, 
qui affiche dynamiquement la liste des produits. 
Si un magasin est sélectionné, l’historique des demandes de réapprovisionnement 
est également récupéré et affiché.

### Diagramme de séquence
![dss-consulterStock](https://img.plantuml.biz/plantuml/svg/XLHBRjim4DqBq1q6NZajukw3oldOQbF0QLtBQ3O50beQoqGfKYKf0zxb3brrrsLz3jqa9-b8fcL5JjoTD7nlvl7D3A-CPTeMcV0zowr064SHF3t-Wf6IfX0MDL3GM1N_W1YbrKn0FyerbodSA6NX7XTdlkTxBBPAGvZbGctGztBAocEUCscXzvcjUSei5hg2Gt--scGea2tFUi0CV2kwXAYgDzgL8o5MAo5Gr-33xmbEX7hDOwm9xhk3dcgL5DpEC5U6a_9DZMr5E_WxbZB3PGVVYlfUmYnRC8EKN1cRQeo-JdgLDK40KRaL65kk92HC6X1FZx-v97UNB4O80jzppi7viF83Be35SVatGIZ_mBkT_zybh3c336K1gW31KVfU1IREmHQYtixdK_WOpXtnCjl9_9G4Wy70APgebFyaRbPkBITMfQ6LX58wuoXVYjOvOyRSv4hoXK3JfxxlCGHlUKUK9PV9jH0LfdrGuHfp21Q5at0qXpVLtJxCljn1xkeGxO0xFqe_uSQ-Tw5XfyGpwHrWexThMGT7yomiED_0DDsJvVQaJMFCc4pGJ9bsiz5lp7zkLtjS3rFbdBlPlCX-cV9VJ-iXUDXlBqNlRyDPkB_T0qyWWCiZmryLM5lLFXs-Bksu5rtYoOPqm2WJDxtNooMFLop5exbjzaUZLOMMj8xr_GShcmbQsZN6pP31MqXSmzmY4Nk_Y1Uk20AjDJrMnO665bOaJ-bgWof5LwIiVaF_0G00)

## 6.5. Scénario : Générer un rapport consolidé des ventes (UC1)

### Description
Le gestionnaire accède à la page “Rapport” via l’interface web. Le contrôleur `RapportController` traite la requête `GET /rapport` et délègue à `RapportService` la génération d’un rapport détaillé.  

Ce service regroupe les ventes par magasin via `VenteRepository`, identifie les produits les plus vendus, puis consulte le stock actuel à l’aide de `ProduitRepository`.  

Les données combinées sont retournées sous forme d’un objet rapport consolidé, ensuite injecté dans la vue `rapport.html`. L’interface affiche un tableau récapitulatif par magasin avec des sous-sections sur les ventes, les stocks, et les tendances.

### Diagramme de séquence  
![dss-genererRapport](https://img.plantuml.biz/plantuml/svg/ZLRDRXj73BuRq3iGlbGEx1RzknIr4LjItGPAf4fAYW85dD4kjPfqDBEP7y6-vTfpJxtrLgldli6-IPwavEnA-sF3xKcxioG_ykD7ZfuxBwmF6zKxyj8hXE5y3Xy__0xNnKuNEuiMWWOhyjnO3udHpYYP5Zj8qS4MjQUV_ZotKcTmPOo7dt1vqZlg7Nq2v-VdS9bu31RuUo9peVYmToGIRonSe_FIQ2sanTfZO3QvSK9xnw5A-vnIbEolFHo_4bkPYHYM3VgBzTq65OhLoJ481sy2TXncPV83exqrIg6DTlNhu-Ppj5kPOBIjdZk6RxZw6LAsaigvAz7tPntRgJLfaBvZtJZjsC-zINvzAJBXfEuunK-bIogyM0g75DyudrcS_pW-PYg9n-8tBtA4p-4RQa9It3ie_WJrySCVaZAqAv4WCBND9i3P1TD77e8zKcINp_Qzxr-F5YVSfJ52rlHAb7nFNYv4gInDy4Q32VoY96hOeEQQH7X4HfmiGn9otH1AuFl5OWe4U82Fm08ilW_5tnw14g2NuY_o9UA0X9kmV_6FOc6yVT3pjsrQlY2CMR5BGauQ5rnBKz1CJ2CTIgvI07bbg96ceWBexnbfebDTghZFA3KbOgtUXgJ4g3aWEbmLT5Fil6GTzexsEDo9lPG8T2LrUgdK0MtlQSiIxgaBddYdr5LT1MgBcIJmOXVVu6eOwJx49P1QDuGo7ur7WmKyW-zcavTL50QSxKbdCxnbJ4ynOIi5l9XUGcweQBprDigXpe3H6W1ddQB6bDktyU2YbLMBRZPYTZSvKKfFQIjwtT0loVQ7b3x8bKpgXkRbXD63oOFY_ChkFi5pDPHkZ6wgWgFFQN0byvTvZWfI6gAWiTPYZjRnC2Zfc1QIliY8zAnC8x8ZgzJGHdwM9DypXwmkzxzwKjLn8zDJ64nUlrhqdvt0BxhHgPlAX4-lPvFNKxZwkU76nvFPS3JZunW1XgFvuCa6BKn-8F2_EjGYcqlCYJlEZCXhT-ehyk576e741-QJh1qljGUjEQowxWZGSBgWVDrdBRSemYqzbw6gs2ukXqsvH-Dy7I8-BVuOvERWs26fcLTKSNDTNxHGMbJ5ysvADJDVywvs3ZTB9RAIqfHqLOwlvs7FgMviizJTdCF5vD4zszeRrL8YjT2IDijt5BrSKe50G6mngORij2NLKuY6L0GdtjoGXvl38YqYozDHvl3fsczK6N5VooCRK-ftIFyKU8tlSq-5PY71ngJ5FVqICkgjaN6fRWdhSEyVxX1AedMTqJssecsmRWIjj-C3RbftsbQwmFECJ11TieH3-_fE-pNlQhxki8oMKUWr7pZMdYUz2vt4lpFFMVlyl-fV)

## 6.6. Scénario : Visualiser les performances des magasins (UC3)

### Description
Un gestionnaire accède à la page du tableau de bord via l’interface web. Le contrôleur `DashboardController` traite la requête `GET /dashboard` et interroge le service `RapportService` pour récupérer les indicateurs de performance : chiffre d’affaires par magasin, alertes de rupture de stock, produits en surstock et tendances récentes.

Les données sont agrégées à partir des `VenteRepository`, `ProduitRepository` et `StockMagasinRepository`, puis structurées sous forme d’un objet DTO envoyé à la vue `dashboard.html`. L’interface affiche des graphiques interactifs, des alertes visuelles, et un résumé global des performances de chaque point de vente.

### Diagramme de séquence  
![dss-visualiserDashboard](https://img.plantuml.biz/plantuml/svg/bLZBRXit5DqRy3zSU1EfaDFtnaYCE8wRfB5hLr8S52ZWK3EKH7HU9ZbgZA90jbrtrLsNTjVz0_r9lgJdafodbBfT6F3Cy77kkUSUNkg9iKBRCarsTwooYQHdambzUFyxNIfJYaGPgIcHXWgfvxbEHHRX8SPVAXR2gCpGO59ebIteQPvRUYDdmzsTtPq7zFZnOpgAh2mryVT85IBXbxix8hAvfkVIM9LdcL1QylXJIOlMAzAoqEix8pChgSoej0fWX5jFt8ZCugMaLIaf_l3-3vaPcSw0tfGURbwixxJWrRfe7pRWZlEqo0rMCeoQ1a97IsLbP4liVdPvJAsuXXvv0QeKHuBjzxuJAxNmWDpywV8cbOaKy-4U2KENfGlh9V3hkOWa_On1FehrdQGYvq3cSnKj0yCgYrNarZEq0lao0RTM82fHlXlHGejYgQvBQKOa4gcjD4CNJGVKM1H5hkrndbcT9nZci3IF3CazhVzE67c8SS5iOJxZqFAwNF-5vUd5T7f1YG9SGwAaW0LyqYpNyKStdqYzKf5qEuV_UTlmBwNhEwiGCxBB-lBRajK2vAT8zuWIP5Giy7tXCaZI-bUPI6wCTFcC8HF6jGdYaXKpbaYjWinkV2wgTuoZ-LQd8sGZYjPtCP21Rrhn54CoqtAX3FXY4WPGo7oklUQGF1Ogqc5b5hl2605gz71BMYvq7fVAzc2rtZAmmNdXv2yJmiHX6oYgBvNMybfWFzAvgyis4IWojvWXemXlWCOml4qi4vj7FvtvUkW1Sf-sy1HKuWdYHPYXJArOcGp8y3o7R2cWqAhQtDwnW6u4qhPtaHkxq7Bo_UaUh_zKk5ms4MYPY400114X0Lg4QcdLZT1ayz8ugKPD2Q-Kg60-muxqxSMHsnw5llxDYaBIvtG0GuhMjzl4J5pURKkY_KCkOSpXg6ynRFqdVHeBivpb0kC7pq-cmss-5ILSfCw8zgRTJVO8v1cdPTMn1IU_V0OTASrcnyPNeaveaUGpaRX060mmDQKCQAv00QG5p44ITH7x8S371PQP5Bf1z3PFmJHWShqn6r7B4jvkUCZR7fDVOEin95RM9ainrgXguq5L6IvPGGkEu1PuX58PmW48jCT-zb6OUY5qaEYWOhcD4GYTIDZnmoeT3jbGSei3Xql4b_88d2nqpfZRk5ne5HBEUMKLW3M79T-3QrpnZ1JR6t3pV7p--eAU_a3o7JDaQw_VKXhDNjYugGxiF3avFJcULeElL3oYoUkpGGeAeFOh5zkGVipec_7vMULLUAntRsPsKFBglJCei18EeR23sQ2e1M-_HzCf_F7HBs7mo1F_ws4GeYjzBL6oEfFkpCRcdg_2YsJGdTbAsR3FKaSqOowIn0g0zOS5dhrKZ2yW_yqWeWe0tlgWczE6yZ8Sms8XUph_SelE-IZqfszZVFT8EzYyESd6PS7TnNavmI7FKxUAEvPeisBfyb7jDQZj7T-kIvSY89SP6urRQjXNSctcGSb7IL9lLnMDio6TBsJZgHksxi_6ed2-fdqAyuKpEOoPgmHa1kgQBJyYw4-yODt2Lo7l7f9lkzZGa_MjzOVy1XI4E-kT6jl4sOxSYRDzv1rsO7PovTvx3DBwHd7Q2ANgMmvGsWWvglLGfmRmjCymvwPAJ3SfzMQEE6w3_0p3NKurnVJILB7V9z_8Kg5BF20Ulqz1Njm1E8aek0_4IPynd4T-DAMUyNsgDDWTVbWD4--wnV0L7v-j3gnkUcWfPp4EbDYNypt5CAsclU1P_wf_3hRTymMROsT2jozK5XnirRsfrC3kitRtp8u-yQjjL4Ux4CFczHdI1VBVxFpDoSch0VeXUULM6_vVK-_FRrjijv6gxRPf9TfPybUYokgnM5nkBQHDbm_nZghPxFCVC-X68G3d4BlRHwTfbUNAjI29uXfpzjBST71KO9W3F8WGqSmVis7POS-jlsQtvb32xSafiKv_ltMg8VGRmS7nKNt1QhNsZknfNd9EadO8hLux73RLQUHhMijibTzKTwYMpXtyTgNK7MUTlxghUxYqQK9Y9QFsDJ7alu3zeiyxEtLzdcOAf1_RlrR2sb9yibQhMOdCWwiqZzUtQ9VgsslJR09FfoC-gEwhzEhYPSqHn-YJu_5qcbjMYUQEiziZ7tW7uhhVSg5gznxLm9RBHbgvtXM7eeVW3uTXCtHJU3sF6ZINQVThnHFcZ7y2-GS0)# 7. Vue de déploiement

Cette section décrit l’infrastructure technique nécessaire à l’exécution du système, ainsi que la répartition des composants logiciels sur cette infrastructure.

Le système étant exécuté dans un environnement local (machine virtuelle), l’architecture suit un modèle simple à deux niveaux (2-tier), sans distribution réseau.

## 7.1. Infrastructure – Niveau 1
Le système repose sur une architecture 2-tier, où la couche cliente (console Java) et la base de données PostgreSQL résident dans la même machine (ou VM).
La communication entre les deux couches se fait via Hibernate (ORM).

### Schéma de déploiement (niveau 1)
![dd](https://img.plantuml.biz/plantuml/svg/ZP5DIiD068RtWTpX9HkJHIYu52fDsuKKOwE5hjnC9nzrC9alp4oA889ty1Bs7Bs99t6cMlyG4OQi7jxdcSd8Ec5StpPnv9Hh25CbraQqBQ7sPxVj6bRKadPuUNn2OUIDSYHTRmZ7kLDauYYEZZ0S3d71rDUGZGfqkYpHi1H-adjKaomWSQJPpoQby3EQtbdw0cS9xkC8aDnshCDGKdHTewHmYZMg-U9QfpEn4PYmjkpNSvJBIPR4qSFF4ajcGxt1U8gqOklMXXQNvx-67D1qolAMpA6Y5awiXFNWE7p3Yx3MkorPV0v-L_iiJNUXTNvLOgC4pjIAZ1QE1hEgU9F_yqx8yzE_rJX18UNksUvDmPX-w3205sOXRKIwtl_ZLm00)

## 7.2. Infrastructure – Niveau 2
![dd2](https://img.plantuml.biz/plantuml/svg/fPD1IiD058RtWTpXaOq9KheNAaqQiOPMemdgefjfyXe79pDnCgc82Dx1KxJNw4qyYPDODQr52i8iilZd_x__lKo_L4IgAcUceQXY23ubcIHvZf3gireKJ53CaIjuVtq3joWOJOYYWiCDJi6E2abv1fuG2WuX5ANA94QN8ySqJEC0UhqUd0kiKaWOhKriJkOq8meh2OyuTMg5QN2Xmwp1RudES2nuDWq0ZKbd955WdN65KdyYL8eoMZRI2H_6uv5c2CYSH8caXPfmExvxof4XcNNTmBrrBDDusGOhKSxhAF9LRRRHQeCmsg7rHNAF4ZhGlhHcJaHU25wNihOcfzdQenjV3tR2JKdvkGZ1-Mg1vIuOpsz1j3QmPaY98esDvMsR_OWtiw-GDQ3hkFyC73gTuxtAmv6EVQXMIwN1Xd4SWdrw4dV3YoXk_gMzF9gmlshgbC3t1jyRzlLm80ZT9gAFF6rUmWS0)

### Schéma de déploiement (texte ou diagramme)
# 8. Concepts transversaux

Cette section présente les principes techniques et conceptuels appliqués globalement dans le système. Ces concepts touchent plusieurs parties de l’architecture et garantissent la cohérence du développement.

## 8.1. Modèle architectural

- Utilisation du **pattern MVC** pour séparer présentation, logique métier et persistance.
- Couche de persistance isolée via Repostory, manipulant des entités JPA.

## 8.2. ORM et persistance

- Utilisation de **Hibernate avec JPA** pour abstraire l’accès aux données SQLite.
- Les entités sont annotées (`@Entity`, `@Id`, etc.) et gérées via des Repostory.

## 8.3. Transactions

- Chaque opération de vente ou retour est encapsulée dans une transaction Hibernate.
- Les mises à jour de stock et les insertions sont atomiques pour garantir la cohérence.

## 8.4. Intégration continue

- Utilisation de **GitHub Actions** pour automatiser les vérifications de code, tests et build Docker.

## 8.5. Conteneurisation

- Déploiement local réalisé via **Docker** et orchestration possible avec Docker Compose.
- L’ensemble de l’application (console + SQLite) tourne dans un conteneur unique.

# 9. Décisions d’architecture

Cette section référence les principales décisions architecturales prises lors du développement du système. Chaque décision est documentée sous forme d’ADR (Architecture Decision Record), selon le format de Michael Nygard.

## Liste des ADR

| ID    | Titre                                                  | Fichier                                               |
|-------|--------------------------------------------------------|--------------------------------------------------------|
| ADR 001 | Choix de la plateforme                                | `docs/adr/001-choix-plateforme.md`                    |
| ADR 002 | Séparation des responsabilités (MVC)                  | `docs/adr/002-separation-responsabilites.md`          |
| ADR 003 | Stratégie de persistance                              | `docs/adr/003-strategie-persistance.md`               |
| ADR 004 | Choix du mécanisme de base de données (SQL, local)    | `docs/adr/004-choix-bd.md`                            |

## Format utilisé

Les ADR sont rédigées selon le modèle de Michael Nygard. Chaque fiche inclut :

- **Statut** (Accepted, Rejected, etc.)
- **Contexte** : la situation ou les contraintes ayant mené à la décision
- **Décision** : le choix retenu
- **Conséquences** : les effets positifs et négatifs du choix

🔗 [Modèle officiel ADR – Michael Nygard (GitHub)](https://github.com/joelparkerhenderson/architecture-decision-record/tree/main/locales/en/templates/decision-record-template-by-michael-nygard)
# 10. Exigences de qualité

Cette section complète les objectifs de qualité mentionnés en section 1.2, en les organisant sous forme d’arbre de qualité et en les illustrant avec des scénarios concrets.

## 10.1. Arbre de qualité
```
Qualité
├── Robustesse
│   └── Le système ne doit pas corrompre les données même en cas d'échec d'une opération.
├── Performance
│   └── Les opérations (vente, retour, consultation) doivent répondre en moins de 1 seconde.
├── Portabilité
│   └── L’application doit fonctionner sur n’importe quelle VM sans configuration externe.
├── Maintenabilité
│   └── Le code doit permettre l’ajout futur de fonctionnalités (ex. : interface Web).
└── Testabilité
    └── Chaque composant métier doit pouvoir être testé de manière unitaire.
```

## 10.2. Scénarios de qualité

### Scénarios d’utilisation (runtime)

- **Performance** : Lorsqu’un employé consulte le stock d’un produit, le système doit afficher la quantité en moins de 1 seconde.
- **Robustesse** : Si une transaction de vente échoue (ex. : erreur de stock), aucune donnée ne doit être enregistrée partiellement.

### Scénarios de changement (évolution)

- **Portabilité** : Le système doit pouvoir être déplacé d'une VM locale à une autre sans adaptation du code.
- **Extensibilité** : Il doit être possible d’ajouter une interface Web ou GUI sans réécrire la logique métier ou DAO.
- **Maintenabilité** : Une modification dans le modèle de données (ex. : ajout d’un champ `remise` dans `Vente`) doit être localisée au niveau du modèle sans impact sur la vue.

## 11. Risques techniques

* Gestion de la concurrence entre caisses
* Transactions mal fermées pouvant corrompre le stock## 12. Glossaire

| Terme | Définition                        |
| ----- | --------------------------------- |
| POS   | Point Of Sale : système de caisse |
| MVC   | Model-View-Controller             |
| ORM   | Object-Relational Mapping         |
| JPA   | Jakarta Persistence API           |
| ADR   | Architectural Decision Records    |
| DAO   | Data Access Object    |
## 13. Références

* [https://arc42.org](https://arc42.org)
* Format Arc42 basé sur Arner :[https://github.com/arner/arc42-template/tree/master/docs](https://github.com/arner/arc42-template/tree/master/docs)
* [https://hibernate.org](https://hibernate.org)
* [https://github.com/xerial/sqlite-jdbc](https://github.com/xerial/sqlite-jdbc)
* Documentation ADR :[https://github.com/joelparkerhenderson/architecture-decision-record](https://github.com/joelparkerhenderson/architecture-decision-record)
* Format ADR basé sur Michael Nygard : [https://github.com/joelparkerhenderson/architecture-decision-record/tree/main/locales/en/templates/decision-record-template-by-michael-nygard](https://github.com/joelparkerhenderson/architecture-decision-record/tree/main/locales/en/templates/decision-record-template-by-michael-nygard)