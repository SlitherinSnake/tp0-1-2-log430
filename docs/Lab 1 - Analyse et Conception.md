#  Lab 1 - Partie 1 – Analyse et Conception

## 1. Analyse des besoins
L'application doit permettre à un employé de magasin d’effectuer les opérations suivantes :
### 1.1 Besoins fonctionnels
* Rechercher un produit (par identifiant, nom ou catégorie)
* Enregistrer une vente (sélection de produits et calcul du total)
* Gérer les retours (annuler une vente précédemment enregistrée)
* Consulter l’état du stock des produits

### 1.2 Besoins non-fonctionnels
* Système simple et local (autonome, sans serveur web)
* Doit gérer des transactions simultanées (3 caisses)
* Haute fiabilité : intégrité des données garantie via des transactions
* Portabilité : l’application doit fonctionner sur une VM fournie
* Performance : temps de réponse rapide lors des opérations

## 2. Proposition d’architecture
### 2.1. Vue logique
#### Diagramme de classe (DCL) représentant les entités principales :
* Employe : id: int, nom: String
* Produit : id: int, nom: String, catégorie: String, prix: float, quantite: int
* Vente : id: int, dateVente: Date, montantTotal: float, listeProduitVendu: List<Produit\>
* Retour : id: int, preuveVente: vente, dateRetour: Date, listeProduitRetourner: List<Produit\>
![dcl](https://img.plantuml.biz/plantuml/svg/jLN9Rjim4BqBq3yCVAdJHeTSZSP84UTGe4kQ5BaJrbGbIf7ga0eQ1FsVzju_y8zrkCYYjzO1seCNpHk-cVSuf2wi4-JwLfM5aquXBAHeIBGjGesmLC9QXCxq1Bq6-s3TwaU2XCoX3davjA8HLcgFV-sPLoALHLayW_FpSxZIdBBwQVr3MGHgk6exPHuG7ii2e09PmmoaTl59cvOVRnr9tOH8vRzVG8FkdMcd9njW2B-kGpGmVFULOvLhCdNFxHrNXQKvR0n9t8ft9Bznw9Co8grbaLuKPdn_xV53MZDsce1hfW_1L20JS8VQ7J2fvcOZF8C5_u_HrcZ7PJuQ9zHcoqfQXyaGNbVt3B_XsCiKcs-fM0pyeRMXGe3UPaNonjTw0g4DLMn8s2gKARn1voTfhyIEiBzVYmo_e_QqC1UVIuomHZf2PipTrJa86RWEQuc3ldZrFf_nbEEZZv6JSFaPoN-cI-9He77IzZ3x5KkZRQyuyzQPvPUfh8UH3ZRjc_bWKrOJDM73n8GKjxDBghOBdWAcGpaRZkV9QFlEbkqlaQoyp_Vi59vOCf9iM7l98qUhNmfvoxqdXc-ipEhB60fPHYcaNSTJpMED_qUsFM4FVQeZuRlxNoxUe19ESZlHk-4Ihghv7WTdq3l9HufLx89-oJZVXpA6MJo4hoV9jnZF-UJv942JiyaWfYpIMVyZTZPYmuqHxuyrCoEcgWBBk0EyEVosqWxc6rIxYPGk0PzO5XV-RlClpzy0)


### ii. Vue des processus
#### Diagrammes de séquence (DSS) illustrant les cas d'utilisation suivants :
* Rechercher un produit : Employé → Console → ConsoleController → ProduitDAO → SQLite
![dss-rechercher-produit](https://img.plantuml.biz/plantuml/svg/VLB1Qi904Bq7yWz3JdeelKiHfHPAiMWRw7sDOpkmxeRPIKjl_OT-XT_Xd_HBEZ6xeX8yBBF9l3VlpKicGGnBixLv9YGMOLQMyFVp2wzOVI2t1ne7DVjrGv9dUMOgyGmFszhwRyopMkbKKMsSG77lNV0wPF16-3Kim0I8h_g1MeIzjSW96nyluCjEVZPT771QysnjnAnCG2ZAbpa95gsvQ0jknaaOHX0C1U95BUJTCxfcu0zNxPJ2vDw7UPR07I-QK00VKuWM-hc9e7JrTiRh8RGGHNQsjV591fwMqzj7MY4x8nfMx9tC4z-mGx0OqiS8mFjTrTJx7QjqGAigdLYKk5pco1l1d8BSnPN6vokSHNULqYMc-9Jw2CncGoTfXK4Qe6jFqAqFFze_)
* Enregistrer une vente : Employé → Console → ConsoleController → VenteDAO → SQLite (avec transaction)
![dss-enregistrerVente](https://img.plantuml.biz/plantuml/svg/fL9DIyD04Bq7yX-6N4nHyLwajDY2IC5AeVTjEz6HxMwOdLIy-H_yX_uI9zcD7sqzUThDlZTlthp9E8XXQNOspuJ48aoNI_XuUuUPOtoa88mCZKFOenFCipmp6_4Cirrj_Qi-r5fE6wgD4oXkl0jUHSeLuSkW01CWFPqcwY7ihKNkkUdpWBvgcqydznrBpppR6Z5h4n2AvSES18lMMZ85bwE-BGmX60h42_RRXYIKTReeCjVnhXCm6kHPNnFBanFbuNSKdP4_DIu0auDX7r2KxRLKMTt_Nx8LKQqukY9xCd2tc5pTqXhPcNPH2JlH4Qo9suHpO1JViAxNdldrCmgiufgx1eSvnA9Xp_avrw_4ZtanLYNDleOnLKMR9sH5A-AT4VAkx2frlZ6wRVTFVW80)
* Gérer les retours : Employé → Console → ConsoleController → RetourDAO → SQLite (mise à jour du stock suite au retour d'un produit)
![dss-gererRetour](https://img.plantuml.biz/plantuml/svg/hLBDIWD13Bulx3k4FRLGyLwajDYYIC7QGk_JRQhHtPabawruynry0Qy-Hz_49p7RcHKjUn4y3279b-_Bpuoz69QwBD94I0g4wMe5dwzlS7NuO6IeBJ2AgpbDqiJauXXcPLMu5qoJIYiffyfOWUpIMU-qlhScVEvdO3p4K3TGZR0h2kGM6zqJ-FAeFftS7c5gqsHhn6oCHyXMTtCIp9hUObTmDcfrOGZUa2SE5BqzMcc2wyOEe6AthKcyHkRavM8H_54_JLg2m1NxKfomKRa_yWq0Osl3TdL1ekLL5HrBisWPxxMK_qUY8LNki2FDxCd0pM9oVaqRP6j43nDp7Hs0ZXyE3oXGwZkymsFZQiABov-YmEcEOj4Gbl7R_9pqrt6wa67j2fjz3sHsEyeVa3Mx8owDaBEzKAj3Zj5kZx_t2m00)
* Consulter le stock : Employé → Console → ConsoleController → ProduitDAO → SQLite
![dss-consulterStock](https://img.plantuml.biz/plantuml/svg/ZPB1IiGm48RlWRp3qDFkGRplGNPn1H71bOBtj9d5OTEa9bF5c-_WK_WSlebFuhHDX5r4F8NCVF_adxzT9pQHXyvfnSGEOLlNyFNs3fV1fy4nHf1Yuj0UjDBAj1mYN7Mz2w-eLIzQQgatdg4Q5K7WnGFXox82ao2NVWVbu1YSmrZOV3t1Tp7OWNYxkPiuERMoXarZ4a9LtzyIOpMQoxRWxA8y32kms1blCTRZ2WkoUPfqoFDIgqBEekn0kctP_mzXeKKhhluZ6Z6XPzDIVIO3DxlD-JKr6BrXYIhtVcP6v314Z-0CF0sI2F7d1rXAVV3KZE5EGq_zJvziVlVOwr4wfzGSXI0bonMO_PxQnxBI9915rkZjzDTy0m00)

### iii. Vue de déploiement
#### Diagrammes de déploiement :
* Architecture 2-tier :
    * Client : Application Java locale (console)
    * Serveur : Base de données SQLite (locale)
    
    ![dd](https://img.plantuml.biz/plantuml/svg/ZL3DIiD04Bu7yWuVEIL853nwa4OzI36qMkZ9osGpwCB-XCss7aJm7Nm9-nnv4v_4sRRYjR2x0_lzpSniZ1JYnfeyYpeQnfscbq3MCdevqsumNhDb5_7p-OKcg5STMSLDO5pMKNF8ipnpNiX5Im8wnbQB8ninAzSjZ5Tak2hmdcT0kWTsCmn6AuhQEomNAvHpsX9klHlzFnrGtWxUfVpYY46KpwvinSNW36lDRr84ZC5BQAYAfFn8VG4zHUfebBoRzAQPl7FJZaUi7Xza574wxxj3bv9AV_zvjdSuFHHrVNtKuWgDKjFtAAiWB9vQbFUwu-18i0lCbqsLl6Vi-ltcVm40)

### iv. Vue d’implémentation
Organisation en couches selon le modèle MVC, avec séparation claire des responsabilités :

* Présentation (view) : affichage console, saisie de l'utilisateur
* Contrôleur (controller) : coordination des interactions entre vue et logique métier
* Logique métier (service) : traitement des règles d’affaires (calculs, validation)
* Accès aux données (dao) : manipulation des entités via Hibernate/JPA

![di](https://img.plantuml.biz/plantuml/svg/XPB1IiGm48RlXRp3i2T5jYXwyY0hrQEiUEWzD4C9JZ8bcIeYmhw39n_1v_1DzabCks2xRkcUmloJVF_yGrPHT93MaSh42Y6KBOBPRhwiQ-Zdsw4NBPOzR3UVi0wrzZRk18CH79kMqddoK1Pm1dUtwrELgQpHrREk4HOLkvoulheypwMvo5yilYxWNMK05UaOeM0VcR1Ckie-vfvMx2Km4OOfGF7NFO990oFj4Hv3oc1b4CeK6OVo2ONRCDJtQI_yXaTyZEOfbxIWVqPxoiwhbthwQ3smNDuSfeLhi0aITDqbE6ntZyRMqpU6GQRxex5cBMRvy_0kxGgzwJ_YBm00)

### v. Vue des cas d’utilisation

Acteur principal : Employé 

Cas d’usage principaux :
* Rechercher un produit
* Enregistrer une vente
* Gérer un retour
* Consulter l’état du stock

![cu-menu](https://img.plantuml.biz/plantuml/svg/NP0nJWD134NxbVOENzi0GYb8KgE89A9A0-80rgorZ9YTYSQUI152oXsek04vnzua9s4sMOfDuURx-VlR2r6AcbfN5chLCLQMcaXjowWPXWJrJLBhh93Qu74wV6D33OdrPL4MP3H4hDkj2tlkXSX6oJVPg7hTYtQ__qPMX75hWfUGc_TuMi45GuxlAdoM1P24yxeyzyBcdMDVI1xR6EfajKAEyhPy695h7xcnel6CCdRibGToEYAVk-C5GcGDAGxGR0GjSxZaD7FkTFZfZagAEa4qc8zXO5uMN_sPmyMOJ1ulgSR2z5gONGlptcN1lZw__Wy0)


## 3. Justification des décisions d’architecture (ADR)
### ADR 001 – Choix de la plateforme
```~\tp0-1-2-log430\docs\adr\001-choix-plateforme.md```
### ADR 002 – Choix du mécanisme de base de données (SQL vs NoSQL, local vs serveur)
```~\tp0-1-2-log430\docs\adr\002-choix-bd.md```

## 4. Choix technologiques
| Outil / Bibliothèque | Justification |
|----------------------|---------------|
| Java | Langage de programmation utilisé, car familier et bonne connaissances |
| SQLite | Base locale, légère, sans besoin de serveur et connaissance présente |
| Hibernate (JPA) | ORM puissant pour l’abstraction de la persistance et fonctionne bien avec JAVA |
| JUnit | Framework de test unitaire pour Java |
| Maven | Outil de build et de gestion de dépendances |
| GitHub Actions | Intégration continue pour build/test/lint automatique |
| Arc42 | Bonne pratique et modèle pour la communication et la documentation de l'architecture             |
| Docker | Pour conteneuriser l’application de manière portable |
| Docker Compose | Pour orchestrer facilement les services locaux |

## 5. Références
* Format ADR basé sur Michael Nygard : [https://github.com/joelparkerhenderson/architecture-decision-record/tree/main/locales/en/templates/decision-record-template-by-michael-nygard](https://github.com/joelparkerhenderson/architecture-decision-record/tree/main/locales/en/templates/decision-record-template-by-michael-nygard)


