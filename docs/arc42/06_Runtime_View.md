# 6. Vue d’exécution

## 6.1. Scénario : Rechercher un produit

### Description

L’employé accède à la page “Rechercher un produit” via l’interface web.
Il saisit un critère de recherche (ex. identifiant, nom, ou catégorie) dans un champ de formulaire.

Le contrôleur `ProduitController` reçoit la requête HTTP avec le paramètre de recherche,
et appelle le `ProduitRepository`, qui exécute une requête JPA personnalisée
sur la base de données PostgreSQL. Les produits correspondants sont récupérés
et envoyés à la vue `home.html`, où ils sont affichés sous forme de tableau.

Cela permet à l’employé de consulter facilement les détails des produits
et de filtrer selon les besoins du client.

### Diagramme de séquence

![dss-rechercher-produit](https://img.plantuml.biz/plantuml/svg/XLMxRXD15Eqj-H-ktE0W4WGb8a0m1YAPLsoHXcOoUsqFcfrPvc7YZfQQYewEc9e_s3_9bt3lUBqFAq1XoMlVnpddddltYG_2XPZhVY-ee16UJQTm_UKRdA5SekCFH0E5irbK0ORJmYcpWAVM1ZZ7YuD-hz-x1SV7nt0Y0qO7_BzKXT3yOxydPB0EndcXxHgRu97D2-k52PwhR4CB0g8urGGOl1OhjH2f8WSCPyjrZXh5_600miFxY7i9RxS8HzO4PxL6b-AQnvl3pv1WA8AuJk7ysEzb8eWBuP6Yh0yBXzDtam5Z9-3bro0AX7lm0AP2UOMGHP1EXVBAqNS4jzCDc5D570uVCMHA4bAMLnHM_e2xbQWUXY_6imEMPdBzvJkcb783Dgwef1PGYCLUQNgeapCielAWALMPW6uk9C9dl630t9DQDoeGWfUpsLkWXat_nvyYklNnqT5HmY2eqQTO_We8w0EWMJ4S1454kL3vM_DOn0fbiePm8Y-l0c4Tf4836Ahi3XYR4mMG8fIRXNKAAwzCi5F6OVdJahDqgss7Q-f3ngn5zgpxttkqo19rdYYHRieLwBmrHcZbcPH7RKrTc_6TQX15WLI0s27avIOiRPRcx6gta2emjqO6HTdPLXWqaP5N8Z1POucCKujb03llU75yoMD63m_wlKEOAvCzNPzcnEcWUNvjSquHoj36dIwCTJWYLmw9T2jgH4kIgDyOAtV__h_kYSd-NOlyGLNGPC2yRXYbN5gQJ81iRKIkI8OSQTNOc2maIIE9y4vFpn8AciEQpm2m5dEboKMUbKjZe_4r2yWBDvwCHpEu3HyCF3zxywhPWVENuxCnx0oCdK5TbXiPmtQOjSEuu8gSmBPBgyu39bkG9LB_Lj_3naKJvSF3wd8yubQjC8V1HcVImfAtaejzWr1QHzGAQp9cA-9bxRaMt_rLTUN6Hnt4VhxVqlJj039r813RPVLxozezNlUfzCbyhkHoUrWwTVXgjLUelbmrmS7IvdYq3BaULCjPqQtPtk0DyT7o47It6Q7CRLPUqQvXXLub1LSHkvInFcS4Q7TLwPmc6jYkIT6UxnBTS7fVnAshoitkl7PkP9tVYg_kJMjsf4LSKGE6y8JydzwMVm00)

## 6.2. Scénario : Enregistrer une vente

### Description

L’employé accède à la page de vente via l’interface web et ajoute des produits au panier
à l’aide d’actions AJAX. À chaque ajout, le `VenteController` délègue au `VenteService`
la validation de la quantité disponible via le `ProduitRepository`.

Si le stock est suffisant, le produit est ajouté à la vente en cours (stock décrémenté).
Une fois tous les articles sélectionnés, l’employé valide la vente. Le contrôleur
enregistre alors l’entité `Vente` dans la base de données via `VenteRepository`,
avec ses lignes `VenteProduit`.

Une fois la transaction confirmée, un reçu de vente est affiché dynamiquement à l’écran
dans la vue `facture.html`.

### Diagramme de séquence

![dss-enregistrerVente](https://img.plantuml.biz/plantuml/svg/XLNDRjD04BvRyZkCSag0f09kI2bDMvSMIXDYat90GbjxaYuuNdTt7IcgaBXovWMuGPyZRyAJC5x_coJaaltvPkQRcM_7HqepgPDbQ5kQwn3Xr7NXpxSVu4GI5rnfYHAI2665aKRekh7aqGAEXT1mZJSzsx8jvciXmLd6eLYZRSNaaFiyPf66pXLRyGNJcCWK3btlTht44Dcyrm6cO9Qq3MPfd1CHQId24AL1LTjjO1VbYljeaFcw1SkfJJ4MYXFNjS7MJbluYHH1mdKBNplTHgE5BiziAs2QtJ25v5WelP3elhlif8KR3c7pNRCOuIcyW97lRtuhO0cGLqvb7mvjAoS9_SEqLeHYAIf0sFo4WnpNVULur8Zqdc1LiGXzxdaJeEi2QrkrwtxfLA8MYQGkPwZ1hLw6m5Revst727m2g2RfPodfpoBHG7pYh3hRIViXlwD5nw0fSfbZPm_joTWjUH-m883xFCP5y0Jk4geyr_YrcT5XeO2qKbcml6tTBSPKj09EbhN-alMSHy7n-YAex0XTXwGMPJl9m7KkdHCF7i772CwcuxT5HLHwS7tkJ1tW0GpX9RcfxFh_1CvthM2Dp4e8lRrGWzB2_m8gcS-veiHi2-YtCx4bLqOzL1sPctQBWa0VxdgP_RvKtqzEHvxJID0bZPLUXZ0O35eqsYqkz6Awj7cmBGo9LyQ7HxkIQNj1AR58Wrn4mGw1Dv8kdisACnYz7dteDMNzdF1dF68X4T1SHFibFIDSGDv2bhsL_olQmDJ1AZUyD_izEiPg4fkXqZK6JUtMPbbTkvWT6-tMqELi8goYPrvwh2xQBR-uSfsfHt_UECjJmUDiyIak1TkKTJtA-4t3UIC_NqHpBfUcrZMQxGfcaoW7RnumRzg8XEBViWMQDksORW6Nw9jUhb0gcDCNAv5enbpUhgg6wUGmFf4CDx-ITBWLpKrZ7f7ApFVoBm00)

## 6.3. Scénario : Gérer un retour

### Description

L’employé accède à la page “Retourner un produit” via l’interface web.
Il saisit l’identifiant d’une vente existante pour visualiser les produits achetés.

Le contrôleur `RetourController` utilise `VenteRepository` pour retrouver la vente
et affiche dynamiquement les lignes concernées. L’employé sélectionne les produits à retourner
et saisit les quantités via un formulaire.

À la soumission, le contrôleur délègue au `RetourService` la validation des données,
la mise à jour des stocks via `ProduitRepository` et l’enregistrement du retour dans
`RetourRepository`. Une fois le traitement complété, un reçu de retour est affiché
à l’employé via la vue Thymeleaf.

### Diagramme de séquence

![dss-gererRetour](https://img.plantuml.biz/plantuml/svg/ZLNBRjD05DrRyZzSTPLI6iHZXHHecvWsecr2RDeD4fhQD-c0xJ4puuZikcNDYXqxo7VuJ_ebtF7RRa0i8jipvynzd7CdHqepgTCej2tDTOWmSLsul_iEfzbMeeGq1ebQf186RY9vl88J8JHSuys-RTaMyxMGu4H9A3PeMmcTnNsUi5Z3tYLRynNJI5o2my2xtKGO8blkxm5JS9Ns2Oiyq5Z4MeemH9d3ci-TQ1VbclkOGylt7kuAOuqBJ8JYbEkcY5sjxJop1suMU-Yv547ATG_UMhMjW6bsmnGIM2YzakY-EzypZHkD8FkcMOBm55x2iUzdlnLaFo5aaB0LGe1LtqSZsoeR38UlJDk8m0o1CCHuKkbpwdYaYTadMDCsGfzvtXnekyBQLckxFhFOYn6MGaPfoBYiSXZUQcEGTjRFYB10_nOb_KosbFZQT7LtlefnQX64ztS_ffCs_1y9psTkdJ4zobXN1QjNGAKdSPSy3ausqs2GdpyDJ4_gRS8sGX3OTSwTiGUFu4CCRnQpYx86-he-SnOEy016yDgsMgJ3NhmoeLQCVZMxKovqN2wvQM282fB2EAhRddRJdvkCiss8lkOY9boBH4we1FoBPrJD98rhtW5yISdDN6VRxY7_gyoQXJp01vugXv4e64jSSQNfEYb6gLBcCKGiAST5aKmLnOZK316T476LM_sJyRcfMcdXVrOm-5hNGVGsfw_p-_das7EQgabWb-QXB9rC2yFXi9D1C_MvVTSuAEVi04A-Yb4L08hRGlR3JYzTP-7Hmvj1JIzUFfQvFF1M9_3iRVVyJcDz4I-vZ9YHj6kK5xGxRktcXgYBwsjQPbRhb6sxOhPkY80kXS9y8YtDQsuhYTclj3GCeIlhDVxE0L3TFhPrX76G_-dy0G00)

## 6.4. Scénario : Consulter le stock central (UC2)

### Description

L’employé accède à la page “Stock central” via l’interface web (navigateur).
Le contrôleur `StockCentralController` traite la requête HTTP `GET /stock`
et délègue au service `StockCentralService` la récupération des produits disponibles.

Ce service interroge le `ProduitRepository`, qui exécute une requête JPA
sur la base de données PostgreSQL pour obtenir la quantité actuelle des produits.

Le contrôleur retourne ensuite la vue Thymeleaf `stock.html`,
qui affiche dynamiquement la liste des produits.
Si un magasin est sélectionné, l’historique des demandes de réapprovisionnement
est également récupéré et affiché.

### Diagramme de séquence

![dss-consulterStock](https://img.plantuml.biz/plantuml/svg/XLHBRjim4DqBq1q6NZajukw3oldOQbF0QLtBQ3O50beQoqGfKYKf0zxb3brrrsLz3jqa9-b8fcL5JjoTD7nlvl7D3A-CPTeMcV0zowr064SHF3t-Wf6IfX0MDL3GM1N_W1YbrKn0FyerbodSA6NX7XTdlkTxBBPAGvZbGctGztBAocEUCscXzvcjUSei5hg2Gt--scGea2tFUi0CV2kwXAYgDzgL8o5MAo5Gr-33xmbEX7hDOwm9xhk3dcgL5DpEC5U6a_9DZMr5E_WxbZB3PGVVYlfUmYnRC8EKN1cRQeo-JdgLDK40KRaL65kk92HC6X1FZx-v97UNB4O80jzppi7viF83Be35SVatGIZ_mBkT_zybh3c336K1gW31KVfU1IREmHQYtixdK_WOpXtnCjl9_9G4Wy70APgebFyaRbPkBITMfQ6LX58wuoXVYjOvOyRSv4hoXK3JfxxlCGHlUKUK9PV9jH0LfdrGuHfp21Q5at0qXpVLtJxCljn1xkeGxO0xFqe_uSQ-Tw5XfyGpwHrWexThMGT7yomiED_0DDsJvVQaJMFCc4pGJ9bsiz5lp7zkLtjS3rFbdBlPlCX-cV9VJ-iXUDXlBqNlRyDPkB_T0qyWWCiZmryLM5lLFXs-Bksu5rtYoOPqm2WJDxtNooMFLop5exbjzaUZLOMMj8xr_GShcmbQsZN6pP31MqXSmzmY4Nk_Y1Uk20AjDJrMnO665bOaJ-bgWof5LwIiVaF_0G00)

## 6.5. Scénario : Générer un rapport consolidé des ventes (UC1)

### Description

Le gestionnaire accède à la page “Rapport” via l’interface web. Le contrôleur `RapportController` traite la requête `GET /rapport` et délègue à `RapportService` la génération d’un rapport détaillé.  

Ce service regroupe les ventes par magasin via `VenteRepository`, identifie les produits les plus vendus, puis consulte le stock actuel à l’aide de `ProduitRepository`.  

Les données combinées sont retournées sous forme d’un objet rapport consolidé, ensuite injecté dans la vue `rapport.html`. L’interface affiche un tableau récapitulatif par magasin avec des sous-sections sur les ventes, les stocks, et les tendances.

### Diagramme de séquence  

![dss-genererRapport](https://img.plantuml.biz/plantuml/svg/ZLRDRXj73BuRq3iGlbGEx1RzknIr4LjItGPAf4fAYW85dD4kjPfqDBEP7y6-vTfpJxtrLgldli6-IPwavEnA-sF3xKcxioG_ykD7ZfuxBwmF6zKxyj8hXE5y3Xy__0xNnKuNEuiMWWOhyjnO3udHpYYP5Zj8qS4MjQUV_ZotKcTmPOo7dt1vqZlg7Nq2v-VdS9bu31RuUo9peVYmToGIRonSe_FIQ2sanTfZO3QvSK9xnw5A-vnIbEolFHo_4bkPYHYM3VgBzTq65OhLoJ481sy2TXncPV83exqrIg6DTlNhu-Ppj5kPOBIjdZk6RxZw6LAsaigvAz7tPntRgJLfaBvZtJZjsC-zINvzAJBXfEuunK-bIogyM0g75DyudrcS_pW-PYg9n-8tBtA4p-4RQa9It3ie_WJrySCVaZAqAv4WCBND9i3P1TD77e8zKcINp_Qzxr-F5YVSfJ52rlHAb7nFNYv4gInDy4Q32VoY96hOeEQQH7X4HfmiGn9otH1AuFl5OWe4U82Fm08ilW_5tnw14g2NuY_o9UA0X9kmV_6FOc6yVT3pjsrQlY2CMR5BGauQ5rnBKz1CJ2CTIgvI07bbg96ceWBexnbfebDTghZFA3KbOgtUXgJ4g3aWEbmLT5Fil6GTzexsEDo9lPG8T2LrUgdK0MtlQSiIxgaBddYdr5LT1MgBcIJmOXVVu6eOwJx49P1QDuGo7ur7WmKyW-zcavTL50QSxKbdCxnbJ4ynOIi5l9XUGcweQBprDigXpe3H6W1ddQB6bDktyU2YbLMBRZPYTZSvKKfFQIjwtT0loVQ7b3x8bKpgXkRbXD63oOFY_ChkFi5pDPHkZ6wgWgFFQN0byvTvZWfI6gAWiTPYZjRnC2Zfc1QIliY8zAnC8x8ZgzJGHdwM9DypXwmkzxzwKjLn8zDJ64nUlrhqdvt0BxhHgPlAX4-lPvFNKxZwkU76nvFPS3JZunW1XgFvuCa6BKn-8F2_EjGYcqlCYJlEZCXhT-ehyk576e741-QJh1qljGUjEQowxWZGSBgWVDrdBRSemYqzbw6gs2ukXqsvH-Dy7I8-BVuOvERWs26fcLTKSNDTNxHGMbJ5ysvADJDVywvs3ZTB9RAIqfHqLOwlvs7FgMviizJTdCF5vD4zszeRrL8YjT2IDijt5BrSKe50G6mngORij2NLKuY6L0GdtjoGXvl38YqYozDHvl3fsczK6N5VooCRK-ftIFyKU8tlSq-5PY71ngJ5FVqICkgjaN6fRWdhSEyVxX1AedMTqJssecsmRWIjj-C3RbftsbQwmFECJ11TieH3-_fE-pNlQhxki8oMKUWr7pZMdYUz2vt4lpFFMVlyl-fV)

## 6.6. Scénario : Visualiser les performances des magasins (UC3)

### Description

Un gestionnaire accède à la page du tableau de bord via l’interface web. Le contrôleur `DashboardController` traite la requête `GET /dashboard` et interroge le service `RapportService` pour récupérer les indicateurs de performance : chiffre d’affaires par magasin, alertes de rupture de stock, produits en surstock et tendances récentes.

Les données sont agrégées à partir des `VenteRepository`, `ProduitRepository` et `StockMagasinRepository`, puis structurées sous forme d’un objet DTO envoyé à la vue `dashboard.html`. L’interface affiche des graphiques interactifs, des alertes visuelles, et un résumé global des performances de chaque point de vente.

### Diagramme de séquence  

![dss-visualiserDashboard](https://img.plantuml.biz/plantuml/svg/bLZBRXit5DqRy3zSU1EfaDFtnaYCE8wRfB5hLr8S52ZWK3EKH7HU9ZbgZA90jbrtrLsNTjVz0_r9lgJdafodbBfT6F3Cy77kkUSUNkg9iKBRCarsTwooYQHdambzUFyxNIfJYaGPgIcHXWgfvxbEHHRX8SPVAXR2gCpGO59ebIteQPvRUYDdmzsTtPq7zFZnOpgAh2mryVT85IBXbxix8hAvfkVIM9LdcL1QylXJIOlMAzAoqEix8pChgSoej0fWX5jFt8ZCugMaLIaf_l3-3vaPcSw0tfGURbwixxJWrRfe7pRWZlEqo0rMCeoQ1a97IsLbP4liVdPvJAsuXXvv0QeKHuBjzxuJAxNmWDpywV8cbOaKy-4U2KENfGlh9V3hkOWa_On1FehrdQGYvq3cSnKj0yCgYrNarZEq0lao0RTM82fHlXlHGejYgQvBQKOa4gcjD4CNJGVKM1H5hkrndbcT9nZci3IF3CazhVzE67c8SS5iOJxZqFAwNF-5vUd5T7f1YG9SGwAaW0LyqYpNyKStdqYzKf5qEuV_UTlmBwNhEwiGCxBB-lBRajK2vAT8zuWIP5Giy7tXCaZI-bUPI6wCTFcC8HF6jGdYaXKpbaYjWinkV2wgTuoZ-LQd8sGZYjPtCP21Rrhn54CoqtAX3FXY4WPGo7oklUQGF1Ogqc5b5hl2605gz71BMYvq7fVAzc2rtZAmmNdXv2yJmiHX6oYgBvNMybfWFzAvgyis4IWojvWXemXlWCOml4qi4vj7FvtvUkW1Sf-sy1HKuWdYHPYXJArOcGp8y3o7R2cWqAhQtDwnW6u4qhPtaHkxq7Bo_UaUh_zKk5ms4MYPY400114X0Lg4QcdLZT1ayz8ugKPD2Q-Kg60-muxqxSMHsnw5llxDYaBIvtG0GuhMjzl4J5pURKkY_KCkOSpXg6ynRFqdVHeBivpb0kC7pq-cmss-5ILSfCw8zgRTJVO8v1cdPTMn1IU_V0OTASrcnyPNeaveaUGpaRX060mmDQKCQAv00QG5p44ITH7x8S371PQP5Bf1z3PFmJHWShqn6r7B4jvkUCZR7fDVOEin95RM9ainrgXguq5L6IvPGGkEu1PuX58PmW48jCT-zb6OUY5qaEYWOhcD4GYTIDZnmoeT3jbGSei3Xql4b_88d2nqpfZRk5ne5HBEUMKLW3M79T-3QrpnZ1JR6t3pV7p--eAU_a3o7JDaQw_VKXhDNjYugGxiF3avFJcULeElL3oYoUkpGGeAeFOh5zkGVipec_7vMULLUAntRsPsKFBglJCei18EeR23sQ2e1M-_HzCf_F7HBs7mo1F_ws4GeYjzBL6oEfFkpCRcdg_2YsJGdTbAsR3FKaSqOowIn0g0zOS5dhrKZ2yW_yqWeWe0tlgWczE6yZ8Sms8XUph_SelE-IZqfszZVFT8EzYyESd6PS7TnNavmI7FKxUAEvPeisBfyb7jDQZj7T-kIvSY89SP6urRQjXNSctcGSb7IL9lLnMDio6TBsJZgHksxi_6ed2-fdqAyuKpEOoPgmHa1kgQBJyYw4-yODt2Lo7l7f9lkzZGa_MjzOVy1XI4E-kT6jl4sOxSYRDzv1rsO7PovTvx3DBwHd7Q2ANgMmvGsWWvglLGfmRmjCymvwPAJ3SfzMQEE6w3_0p3NKurnVJILB7V9z_8Kg5BF20Ulqz1Njm1E8aek0_4IPynd4T-DAMUyNsgDDWTVbWD4--wnV0L7v-j3gnkUcWfPp4EbDYNypt5CAsclU1P_wf_3hRTymMROsT2jozK5XnirRsfrC3kitRtp8u-yQjjL4Ux4CFczHdI1VBVxFpDoSch0VeXUULM6_vVK-_FRrjijv6gxRPf9TfPybUYokgnM5nkBQHDbm_nZghPxFCVC-X68G3d4BlRHwTfbUNAjI29uXfpzjBST71KO9W3F8WGqSmVis7POS-jlsQtvb32xSafiKv_ltMg8VGRmS7nKNt1QhNsZknfNd9EadO8hLux73RLQUHhMijibTzKTwYMpXtyTgNK7MUTlxghUxYqQK9Y9QFsDJ7alu3zeiyxEtLzdcOAf1_RlrR2sb9yibQhMOdCWwiqZzUtQ9VgsslJR09FfoC-gEwhzEhYPSqHn-YJu_5qcbjMYUQEiziZ7tW7uhhVSg5gznxLm9RBHbgvtXM7eeVW3uTXCtHJU3sF6ZINQVThnHFcZ7y2-GS0)

## 6.7. Scénario : Authentification et obtention d’un token JWT

### Description  

L’employé ouvre la page **« Se connecter »** (`/login`) depuis le navigateur.  
Après avoir saisi son nom d’utilisateur et son mot de passe :

1. Le navigateur émet un **POST** `/api/v1/auth/login` contenant le JSON `{ username, password }`.  
2. Le **`AuthController`** reçoit la requête et délègue à **Spring Security** la vérification des identifiants :  
   - `UserDetailsServiceImpl` charge l’objet `User` et ses `Role` depuis `UserRepository`.  
   - Le mot de passe est comparé en **BCrypt**.  
3. Si l’authentification réussit, le contrôleur appelle **`JwtUtils.generateJwtToken()`** pour créer un **JWT signé** (contenant le `username` et la liste des rôles).  
4. La réponse JSON `{ token: "...", id: 42, username: "...", roles: [...] }` est renvoyée au navigateur.  
5. Le navigateur stocke le token (ex. `localStorage`) ; toutes les requêtes API suivantes incluent l’en-tête  

### Diagramme de séquence  

![dss-authentification](https://img.plantuml.biz/plantuml/svg/VP9DJiCm48NtaNA7KJQaYog5PHQeAIg88j5ABMB3nZIcZ71jOqzQ5SI1oZLwCIPEVq4YgzZpvhrlhEbw4ewgkOeZCINZe3-togna73sRIfV2hQ3dpD9B5qTJeyaP1KbHqUjLSr3IFUaq4nQpnNack90fCqFTIa1u249YRyiXE4KhD44oMD83eV91meTVpXFp9lKrAf8kA9hVE8gZBHkqBtTGaCDOe4S98JMpmlkbSMKSxGIitL80i7eq74_WBowaW_7mlXL7UottCHg4BuUPrD89admLy5AMxiljeumS1e-JOviJrejE1uPty0bKcmGjVDMJdK5XhLHGZ6wXKhHPEvILEvcYUOO6vwP_6C8wKrP87Y2jKut33q5esBqdXUCE3c7QVVlfO0kXiFnli5rVpk37BbnAHarqOV6m3GahdNolDjyaeNvWgGbVSBFcbUfANOPL-m40)
