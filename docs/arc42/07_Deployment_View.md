# 7. Vue de déploiement

Cette section décrit l’infrastructure technique nécessaire à l’exécution du système, ainsi que la répartition des composants logiciels sur cette infrastructure.

Le système étant exécuté dans un environnement local (machine virtuelle), l’architecture suit un modèle simple à deux niveaux (2-tier), sans distribution réseau.

## 7.1. Infrastructure – Niveau 1
Le système repose sur une architecture 2-tier, où la couche cliente (console Java) et la base de données PostgreSQL résident dans la même machine (ou VM).
La communication entre les deux couches se fait via Hibernate (ORM).

### Schéma de déploiement (niveau 1)
![dd-niv1](https://img.plantuml.biz/plantuml/svg/ZP5DIiD068RtWTpX9HkJHIYu52fDsuKKOwE5hjnC9nzrC9alp4oA889ty1Bs7Bs99t6cMlyG4OQi7jxdcSd8Ec5StpPnv9Hh25CbraQqBQ7sPxVj6bRKadPuUNn2OUIDSYHTRmZ7kLDauYYEZZ0S3d71rDUGZGfqkYpHi1H-adjKaomWSQJPpoQby3EQtbdw0cS9xkC8aDnshCDGKdHTewHmYZMg-U9QfpEn4PYmjkpNSvJBIPR4qSFF4ajcGxt1U8gqOklMXXQNvx-67D1qolAMpA6Y5awiXFNWE7p3Yx3MkorPV0v-L_iiJNUXTNvLOgC4pjIAZ1QE1hEgU9F_yqx8yzE_rJX18UNksUvDmPX-w3205sOXRKIwtl_ZLm00)

## 7.2. Infrastructure – Niveau 2
![dd-niv2](https://img.plantuml.biz/plantuml/svg/fPD1IiD058RtWTpXaOq9KheNAaqQiOPMemdgefjfyXe79pDnCgc82Dx1KxJNw4qyYPDODQr52i8iilZd_x__lKo_L4IgAcUceQXY23ubcIHvZf3gireKJ53CaIjuVtq3joWOJOYYWiCDJi6E2abv1fuG2WuX5ANA94QN8ySqJEC0UhqUd0kiKaWOhKriJkOq8meh2OyuTMg5QN2Xmwp1RudES2nuDWq0ZKbd955WdN65KdyYL8eoMZRI2H_6uv5c2CYSH8caXPfmExvxof4XcNNTmBrrBDDusGOhKSxhAF9LRRRHQeCmsg7rHNAF4ZhGlhHcJaHU25wNihOcfzdQenjV3tR2JKdvkGZ1-Mg1vIuOpsz1j3QmPaY98esDvMsR_OWtiw-GDQ3hkFyC73gTuxtAmv6EVQXMIwN1Xd4SWdrw4dV3YoXk_gMzF9gmlshgbC3t1jyRzlLm80ZT9gAFF6rUmWS0)

## 7.2. Infrastructure – Niveau 2.1
![dd-niv2.1](https://img.plantuml.biz/plantuml/svg/fPJTRjf048NlBVaTHhuff494_P4Y5A8OI9EW_BZOIdh1pTeCP1LxrzrT9qshIdc7la7lw6lm9dcIpjen44gkQY7XzSwU-Npco8VQC6MAB7KTmqsACE1ifbYM8KpejvZdgUIOeJ3mzFWB_3nFUS8CbmAkCOP6c2ikPj2Jqi1R2AGsCuNXvpDQ3A-ZfkkupXleThjm8R6OG99oAzKGx8xFcC52mJt6JLlXEa9IIw__RmbrQdh0DFIKlDUeu8Vh010tdxB4W7TL84Gt3nccoAPZSHATs_x-qF_YlIpiItdB4ORXKHYUNbwS3cZ_vokCVd0A-CsW4WYDJCQSp6a35sOn9uEi0zkmrqwLn7xEdtSsc4VujLZyCQY9vl9YBFp2t4Z5lvVA7UWXK_IY-sJYmJP8Sk7EMiFgWKoXaKXXKDYd0vdSedh9jznS6zbOD4YWixUxjzjyniqhq4HckHIMsriVyVbLlwoZ_usgqL4OWMsYP9gYqYJTPZblx-nKoYFKPhjmY4cXk7aOYwV7t_19gWnIEUEYMfC1CEKfELhArCLRB8cPha8hXLZCoTZN39ammyfYRvLNmft4dOyVth-hS8DZAsyRj5ejWa_BkMYx-e_BTUe4az21TH7irO6JA0f2E3wAsi5b615CKYLLezOsQHi7hPdLmIcQvAQC43GqXp3mc_knQXziZnEPwJghIAv9rGXL6DVBwrvbzSh-cdtZSNstWQcMQMS4HhtvbDiPMPfyfHCSmxCXmq6ll_ow36XWCAHNinCvH34flrT_0G00)