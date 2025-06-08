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
#### Diagramme de classe représentant les entités et composants principaux du système :
- **Employe** : id: int, nom: String, identifiant: String  
  → Représente un employé responsable des ventes ou retours ou regarder le stock.
- **Produit** : id: int, nom: String, catégorie: String, prix: float, quantite: int  
  → Article en stock pouvant être vendu ou retourné.
- **Vente** : id: int, dateVente: Date, montantTotal: float, employe: Employe, venteProduits: List<VenteProduit>  
  → Une vente est associée à un employé et composée de plusieurs lignes de vente (`VenteProduit`).
- **Retour** : id: int, dateRetour: Date, vente: Vente, employe: Employe, retourProduits: List<RetourProduit>  
  → Un retour est lié à une vente précédente et contient plusieurs lignes de retour (`RetourProduit`).
- **VenteProduit** : id: int, vente: Vente, produit: Produit, quantite: int  
  → Représente une ligne d'achat d'un produit dans une vente.
- **RetourProduit** : id: int, retour: Retour, produit: Produit, quantite: int  
  → Représente une ligne de retour d'un produit dans un retour.
- **MagasinController** : contient la logique d'orchestration (vente, retour, recherche, stock), en lien avec les DAO.
- **DAO (Data Access Object)** : `ProduitDAO`, `VenteDAO`, `RetourDAO`, `EmployeDAO`  
  → Gèrent les opérations de persistance sur la base de données.
![dcl](https://img.plantuml.biz/plantuml/svg/hLVDRjiu4BuRy3iGliJUneTYRqPZj4XoQD5itRYREokFLUvIea9IGOjYtsMFSSzz0xrOXp-YI5kAZQ08ajZ3cMzcllaHzLffAdLTyF58Cys1N36QIreKG3P0CawL0Z8dwszADuzE-2V9A4EnGlaDpQbY9LbzM9FfNGs4YvpTrp0RZyQZCt9nSK6kImHkTefKafKPgoX7IpmOZomwkIugBhu1-JuU4KHa6x8WhDJkMoaA_BhMQ9gtvu20MqPBdPxCpTyN90VzTZETTI1Mz9SehApJzve1R3hhJlypqRleLb9iQgPFZIwZ6d8X6Up9CVUlADfoGRtjVKqDCH3XFIU3ozPXt-4AlLfvy6l57xthpaUKy1qoCb2C3VfonmloNcIKNw707HMYr0ZwIMZAqpp1btVH5jg97moE9rSPFARqgxj8k3nEoLKRZpr98hBdDr5GFJJuUEn959izYoDH3hudm8YsMz2YbiEy-VC3uXuydnRvISQaHTYdx3QMdiYPinaWhICqX7IKhJOe1rn2nDX-VFkTzkHyzP1JUVr5EvdFcwQXB3seFOaS71-R9C_-R6JR0axtj-OJbRH3Vsv6RczVlVaGhsblQwJV7O24x0j8yxkY4cDgiptmb2YQFHRlpT0fSG_lsNHBxd2_3jWNeBM4D--GYhKMsR_J0wBFSNtN3y0vV4p8De0FWcqS3jrkcOd2kEXP27kDdRJsYLH53GYbX1nX9KZNeCa0f87Xt6Or1-cscyzENOUQcWrnGwjN8Tkt2963N1gOZQW_S-Xfu1DZZdii2jftuLwZft5ZstqIjNeFVbiSx8fqIVXLSs9SoM8whUsyoJBf_KjySiZc9mTC8ve1Vzknfm8RH_R4vCTDPRGimP2mTfzI5WugVKGV2nVeFQAQYLBLN8Fl-oUrvmFzNUqzPTesxsmZicw1KytGgwFgp5UXsP5QCyxm_eZNognZT3nghtpp-RA7qqLhvrpyRRzO3tZxL7OlLLGUW1u26VXeRM7RDplUFEPuJPLmNpS_p9HKxQRgjm-J75lde6muTZpLa6atKNssRaW6ZQE-xxC6Jx5o70N6ORk5JADYnMl5a1VbWoIWEvSj_vEN4-jiyiU4j1UxWB3irmiN6LfhNCJAAfeO702wyHgSDJiWpV-axi8TZvnM0CJ_r-6w8viTnL8RCrDWnFLG_UfQKlDY4PHD4Du0aRRDJaAP0Re7Vv60hzZBr6xR7k2MW4e2FXQ6iZ8jnr0M4SnPZbSUysOKUkaCgPhF9yQkn6fDyFCvRcoPtda2QYjG26vxF1D59HIpKsNGF0xk894nDsmEBpl3Xa02b65PVFu75Lc9xL91kS5Cpqc1hsgMWWZI82g1_eBDr6WWZPZQ3EyUZ7W8ObwFcaS91TZ824QBsdkapEAoOjvTFwt2GpVmbbQrrKvvMFXMBKCPBh_GU_DsQQWEToQJkgEawxHGWiI_ST7MEOaf0-ya3U2ullbvZGpPD-TV)

### ii. Vue des processus
#### Diagrammes de séquence (DSS) illustrant les cas d'utilisation suivants :
![dss-rechercher-produit](https://img.plantuml.biz/plantuml/svg/pLRBRXDB4DrRyZ-KlCa49GikEtJ3A_OL5kOIOYGsR3ez5QTHJ_VG3mjs_0ChTYnZrlp1_0bVGbNFqtPu23PO6jipKttgr3cdntxWWt0X5dey2YfeXEbo2L_VVu1pb5Ve-81ee7GsZof0jbO2JgtnLYDz16UrHdSu7er71t1oSW8FPS3eF600QOlIUc621rMKMhs9rSPp4TSk3c9GMdd1vN0L2w4CPFe0gTA-gpO4AMIm3cRf0lAQkGdeBeL4WauCu4rKe8cM5k25yehAIXf7ILLINvWqJ25RIc4C4Ps0y2r_XampL5yqaoYTChnG9rZBi_lW43Hw82wALkt0FnKhuPNfXMZXcrFkC7tuBE49AQlh7A7wn6mVJO6LVAmkq1dIO0Vuh9QRrYP4-cIeHy8Z44HkZQasnCgX5ipB87M3Mb2wKRlKOPEKkGZYWbTFpgmFAuVBvuj9Rzgqsbp407a4BwkD4KxPsgT6V3Y-rue6TskbIVC3Mt9pcBYReZA1LuOH2pGHSc4yZorxvHTVttySHa2nkM95m6ADSb0CfQNRyifIrzGWl2kJ35rRoERJ8p2sE08RGOfGRLRMAKonR7dN8pJ6q1GzwKRip3SPIUpwzg8iaxp4gAWsuSgo0v5lTyb9Z8bPap90Ka4uKLJNb9zUvbEoylQmDLU1oj44TQdO-2Iwq66dP3uTZu1UWm5e74xMrXp2pVDCvOTrCGs1Moo38nSAJBl58Ut6pu6cYmadywZ9IXgJXNuBjnkG-fqhg66V7Ugxd2rcfo_W5jlyt_cpfwtldY-yV3mxdz5aS0BtMpPT7c556QkkQMT5WpqJXcsitd_krFGy7GEvXXG7t8uxRHfdEpq4f4tWqAojulPWBrwJ86qNQPr3NrgJxw0d0NOJj8K-j6PhjuZBIyNJP4tvq9ssBwsCwse3zBnfwkbaRyyCO_JFNeme_f0LDXnBJK_FEaceFUwN_MAM_hT5HjM7VnG1PhMOFvcrCRZHfeNouTz6ddi_4Fo6kf_ApckmMUEpJlmp7UdNWd-eEiubnZL5Qn02XqTiW6Q8BMzspBYpPqR_3Fgk4Nt9B_fnIbidrMUo_VPRtjJ6qEKEvYzp24q-7ZsWz_Ilv1i0)
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
### ADR 002 – Séparation des responsabilités (SQL vs NoSQL, local vs serveur)
`~\tp0-1-2-log430\docs\adr\002-separation-responsabilites.md`    
### ADR 003 – Stratégie de persistance (SQL vs NoSQL, local vs serveur)
`~\tp0-1-2-log430\docs\adr\003-strategie-persistance.md`
### ADR 004 – Choix du mécanisme de base de données (SQL vs NoSQL, local vs serveur)
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


