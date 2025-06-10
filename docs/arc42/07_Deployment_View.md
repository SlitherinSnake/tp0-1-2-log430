# 7. Vue de déploiement

Cette section décrit l’infrastructure technique nécessaire à l’exécution du système, ainsi que la répartition des composants logiciels sur cette infrastructure.

Le système étant exécuté dans un environnement local (machine virtuelle), l’architecture suit un modèle simple à deux niveaux (2-tier), sans distribution réseau.

## 7.1. Infrastructure – Niveau 1
Le système repose sur une architecture 2-tier, où la couche cliente (console Java) et la base de données PostgreSQL résident dans la même machine (ou VM).
La communication entre les deux couches se fait via Hibernate (ORM).

### Schéma de déploiement (niveau 1)
![dd](https://img.plantuml.biz/plantuml/svg/ZP5DIiD068RtWTpX9HkJHIYu52fDsuKKOwE5hjnC9nzrC9alp4oA889ty1Bs7Bs99t6cMlyG4OQi7jxdcSd8Ec5StpPnv9Hh25CbraQqBQ7sPxVj6bRKadPuUNn2OUIDSYHTRmZ7kLDauYYEZZ0S3d71rDUGZGfqkYpHi1H-adjKaomWSQJPpoQby3EQtbdw0cS9xkC8aDnshCDGKdHTewHmYZMg-U9QfpEn4PYmjkpNSvJBIPR4qSFF4ajcGxt1U8gqOklMXXQNvx-67D1qolAMpA6Y5awiXFNWE7p3Yx3MkorPV0v-L_iiJNUXTNvLOgC4pjIAZ1QE1hEgU9F_yqx8yzE_rJX18UNksUvDmPX-w3205sOXRKIwtl_ZLm00)

## 7.2. Infrastructure – Niveau 2
![dd2](https://img.plantuml.biz/plantuml/svg/fPD1IiD058RtWTpXaOq9KheNAaqQiOPMemdgefjfyXe79pDnCgc82Dx1KxJNw4qyYPDODQr52i8iilZd_x__lKo_L4IgAcUceQXY23ubcIHvZf3gireKJ53CaIjuVtq3joWOJOYYWiCDJi6E2abv1fuG2WuX5ANA94QN8ySqJEC0UhqUd0kiKaWOhKriJkOq8meh2OyuTMg5QN2Xmwp1RudES2nuDWq0ZKbd955WdN65KdyYL8eoMZRI2H_6uv5c2CYSH8caXPfmExvxof4XcNNTmBrrBDDusGOhKSxhAF9LRRRHQeCmsg7rHNAF4ZhGlhHcJaHU25wNihOcfzdQenjV3tR2JKdvkGZ1-Mg1vIuOpsz1j3QmPaY98esDvMsR_OWtiw-GDQ3hkFyC73gTuxtAmv6EVQXMIwN1Xd4SWdrw4dV3YoXk_gMzF9gmlshgbC3t1jyRzlLm80ZT9gAFF6rUmWS0)

### Schéma de déploiement (texte ou diagramme)
