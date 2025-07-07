# Tests de Charge avec Artillery

Ce projet utilise [Artillery](https://artillery.io/) pour effectuer des tests de charge sur les points d'accès API critiques.

## Points d'accès testés

Les points d'accès critiques suivants sont ciblés dans la configuration Artillery fournie :

- `GET /api/inventory` — Lister tous les articles d'inventaire
- `POST /api/inventory` — Créer un nouvel article d'inventaire
- `PATCH /api/inventory/{id}/stock/increase` — Augmenter le stock d'un article
- `PATCH /api/inventory/{id}/stock/decrease` — Diminuer le stock d'un article
- `POST /api/transactions` — Créer une transaction de vente

Ces points d'accès ont été sélectionnés car ils représentent les opérations de lecture et d'écriture les plus importantes de votre système d'inventaire et de transactions.

## Comment fonctionne Artillery

Artillery est un outil moderne, puissant et facile à utiliser pour les tests de charge. Il vous permet de définir des scénarios de test en YAML et de les exécuter contre votre API.

## Comment exécuter les tests Artillery

### 1. Installer Artillery

Si vous n'avez pas Artillery installé, exécutez :

```sh
npm install -g artillery
```

### 2. Exécuter le test

Vous pouvez utiliser le script fourni pour lancer le test :

```sh
generate-artillery-report.cmd
```

Cela va :

- Exécuter le test défini dans `artillery-critical-endpoints.yml`
- Sauvegarder les résultats dans `result.json`

### 3. Visualiser les résultats avec Artillery Cloud

La commande `artillery report` n'est plus supportée. Pour visualiser les résultats de vos tests de manière graphique :

1. Créez un compte gratuit sur [Artillery Cloud](https://app.artillery.io/).
2. Connectez-vous et créez un nouveau projet si besoin.
3. Importez le fichier `result.json` généré après l'exécution du test.
4. Consultez les rapports interactifs et partagez-les avec votre équipe.

> Artillery Cloud permet de visualiser les résultats, créer des rapports personnalisés et collaborer facilement.

### 4. Consulter le rapport (optionnel)

Si vous utilisez Artillery Cloud, ouvrez simplement le rapport en ligne. Sinon, vous pouvez analyser le fichier `result.json` manuellement ou avec des outils tiers.

## Personnaliser les tests

Modifiez `artillery-critical-endpoints.yml` pour ajouter ou modifier des scénarios et des points d'accès selon vos besoins.

---
