# 9. Décisions d’architecture

Cette section référence les principales décisions architecturales prises lors du développement du système. Chaque décision est documentée sous forme d’ADR (Architecture Decision Record), selon le format de Michael Nygard.

## Liste des ADR

| ID      | Titre                                                         | Fichier                                   | Statut |
|---------|--------------------------------------------------------------|-------------------------------------------|--------|
| ADR 001 | Choix de la plateforme (console Java, Docker, CI)            | `docs/adr/001-choix-plateforme.md`        | Accepted |
| ADR 002 | Séparation des responsabilités (pattern MVC)                 | `docs/adr/002-separation-responsabilites.md` | Accepted |
| ADR 003 | Stratégie de persistance (ORM JPA + Hibernate)               | `docs/adr/003-strategie-persistance.md`   | Accepted |
| ADR 004 | Choix initial de la base de données locale (SQLite)          | `docs/adr/004-choix-bd.md`                | **Superseded** par ADR 006 |
| ADR 005 | **Sécurisation : JWT + Sessions vs Sessions only**            | `docs/adr/005-jwt-vs-session.md`          | Accepted |
| ADR 006 | **Migration vers PostgreSQL centralisé (≠ SQLite)**           | `docs/adr/006-postgresql-centralise.md`   | Accepted |

> *ADR 004 est conservée pour l’historique mais marquée « Superseded »: la décision est remplacée par ADR 006.*

## Format utilisé

Les ADR sont rédigées selon le modèle de Michael Nygard. Chaque fiche inclut :

- **Statut** (Accepted, Rejected, etc.)
- **Contexte** : la situation ou les contraintes ayant mené à la décision
- **Décision** : le choix retenu
- **Conséquences** : les effets positifs et négatifs du choix

🔗 [Modèle officiel ADR – Michael Nygard (GitHub)](https://github.com/joelparkerhenderson/architecture-decision-record/tree/main/locales/en/templates/decision-record-template-by-michael-nygard)
