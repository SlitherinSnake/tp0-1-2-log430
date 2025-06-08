# 5. Vue des blocs de construction

## 5.1. Boîte blanche – Vue d’ensemble du système

### Diagramme
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

### Motivation
J'ai pris l'architecture MVC, car je suis familier avec. Aussi elle offre une séparation claire entre l’interface utilisateur, la logique métier et la gestion des interactions utilisateurs.

### Blocs principaux (black box)

| Bloc         | Rôle                                              |
|--------------|---------------------------------------------------|
| View         | Affichage console et saisie utilisateur (CLI)     |
| Controller   | Gère les actions de l’utilisateur et appelle les services/DAO |
| Model/DAO    | Contient les entités JPA et l’accès aux données via Hibernate |
| PostgreSQL       | Base de données locale relationnelle              |

## 5.2. Niveau 2

| Sous-bloc     | Description                                      |
|---------------|--------------------------------------------------|
| EmployeDAO    | L'employer qui fait les ventes, retours et consulation du stock        |
| ProduitDAO    | Accès aux produits : recherche, liste, stock     |
| VenteDAO      | Enregistrement d’une vente                       |
| RetourDAO     | Gestion des retours et ajustement du stock       |
| HibernateUtil | Initialise les sessions Hibernate                |

## 5.3. Niveau 3

Ce niveau de détail n’est pas nécessaire dans notre contexte. Notre application repose sur une architecture à deux couches (client-console et base de données). Aucun sous-bloc supplémentaire n’est requis à ce stade.
