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
