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

