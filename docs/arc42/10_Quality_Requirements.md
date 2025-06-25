# 10. Exigences de qualité

Cette section complète les objectifs de qualité mentionnés en section 1.2, en les organisant sous forme d’arbre de qualité et en les illustrant avec des scénarios concrets.

## 10.1. Arbre de qualité
```
Qualité
├── Sécurité
│ ├── Authentification forte (BCrypt, JWT signés)
│ └── Autorisation par rôles (ADMIN / EMPLOYEE / VIEWER)
├── Robustesse
│ ├── Transactions atomiques (vente, retour)
│ └── Tolérance aux échecs (rollback automatique)
├── Performance
│ ├── Vue Web : < 300 ms serveur
│ └── API REST : < 500 ms par appel
├── Scalabilité
│ └── API stateless (JWT) ⇒ conteneurs réplicables
├── Observabilité
│ ├── Endpoints Actuator (/health, /metrics)
│ └── Logs structurés JSON
├── Maintenabilité
│ ├── Modularisation (controller / service / repository)
│ └── Tests unitaires & d’intégration
└── Portabilité
└── Orchestration Docker Compose (app + Postgres)
```

## 10.2. Scénarios de qualité

### Scénarios d’utilisation (runtime)

| Qualité | Scénario concret |
|---------|------------------|
| **Sécurité** | Un appel `/api/v1/produits` sans en-tête `Authorization` renvoie 401 ; un token expiré renvoie 403. |
| **Performance** | Lorsque l’employé valide une vente de 30 lignes, la réponse HTTP POST `/api/v1/ventes` est renvoyée en < 500 ms (p95) sur un poste standard. |
| **Robustesse** | Si le décrément de stock échoue pendant l’enregistrement d’une vente, la transaction est rollbackée ; aucune ligne partielle n’est sauvegardée. |
| **Observabilité** | Un redémarrage forcé du conteneur Postgres fait passer `/actuator/health` de UP à DOWN en < 10 s, déclenchant une alerte Prometheus. |


### Scénarios de changement (évolution)

| Qualité | Scénario concret |
|---------|------------------|
| **Scalabilité** | Ajouter un second conteneur Spring Boot derrière un load-balancer ne nécessite aucune modification de code grâce au JWT stateless. |
| **Maintenabilité** | L’ajout d’un champ `remise` à l’entité `Vente` ne touche que : modèle JPA, DTO, mapping ; aucun changement dans `VenteController`. |
| **Portabilité** | Le projet est cloné sur un nouvel ordinateur ; un `docker compose up -d` suffit pour lancer l’app sans variable externe. |
| **Sécurité** | Passer la durée de vie du JWT de 8 h à 30 min ne nécessite qu’un changement de propriété `app.jwtExpirationMs`, sans recompilation. |