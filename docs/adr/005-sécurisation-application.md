# ADR 005 – Sécurisation de l’application : JWT + Sessions vs Sessions only

## Status
Accepted

## Context
Depuis le Laboratoire 2, l’application n’est plus un simple CLI :  
- Elle expose **des vues Thymeleaf** (interne) et **une API REST** (publique) que consommeront, à terme, un front-end SPA et des intégrations mobiles.  
- Le mode « session HTTP classique » fonctionne bien pour les vues serveur‐side, mais il **couple l’état** de l’utilisateur au serveur.  
- Les appels API doivent être **stateless** pour autoriser la mise à l’échelle horizontale (plusieurs pods/container) et simplifier l’accès multi-client.

## Decision
Adopter une **stratégie hybride** :  
1. **Form Login + Session** pour les pages Thymeleaf (`/login`, `/logout`).  
2. **JWT Bearer** pour tous les endpoints `/api/**`, via un filtre `AuthTokenFilter` qui :  
   - lit l’en-tête `Authorization: Bearer <token>`  
   - valide la signature / l’expiration  
   - place l’utilisateur dans le `SecurityContext`.

## Justification
- **Scalabilité** : le JWT est auto-porté ; aucun sticky-session ni réplication de session n’est requis côté API.  
- **Interopérabilité** : un client mobile ou une application React peut appeler l’API sans gestion de cookies.  
- **Simplicité côté vues serveur** : la session Spring Security reste la solution la plus rapide pour Thymeleaf.  
- **Séparation claire des canaux** : routes Web = étatful, routes API = stateless.

## Consequences
*Avantages*   
- Tolérance native au load-balancing pour l’API.  
- Compatible avec les standards Swagger / OpenAPI (*bearerAuth*).  
- Facile à révoquer : il suffit de raccourcir `jwtExpirationMs` ou de changer la clé.

*Inconvénients*   
- Deux mécanismes à maintenir (session **et** JWT).  
- Le front-end doit gérer le stockage sûr du token (XSS, CSRF : `SameSite=None` si cookies).

## Options envisagées
| Option | Pour | Contre |
|--------|------|--------|
| **Sessions only** | Simplicité, aucune modif front | Non stateless, difficile à scaler |
| **JWT only** | Vrai stateless partout | Complexifie les vues Thymeleaf (pas de formulaire Spring natif) |
| **OAuth2 / OIDC** | Standard complet | Sur-dimensionné pour un projet pédagogique |