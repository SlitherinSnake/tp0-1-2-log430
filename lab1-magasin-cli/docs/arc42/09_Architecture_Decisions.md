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
