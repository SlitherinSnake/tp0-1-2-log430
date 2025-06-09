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

