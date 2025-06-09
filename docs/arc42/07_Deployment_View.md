# 7. Vue de déploiement - à finir

Cette section décrit l’infrastructure technique nécessaire à l’exécution du système, ainsi que la répartition des composants logiciels sur cette infrastructure.

Le système étant exécuté dans un environnement local (machine virtuelle), l’architecture suit un modèle simple à deux niveaux (2-tier), sans distribution réseau.

## 7.1. Infrastructure – Niveau 1
Le système repose sur une architecture 2-tier, où la couche cliente (console Java) et la base de données PostgreSQL résident dans la même machine (ou VM).
La communication entre les deux couches se fait via Hibernate (ORM).

### Schéma de déploiement (niveau 1)
![dd](https://img.plantuml.biz/plantuml/svg/ZP5DIiD068RtWTpX9HkJHIYu52fDsuKKOwE5hjnC9nzrC9alp4oA889ty1Bs7Bs99t6cMlyG4OQi7jxdcSd8Ec5StpPnv9Hh25CbraQqBQ7sPxVj6bRKadPuUNn2OUIDSYHTRmZ7kLDauYYEZZ0S3d71rDUGZGfqkYpHi1H-adjKaomWSQJPpoQby3EQtbdw0cS9xkC8aDnshCDGKdHTewHmYZMg-U9QfpEn4PYmjkpNSvJBIPR4qSFF4ajcGxt1U8gqOklMXXQNvx-67D1qolAMpA6Y5awiXFNWE7p3Yx3MkorPV0v-L_iiJNUXTNvLOgC4pjIAZ1QE1hEgU9F_yqx8yzE_rJX18UNksUvDmPX-w3205sOXRKIwtl_ZLm00)

## 7.2. Infrastructure – Niveau 2
Pas de répartition multi-machine réelle : le système reste local/monolithique pour l’instant.
Cependant, cette architecture pourrait évoluer vers un modèle 3-tier (ex : console → serveur API → BD) si des services web sont ajoutés. < Pour le lab 3

### Schéma de déploiement (texte ou diagramme)
