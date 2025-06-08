# 6. Vue d’exécution

## 6.1. Scénario : Rechercher un produit

### Description
L’employé saisit l'option rechercher un produit dans le menu et ensuite saisit un critère de recherche (identifiant, nom ou catégorie) dans l’interface console. Le contrôleur transmet la demande au `ProduitDAO`, qui interroge la base de données SQLite et retourne les résultats à afficher.

### Diagramme de séquence
![dss-rechercher-produit](https://img.plantuml.biz/plantuml/svg/pLRBRXDB4DrRyZ-KlCa49GikEtJ3A_OL5kOIOYGsR3ez5QTHJ_VG3mjs_0ChTYnZrlp1_0bVGbNFqtPu23PO6jipKttgr3cdntxWWt0X5dey2YfeXEbo2L_VVu1pb5Ve-81ee7GsZof0jbO2JgtnLYDz16UrHdSu7er71t1oSW8FPS3eF600QOlIUc621rMKMhs9rSPp4TSk3c9GMdd1vN0L2w4CPFe0gTA-gpO4AMIm3cRf0lAQkGdeBeL4WauCu4rKe8cM5k25yehAIXf7ILLINvWqJ25RIc4C4Ps0y2r_XampL5yqaoYTChnG9rZBi_lW43Hw82wALkt0FnKhuPNfXMZXcrFkC7tuBE49AQlh7A7wn6mVJO6LVAmkq1dIO0Vuh9QRrYP4-cIeHy8Z44HkZQasnCgX5ipB87M3Mb2wKRlKOPEKkGZYWbTFpgmFAuVBvuj9Rzgqsbp407a4BwkD4KxPsgT6V3Y-rue6TskbIVC3Mt9pcBYReZA1LuOH2pGHSc4yZorxvHTVttySHa2nkM95m6ADSb0CfQNRyifIrzGWl2kJ35rRoERJ8p2sE08RGOfGRLRMAKonR7dN8pJ6q1GzwKRip3SPIUpwzg8iaxp4gAWsuSgo0v5lTyb9Z8bPap90Ka4uKLJNb9zUvbEoylQmDLU1oj44TQdO-2Iwq66dP3uTZu1UWm5e74xMrXp2pVDCvOTrCGs1Moo38nSAJBl58Ut6pu6cYmadywZ9IXgJXNuBjnkG-fqhg66V7Ugxd2rcfo_W5jlyt_cpfwtldY-yV3mxdz5aS0BtMpPT7c556QkkQMT5WpqJXcsitd_krFGy7GEvXXG7t8uxRHfdEpq4f4tWqAojulPWBrwJ86qNQPr3NrgJxw0d0NOJj8K-j6PhjuZBIyNJP4tvq9ssBwsCwse3zBnfwkbaRyyCO_JFNeme_f0LDXnBJK_FEaceFUwN_MAM_hT5HjM7VnG1PhMOFvcrCRZHfeNouTz6ddi_4Fo6kf_ApckmMUEpJlmp7UdNWd-eEiubnZL5Qn02XqTiW6Q8BMzspBYpPqR_3Fgk4Nt9B_fnIbidrMUo_VPRtjJ6qEKEvYzp24q-7ZsWz_Ilv1i0)

## 6.2. Scénario : Enregistrer une vente

### Description
L’employé sélectionne les produits à vendre via l’interface console. Le contrôleur crée une entité `Vente` et transmet l’enregistrement au `VenteDAO`, qui effectue la transaction en base avec Hibernate.

### Diagramme de séquence
*(voir diagramme PlantUML ci-joint)*

## 6.3. Scénario : Gérer un retour

### Description
L’employé saisit un retour de produit dans l’interface console. Le contrôleur crée une entité `Retour` et la transmet au `RetourDAO`, qui enregistre le retour et met à jour le stock dans la base.

### Diagramme de séquence
*(voir diagramme PlantUML ci-joint)*

## 6.4. Scénario : Consulter le stock

### Description
L’employé demande la quantité disponible d’un produit via la console. Le contrôleur interroge le `ProduitDAO`, qui récupère l’information depuis SQLite et la transmet à l’interface.

### Diagramme de séquence
*(voir diagramme PlantUML ci-joint)*
