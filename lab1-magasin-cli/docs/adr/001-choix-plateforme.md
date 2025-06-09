# ADR 001 – Choix de la plateforme

## Status
Accepted

## Context
L'application doit être simple, robuste, autonome et fonctionnel dans une machine virtuelle locale sans dépendance réseau. Aucune interface graphique n’est utilisé. Le client est une application console Java, fonctionnant sur la même VM que la base de données locale (modèle 2-tier). Un déployement facile et compatible avec les outils de build et de conteneurisation utilisés dans le cours est obligatoire.

## Decision
L'application est développée sous forme console en **Java**, exécutée localement sur une machine virtuelle. Elle utilise une interface utilisateur en ligne de commande (CLI) au lieu d’une interface web ou graphique. L'implémentation d'un API HTTP ou serveur distant n'est pas présent et donc non utilisé. Conteneurisation faite avec Docker pour assurer portabilité et facilité de déploiement.

## Justification

J'ai chosi Java, car c'est simple et pour sa compatibilité avec les outils imposés (Docker, Hibernate, SQLite, etc.) et de ma maîtrise du langage. J'ai fait une application console parce que c'était plus simple pour moi.

## Consequences
* **Avantages :**
  - Java est un langage que je maîtrise et il s'intègre bien avec Maven, Docker et CI/CD.
  - Implémentation et déployement d'une application console Java est facile.
  - Java est compatible avec Docker et Docker Compose.

* **Inconvénients :**
  - Interface utilisateur seulement dans la console (CLI uniquement, pas de GUI).
  - L'interaction utilisateur version console est moins intuitive qu’une interface graphique.

## Options envisagées

- Alternative 1 : Java SpringBoot
- Alternative 2 : Flutter