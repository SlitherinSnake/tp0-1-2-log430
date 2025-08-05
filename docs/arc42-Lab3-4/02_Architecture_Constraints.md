# 2. Contraintes architecturales

| # | Contrainte | État | Commentaire / justification |
|---|------------|------|-----------------------------|
| 1 | Le système doit être développé en **Java 21** | 🔄 | Mise à jour vers la LTS actuelle pour Spring Boot |
| 2 | **Spring Boot ** est imposé comme framework applicatif | 🆕 | Facilite MVC, REST, sécurité, Actuator |
| 3 | La couche de persistance doit utiliser **JPA (Hibernate)** | ✅ | Contrainte d’origine conservée |
| 4 | La base de données doit être **PostgreSQL** (conteneur Docker) | 🔄 | Remplace SQLite ; supporte transactions concurrentes et multi-site |
| 5 | L’application doit offrir :<br>  • des vues **Thymeleaf** accessibles via session Spring Security<br>  • une **API REST stateless** JSON sous `/api/**` | 🆕 | Cohabitation serveur-side rendering + API publique |
| 6 | L’API doit être documentée en **OpenAPI v3 / Swagger-UI** | 🆕 | Contrainte de transparence et testabilité |
| 7 | **JWT Bearer** est requis pour sécuriser tous les appels API | 🆕 | Décision ADR 005 |
| 8 | L’authentification formulaire classique reste autorisée pour les vues Thymeleaf | 🆕 | Session HTTP côté Web |
| 9 | Le système doit être conteneurisé avec **Docker & Docker Compose** et exécutable dans une **machine virtuelle** ou un environnement CI | 🔄 | Étend la contrainte initiale de « VM locale » |
| 10 | **CI/CD GitHub Actions** : build Maven, tests JUnit 5, analyse SCA, build & push image | 🆕 | Garantit automatisation et sécurité |
| 11 | **CORS** doit être configuré de façon centralisée (whitelist d’origines) | 🆕 | Sécurité front-end / API |
| 12 | Les points de santé et métriques doivent être exposés via **Spring Boot Actuator + Micrometer** | 🆕 | Prépare le Lab 4 (observabilité) |
| 13 | Les décisions techniques majeures doivent être tracées sous forme d’**ADR** (format Michael Nygard) | ✅ | Contrainte déjà appliquée (ADR 001, 005, 006, etc.) |
