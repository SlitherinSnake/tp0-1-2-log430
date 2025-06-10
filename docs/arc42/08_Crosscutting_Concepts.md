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

