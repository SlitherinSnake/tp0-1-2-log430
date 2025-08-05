# 11. Risques techniques et Dette technique

## 11.1. Risques techniques identifiés

| Risque | Probabilité | Impact | Mitigation |
|--------|-------------|---------|------------|
| **Gestion de la concurrence** | Moyenne | Élevé | Transactions atomiques avec `@Transactional`, politique READ_COMMITTED de PostgreSQL |
| **Corruption du stock** | Faible | Élevé | Rollback automatique sur exception, contraintes de base de données |
| **Authentification compromise** | Faible | Élevé | JWT signés, hashage BCrypt, rotation des secrets, timeout des sessions |
| **Déni de service (DoS)** | Moyenne | Moyen | Rate limiting à prévoir, monitoring via Actuator |
| **Failles de sécurité OWASP** | Faible | Élevé | Analyse SCA automatique, validation des DTO, CORS strict |

## 11.2. Dette technique actuelle

### Architecture

- **Monolithe modulaire** : bien structuré mais pourrait nécessiter une migration vers des microservices pour la scalabilité future
- **Pas de cache distribué** : les requêtes répétées sur le stock pourraient bénéficier d'un cache Redis

### Tests

- **Couverture de tests** : tests unitaires présents mais manque de tests d'intégration complets
- **Tests de charge** : aucun test de performance automatisé

### Observabilité

- **Logs structurés** : logs basiques, pas de format JSON structuré pour l'analyse
- **Métriques métier** : métriques techniques présentes mais manque d'indicateurs business

### Sécurité

- **Audit des accès** : pas de traçabilité complète des actions utilisateur
- **Chiffrement des données sensibles** : mots de passe hachés mais autres données en clair

## 11.3. Plan de réduction de la dette

### Court terme (prochaine itération)

1. Ajout de tests d'intégration Spring Boot Test
2. Mise en place de logs structurés JSON
3. Configuration d'un cache local (Caffeine) pour les produits

### Moyen terme

1. Audit trail complet des actions utilisateur
2. Tests de charge avec Artillery ou JMeter
3. Métriques métier personnalisées (ventes/h, stock critique)

### Long terme

1. Évaluation de l'architecture microservices
2. Chiffrement des données sensibles au repos
3. Cache distribué Redis pour la scalabilité horizontale