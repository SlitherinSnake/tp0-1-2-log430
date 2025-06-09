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
* Rechercher un produit
![dss-rechercher-produit](https://img.plantuml.biz/plantuml/svg/pLRBRXDB4DrRyZ-KlCa49GikEtJ3A_OL5kOIOYGsR3ez5QTHJ_VG3mjs_0ChTYnZrlp1_0bVGbNFqtPu23PO6jipKttgr3cdntxWWt0X5dey2YfeXEbo2L_VVu1pb5Ve-81ee7GsZof0jbO2JgtnLYDz16UrHdSu7er71t1oSW8FPS3eF600QOlIUc621rMKMhs9rSPp4TSk3c9GMdd1vN0L2w4CPFe0gTA-gpO4AMIm3cRf0lAQkGdeBeL4WauCu4rKe8cM5k25yehAIXf7ILLINvWqJ25RIc4C4Ps0y2r_XampL5yqaoYTChnG9rZBi_lW43Hw82wALkt0FnKhuPNfXMZXcrFkC7tuBE49AQlh7A7wn6mVJO6LVAmkq1dIO0Vuh9QRrYP4-cIeHy8Z44HkZQasnCgX5ipB87M3Mb2wKRlKOPEKkGZYWbTFpgmFAuVBvuj9Rzgqsbp407a4BwkD4KxPsgT6V3Y-rue6TskbIVC3Mt9pcBYReZA1LuOH2pGHSc4yZorxvHTVttySHa2nkM95m6ADSb0CfQNRyifIrzGWl2kJ35rRoERJ8p2sE08RGOfGRLRMAKonR7dN8pJ6q1GzwKRip3SPIUpwzg8iaxp4gAWsuSgo0v5lTyb9Z8bPap90Ka4uKLJNb9zUvbEoylQmDLU1oj44TQdO-2Iwq66dP3uTZu1UWm5e74xMrXp2pVDCvOTrCGs1Moo38nSAJBl58Ut6pu6cYmadywZ9IXgJXNuBjnkG-fqhg66V7Ugxd2rcfo_W5jlyt_cpfwtldY-yV3mxdz5aS0BtMpPT7c556QkkQMT5WpqJXcsitd_krFGy7GEvXXG7t8uxRHfdEpq4f4tWqAojulPWBrwJ86qNQPr3NrgJxw0d0NOJj8K-j6PhjuZBIyNJP4tvq9ssBwsCwse3zBnfwkbaRyyCO_JFNeme_f0LDXnBJK_FEaceFUwN_MAM_hT5HjM7VnG1PhMOFvcrCRZHfeNouTz6ddi_4Fo6kf_ApckmMUEpJlmp7UdNWd-eEiubnZL5Qn02XqTiW6Q8BMzspBYpPqR_3Fgk4Nt9B_fnIbidrMUo_VPRtjJ6qEKEvYzp24q-7ZsWz_Ilv1i0)
* Enregistrer une vente
![dss-enregistrerVente](https://img.plantuml.biz/plantuml/svg/ZLVDRjj64BuBq3iCT97SDeUsjm9kubeigiQE54jfBmMADRcojoLtwVqna5kkFVVKMs-1SkuRy4to99rPxd99Mh5Zs9PNcj_l-sQuVc4iqzPLvNHYXIqvBBTR-FZkRyYavZV2MCqrECdXdalBON4l69mhQLJ9ySLgLPPSdqmdqyaSJazFuIott6a3t4AkgbeP9gq1KTMbk7FDmP3HTC9ogpHa-AvwowUJ6aiGkQZH5cOXEYosEPCIWy-0cPXpP7l9RfWHCbKIRSDnP1uIBi_MtYuTHtORhGedRBHBnv7TBuH9j8g7wQHWbbqpmp6GClP6y-thYrc2QDaShfs5me79cuDaMgWMbJbSpBclgJb0Vgk44MWsvqUP828GfOfBXqW7S497yEpx21Xy1wOvb3otGY6QYC6nQ3FuJKB1f85oBl2izopdAMoCrOODs6BaNcM--uK9bALccxyigpbyZTQh0ct4NkICQe62OxAktGY0psfryr-9CaARYablA4RE7z_zuoHqxis_u9dH4An1EK9OgigN7sl63XBhM9Fc-ItNzBDXUbKiH752deEtNwbgWN5EgBuhZYh-WF0ZfL0hB55pZEtkCNRgfYE2ac3A90DCkSqkilCTV46G_tYrlWnDS4Dl_FfJTfK1bdyAp-cyVaCjmCNgPGRFfvDUe6VZLYBtlfnn3MFckj5x03dR8yYt228M9EHUwSeZRaPyjM9X0urq80_a5mBgH5PFa5JdI2hVeCyFokNeZ2cJBPuCrEqecZGmpefI64PR1_M8KaHL4uLrwOpmgwZpOVSyXpk7eoiihgBff5IgHdBGCxzbTuv7Kp0CPsyw0Vpwf2OnJHGbBOEYysxzXg9BS0HtJIGF74XSGNMat5XA19eMoh78QJaTaNC8iYfEMjUUyPEox60RoJ946YfnKCMctrO_vLY5dM54O1cmnU-E9vhjia_pP-AR0XZbRXdznMCcLjh-vmubIW-g071RowN03Owv_o1zqBAsAlyJZ20oaYytI94mjPBYkkpvFq9InItAzEndZBkbc96bht3KSP6Wp-isF3z9yHvXxircURRB1dnjirsiaSTLyf33ftXSlpoUVqmZJiTUX3tHHt2yMH19IukbqpOnoyBpZ4iq3ZS7HAX37isjigpy5BI3Ia8AzFL1Dpr8Hc4hT4DyTYaqBv7lbcGXEqdq-pdpcupTe3xdEFqLDwOzSAsfGT96346wE1hcwJqvUr34B5GeYpGgs1lUhaJtD6nRfsSwC4ThyBEsKUsZy41EVAmE0f1QGkR1dluMtSzrSsWVo0XjkWtWfSGln8xSiAspeGLT44i-jFJ7_iusHkN2R-jX3_6wTUmQuaFvv_LAOdE-G5BuaKIbk97SzBPtIianPvTbF7IhLzliQeSlktKRq8liI_hruAFVEof6qzRhWMOj9NjymaP4x8LaFGMH9Fls4VoUUkc2qxnt0pne5edz_fvzMgjuJxHEjsWs1yj4QTfulKjZkgWapuS5J2SlK6N-7v7_0G00)
* Gérer les retours
![dss-gererRetour](https://img.plantuml.biz/plantuml/svg/bLTDRnit4BqBq7yOw28bTGAqlGLm4zTM6gLEx5XEUYaGKBkKmtJ5LVWXjBTSU-wfjzvQzTn_iFyalwHloEL-oKaGmx2rNCwRDpDlXjGZwuHnVbECHqwvGjBPSaaVtlz1FrHx8mrvJKQwqflnQ3oQql7nCPraJdfZIJhAoissj48xIsgpBTGxN-qjR-AzvrFlLA6iuEtqnc-4qZGhfX8xoz-g_PtnI6IkD3GF2v9j5jf9inQP1BIs9VYyyueAvQWGP8Mo2jksOAmojOLVcfpM-sRBJ6WjpPq92KlrAaFYepFLVmMJs1gbsR08iAN9taZhZAH2MW8N1Qi1_3Dn8wpI0QGi2caIVFt87ivEBcYB504Yg_OvibRznNnt4j5G_k7zdwLHDqeFgR_Yzp0EaEcX1xYHZka1xYq_8aphokmNJdtZI_XV4LUPU-M6v2_ZSdBHFlQSI6taZU8y10-ft3sagx2OW9ednlbUM0I9trBhQez46ba8JgMMI1153U05pNPAq1EraaP34gX-BfnOiVNaihJkniZbY_D9gxEpQh_oZd9F5g5gOLGPfLNhXUvzb-fC3mWk2fb5lpGPgdT2FslAXRPKJDLGOuTYpTwKa9ghBIJge3t7w8nGhelBYstonwF49rAhDG9sDn9qOjfcjXPgS2iQeOljDYIi2BmQoOgTpDXVBMzkkGry2YCsrJ-eMPklwdSdjfA-Xhj53ZMfjSf28PXa8qTWPwW05oGaiqCJf9CAWM4akeCxn5mAiyXdmNoHHzfzZhdS20qdmWTLDRwaHf-XmvX3YCD27jrqDUxWkjK0d2_dv_FJQzhTltj4k-tzkro-nrSNpoAmfHqldCyVNzFJYyNpkFgwHeO1lxt0ydsLqp43n0rumekVdiolvkdTGmwd4IN9zHeYyf0jLFIsX5Gy-Y-qYQdscTz6NRKHkbvC735RMkb39s4U_kj2YJhHtUldENoaMSXF0AtRrDxfvcSegF2cNxrUEuWrge_QKLxjdL25xOgWLbuTWdNrRjR4GPbYy2WZ5HXaQNRJ3ec0mEBeL7fTsWCXViDr32CRlZk0c6QnULZEGOD5MMvH_z1uWbsbIjRxglruHFZfQxKDjjQV8PGqcNQzHEkFofQdTQ_ziHRE6XHwkZWB4nmTM2Dpd0md2jSu0okFZDJpeiaorJzJUfNI5HBP1XZSq1f_CLWeZ5pPQzTysfmT2UspMNZdCREL0vz-4be8vjxkseb2vHq7aV9ICOjsLrEBA9ciD9WGRc3MvzOUEA0NpxAJfm1PyY4sgtDmH1h9uz7t8lhYew6BsK4PZecNbsSdr_DMMkZkvVmwaPRStst7ftxlOVVRiaVuuiUFynYs8sIrLcOJnkqmaUVDgOsqRPHjZdFhcIE6PZfoauqihEnaNkdRAlJvrf-yE82OV66LSLsgwto40behRkA5HcAnnrZdyI5GJ-eMAmiHzoVF5z2l4mvzSFFXPXd0kCKoQ4b6n4ctpJYQqB6-uO1fl8wzPTl-waoZ0t8Xb4_F6nZZselLT0c80q9ulgmbnm_JrBcIgz9dC7HybKgJj3jlTjAxpanak83LFInhMhq9-osud28nyPJ7MTHUvJOXLqbQjntrpIKE89qhO7r9YNRXi7_9ASHzYB1YmxnlaXV6bprghur1D0qQz1JE4xOfLsybB8IrPQPGImwcfJ1ixCNpvVpg6l-kBsfaIrznAn--UDtCvK4pTmFaNkvu-tJx3hAyLbg4ify4POOMuCHMVuSxuSSoVBkeerd8IfF5fkV2TofNEdm_GGLN73KVVSsb4f3fxdi0cRStzsdDD2WR5zrmXscrC3pkngD745Vu2lW_)
* Consulter le stock
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


