# ADR 005 – Interface Web et APIs REST

## Status

Accepted (Évolué vers microservices - voir ADR 007)

## Context

L'application nécessite une interface utilisateur moderne pour les employés et les clients du magasin. Une interface console CLI n'est plus suffisante pour les besoins actuels. L'application doit également exposer des APIs pour une potentielle intégration avec d'autres systèmes (applications mobiles, systèmes de caisse, etc.).

## Decision

Implémentation d'une **interface web moderne** avec **APIs REST** complémentaires :

### Interface Web (MVC)

- **Thymeleaf** comme moteur de template côté serveur
- **Bootstrap** pour un design responsive et moderne
- Pages dédiées pour clients et employés
- Interface d'administration pour la gestion

### APIs REST

- **Endpoints RESTful** documentés avec Swagger/OpenAPI
- **Controllers séparés** pour les APIs (`/api/**`)
- **DTOs** pour le transfert de données
- **CORS** configuré pour les intégrations externes

### Routes principales

- `/` : Interface client (produits, panier, achats)
- `/admin/**` : Interface employé (dashboard, gestion stock)
- `/api/**` : APIs REST pour intégration

## Justification

Une interface web moderne améliore l'expérience utilisateur et la productivité :

- **Accessibilité** : interface familière pour tous les utilisateurs
- **Mobilité** : accessible depuis tablettes et smartphones
- **Visualisation** : graphiques et tableaux pour le dashboard
- **Intégration** : APIs pour connecter d'autres systèmes
- **Formation** : réduction du temps de formation des employés

## Consequences

- **Avantages :**
  - Interface moderne et intuitive
  - Accès multi-plateforme (desktop, mobile, tablette)
  - APIs réutilisables pour d'autres développements
  - Documentation automatique avec Swagger
  - Meilleure productivité des employés

- **Inconvénients :**
  - Développement plus complexe qu'une interface console
  - Nécessite des compétences en développement web
  - Maintenance des templates et assets

## Technologies utilisées

- **Spring Boot MVC** : Framework web
- **Thymeleaf** : Moteur de template
- **Spring Web** : APIs REST
- **Swagger/OpenAPI 3** : Documentation des APIs
- **Bootstrap 5** : Framework CSS
- **JavaScript ES6** : Interactions côté client

## APIs exposées

- **Inventory API** : Gestion des produits et stock
- **Transaction API** : Ventes et retours
- **Logs API** : Consultation des logs système

## Options envisagées

- Alternative 1 : Interface console CLI uniquement
- Alternative 2 : Application desktop JavaFX
- Alternative 3 : SPA (Single Page Application) avec React/Angular
