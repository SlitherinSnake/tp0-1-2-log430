# 6. Vue d’exécution

## 6.1. Scénario : Rechercher un produit

### Description
L’employé saisit un critère de recherche (identifiant, nom ou catégorie) dans l’interface console. Le contrôleur transmet la demande au `ProduitDAO`, qui interroge la base de données SQLite et retourne les résultats à afficher.

### Diagramme de séquence
*(voir diagramme PlantUML ci-joint)*

## 6.2. Scénario : Enregistrer une vente

### Description
L’employé sélectionne les produits à vendre via l’interface console. Le contrôleur crée une entité `Vente` et transmet l’enregistrement au `VenteDAO`, qui effectue la transaction en base avec Hibernate.

### Diagramme de séquence
*(voir diagramme PlantUML ci-joint)*

## 6.3. Scénario : Gérer un retour

### Description
L’employé saisit un retour de produit dans l’interface console. Le contrôleur crée une entité `Retour` et la transmet au `RetourDAO`, qui enregistre le retour et met à jour le stock dans la base.

### Diagramme de séquence
*(voir diagramme PlantUML ci-joint)*

## 6.4. Scénario : Consulter le stock

### Description
L’employé demande la quantité disponible d’un produit via la console. Le contrôleur interroge le `ProduitDAO`, qui récupère l’information depuis SQLite et la transmet à l’interface.

### Diagramme de séquence
*(voir diagramme PlantUML ci-joint)*
