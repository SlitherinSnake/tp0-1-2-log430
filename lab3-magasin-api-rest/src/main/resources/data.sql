-- Supprimer les tables existantes si elles existent (dans l'ordre pour éviter les conflits de clés étrangères)
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Créer la table des rôles
CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(20) NOT NULL
);

-- Créer la table des utilisateurs
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

-- Créer la table de liaison entre utilisateurs et rôles (relation plusieurs-à-plusieurs)
CREATE TABLE IF NOT EXISTS user_roles (
    user_id INTEGER NOT NULL,
    role_id INTEGER NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Insérer les rôles par défaut
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');
INSERT INTO roles (name) VALUES ('ROLE_EMPLOYEE');
INSERT INTO roles (name) VALUES ('ROLE_VIEWER');

-- Supprimer d'abord les tables dépendantes pour éviter les violations de contraintes de clé étrangère
DROP TABLE IF EXISTS retour_produit CASCADE;
DROP TABLE IF EXISTS retour CASCADE;
DROP TABLE IF EXISTS vente_produit CASCADE;
DROP TABLE IF EXISTS vente CASCADE;
DROP TABLE IF EXISTS stock_magasin CASCADE;
DROP TABLE IF EXISTS stock_central CASCADE;

-- Ensuite, supprimer les tables de base
DROP TABLE IF EXISTS produits CASCADE;
DROP TABLE IF EXISTS employes CASCADE;
DROP TABLE IF EXISTS magasin CASCADE;

-- Création de la table employes
CREATE TABLE IF NOT EXISTS employes (
    id SERIAL PRIMARY KEY,
    identifiant VARCHAR(50) NOT NULL,
    nom VARCHAR(100) NOT NULL
);

-- Création de la table magasin (si pas déjà existante)
CREATE TABLE IF NOT EXISTS magasin (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    quartier VARCHAR(100) NOT NULL
);

-- Création de la table produits
CREATE TABLE produits (
    id SERIAL PRIMARY KEY,
    categorie VARCHAR(50),
    nom VARCHAR(100) UNIQUE,
    prix DECIMAL(10,2),
    quantite INTEGER
);

-- Création de la table stock_magasin
CREATE TABLE IF NOT EXISTS stock_magasin (
    id SERIAL PRIMARY KEY,
    quantite INTEGER NOT NULL,
    magasin_id INTEGER REFERENCES magasin(id),
    produit_id INTEGER REFERENCES produits(id)
);

-- Création de la table stock_central
CREATE TABLE IF NOT EXISTS stock_central (
    id SERIAL PRIMARY KEY,
    quantite_demandee INTEGER NOT NULL,
    date_demande DATE,
    magasin_id INTEGER REFERENCES magasin(id),
    produit_id INTEGER REFERENCES produits(id)
);

-- Création de la table ventes
CREATE TABLE IF NOT EXISTS ventes (
    id SERIAL PRIMARY KEY,
    date_vente DATE,
    montant_total DECIMAL(10,2),
    employe_id INTEGER REFERENCES employes(id),
    magasin_id INTEGER REFERENCES magasin(id)
);

-- Création de la table vente_produit
CREATE TABLE IF NOT EXISTS vente_produit (
    id SERIAL PRIMARY KEY,
    quantite INTEGER NOT NULL,
    produit_id INTEGER REFERENCES produits(id),
    vente_id INTEGER REFERENCES ventes(id)
);

-- Création de la table retours
CREATE TABLE IF NOT EXISTS retours (
    id SERIAL PRIMARY KEY,
    date_retour DATE,
    vente_id INTEGER REFERENCES ventes(id),
    employe_id INTEGER REFERENCES employes(id)
);

-- Création de la table retour_produit
CREATE TABLE IF NOT EXISTS retour_produit (
    id SERIAL PRIMARY KEY,
    quantite INTEGER NOT NULL,
    retour_id INTEGER REFERENCES retours(id),
    produit_id INTEGER REFERENCES produits(id)
);

-- Insertion des employés
INSERT INTO employes (id, identifiant, nom) VALUES 
    (1, 'emp001', 'Alice'),
    (2, 'emp002', 'Tom'),
    (3, 'emp003', 'Paul'),
    (4, 'emp004', 'Jennifer');

-- Insertion de magasins (y compris Maison Mère)
INSERT INTO magasin (id, nom, quartier) VALUES
    (1, 'Magasin A', 'Centre-ville'),
    (2, 'Magasin B', 'Vietnam'),
    (3, 'Magasin C', 'Paris');

-- Insertion des produits
INSERT INTO produits (id, categorie, nom, prix, quantite) VALUES 
    (1, 'Fruits', 'Banane', 0.99, 100),
    (2, 'Fruits', 'Fraise', 1.99, 50),
    (3, 'Fruits', 'Melon', 3.99, 30),
    (4, 'Legumes', 'Tomate', 2.99, 50),
    (5, 'Legumes', 'Concombre', 0.99, 75),
    (6, 'Legumes', 'Laitue', 1.99, 70),
    (7, 'Electroniques', 'TV', 100.99, 25),
    (8, 'Electroniques', 'PS5', 300.99, 50),
    (9, 'Electroniques', 'GTAVI', 120.99, 60),
    (10, 'Vetements', 'Chandail', 8.99, 50),
    (11, 'Vetements', 'Pantalon', 10.99, 50),
    (12, 'Vetements', 'Manteau', 250.99, 90);

-- Vider les données existantes de stock_magasin
DELETE FROM stock_magasin;

-- Insertion du stock pour Magasin A
INSERT INTO stock_magasin (produit_id, magasin_id, quantite) VALUES
    (1, 1, 80), -- Banane
    (2, 1, 40), -- Fraise
    (3, 1, 20), -- Melon
    (4, 1, 35), -- Tomate
    (5, 1, 60), -- Concombre
    (6, 1, 50), -- Laitue
    (7, 1, 15), -- TV
    (8, 1, 30), -- PS5
    (9, 1, 45), -- GTAVI
    (10, 1, 40), -- Chandail
    (11, 1, 40), -- Pantalon
    (12, 1, 70); -- Manteau

-- Insertion du stock pour Magasin B
INSERT INTO stock_magasin (produit_id, magasin_id, quantite) VALUES
    (1, 2, 60), -- Banane
    (2, 2, 30), -- Fraise
    (3, 2, 15), -- Melon
    (4, 2, 40), -- Tomate
    (5, 2, 55), -- Concombre
    (6, 2, 45), -- Laitue
    (7, 2, 20), -- TV
    (8, 2, 35), -- PS5
    (9, 2, 50), -- GTAVI
    (10, 2, 35), -- Chandail
    (11, 2, 30), -- Pantalon
    (12, 2, 60); -- Manteau

-- Insertion du stock pour Magasin C
INSERT INTO stock_magasin (produit_id, magasin_id, quantite) VALUES
    (1, 3, 70), -- Banane
    (2, 3, 35), -- Fraise
    (3, 3, 25), -- Melon
    (4, 3, 45), -- Tomate
    (5, 3, 65), -- Concombre
    (6, 3, 55), -- Laitue
    (7, 3, 18), -- TV
    (8, 3, 25), -- PS5
    (9, 3, 40), -- GTAVI
    (10, 3, 45), -- Chandail
    (11, 3, 35), -- Pantalon
    (12, 3, 75); -- Manteau

-- Important : respecter l’ordre des dépendances pour éviter les violations de contraintes
-- Vider les données liées aux ventes dans l’ordre des dépendances
DELETE FROM retour_produit;
DELETE FROM retours;
DELETE FROM vente_produit;
DELETE FROM ventes;

-- Insertion des ventes pour Magasin A
INSERT INTO ventes (id, date_vente, montant_total, employe_id, magasin_id) VALUES
    (1, '2025-06-01', 1500.50, 1, 1),
    (2, '2025-06-02', 2200.75, 2, 1),
    (3, '2025-06-03', 1800.25, 1, 1),
    (4, '2025-06-04', 3100.00, 3, 1),
    (5, '2025-06-05', 2500.50, 2, 1),
    (6, '2025-06-06', 1900.75, 4, 1),
    (7, '2025-06-07', 2800.25, 1, 1);

-- Insertion des ventes pour Magasin B
INSERT INTO ventes (id, date_vente, montant_total, employe_id, magasin_id) VALUES
    (8, '2025-06-01', 900.50, 3, 2),
    (9, '2025-06-02', 1200.75, 4, 2),
    (10, '2025-06-03', 1500.25, 3, 2),
    (11, '2025-06-04', 1800.00, 4, 2),
    (12, '2025-06-05', 1100.50, 3, 2),
    (13, '2025-06-06', 1300.75, 4, 2),
    (14, '2025-06-07', 1600.25, 3, 2);

-- Insertion des ventes pour Magasin C
INSERT INTO ventes (id, date_vente, montant_total, employe_id, magasin_id) VALUES
    (15, '2025-06-01', 2100.50, 2, 3),
    (16, '2025-06-02', 1800.75, 1, 3),
    (17, '2025-06-03', 2500.25, 2, 3),
    (18, '2025-06-04', 2200.00, 1, 3),
    (19, '2025-06-05', 1900.50, 2, 3),
    (20, '2025-06-06', 2300.75, 1, 3),
    (21, '2025-06-07', 2700.25, 2, 3);

-- Insertion de produits vendus (vente_produit)
INSERT INTO vente_produit (vente_id, produit_id, quantite) VALUES
    (1, 7, 5),  -- 5 Télévisions dans la vente 1
    (1, 8, 2),  -- 2 PS5 dans la vente 1
    (2, 9, 10), -- 10 GTAVI dans la vente 2
    (2, 12, 5); -- 5 Manteaux dans la vente 2

-- Insertion des demandes de réapprovisionnement (stock_central)
INSERT INTO stock_central (produit_id, magasin_id, quantite_demandee, date_demande) VALUES
    (1, 1, 20, '2025-06-01'),  -- Magasin A demande 20 Bananes
    (7, 2, 5, '2025-06-02'),   -- Magasin B demande 5 Télévisions
    (8, 3, 10, '2025-06-03');  -- Magasin C demande 10 PS5

    -- Réinitialisation des séquences après insertion manuelle d'ID
SELECT setval('employes_id_seq', (SELECT MAX(id) FROM employes));
SELECT setval('magasin_id_seq', (SELECT MAX(id) FROM magasin));
SELECT setval('produits_id_seq', (SELECT MAX(id) FROM produits));
SELECT setval('ventes_id_seq', (SELECT MAX(id) FROM ventes));
SELECT setval('vente_produit_id_seq', (SELECT MAX(id) FROM vente_produit));
SELECT setval('retours_id_seq', (SELECT MAX(id) FROM retours));
SELECT setval('retour_produit_id_seq', (SELECT MAX(id) FROM retour_produit));
SELECT setval('stock_magasin_id_seq', (SELECT MAX(id) FROM stock_magasin));
SELECT setval('stock_central_id_seq', (SELECT MAX(id) FROM stock_central));
